/*
 * The MIT License
 *
 * Copyright (c) 2017-2020, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugin.gitea;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Job;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.model.listeners.SCMListener;
import hudson.model.queue.QueueListener;
import hudson.model.queue.Tasks;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.LogTaskListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMRevisionAction;
import jenkins.scm.api.SCMSource;
import org.jenkinsci.plugin.gitea.client.api.GiteaCommitState;
import org.jenkinsci.plugin.gitea.client.api.GiteaCommitStatus;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaHttpStatusException;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

/**
 * Notification of commit status information to Gitea.
 */
public class GiteaNotifier {

    /**
     * Our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GiteaNotifier.class.getName());

    /**
     * Sends notifications to Bitbucket on Checkout (for the "In Progress" Status).
     */
    private static void sendNotifications(Run<?, ?> build, TaskListener listener)
            throws IOException, InterruptedException {
        final SCMSource s = SCMSource.SourceByItem.findSource(build.getParent());
        if (!(s instanceof GiteaSCMSource)) {
            return;
        }
        GiteaSCMSource source = (GiteaSCMSource) s;
        if (new GiteaSCMSourceContext(null, SCMHeadObserver.none())
                .withTraits(source.getTraits())
                .notificationsDisabled()) {
            return;
        }
        String url;
        try {
            url = DisplayURLProvider.get().getRunURL(build);
        } catch (IllegalStateException e) {
            listener.getLogger().println(
                    "Can not determine Jenkins root URL. Commit status notifications are disabled until a root URL is"
                            + " configured in Jenkins global configuration.");
            return;
        }
        Result result = build.getResult();
        GiteaCommitStatus status = new GiteaCommitStatus();
        status.setTargetUrl(url);

        if (Result.SUCCESS.equals(result)) {
            status.setDescription("This commit looks good");
            status.setState(GiteaCommitState.SUCCESS);
        } else if (Result.UNSTABLE.equals(result)) {
            status.setDescription("This commit is unstable");
            status.setState(GiteaCommitState.WARNING);
        } else if (Result.FAILURE.equals(result)) {
            status.setDescription("There was a failure building this commit");
            status.setState(GiteaCommitState.FAILURE);
        } else if (Result.NOT_BUILT.equals(result)) {
            status.setDescription("This commit was not built");
            status.setState(GiteaCommitState.WARNING);
        } else if (result != null) { // ABORTED etc.
            status.setDescription("Something is wrong with the build of this commit");
            status.setState(GiteaCommitState.ERROR);
        } else {
            status.setDescription("Build started...");
            status.setState(GiteaCommitState.PENDING);
        }

        SCMRevision revision = SCMRevisionAction.getRevision(source, build);
        String statusContext = stripBranchName(build.getParent()) + "/pipeline/";
        String hash;
        if (revision instanceof BranchSCMRevision) {
            listener.getLogger().format("[Gitea] Notifying branch build status: %s %s%n",
                    status.getState().name(), status.getDescription());
            hash = ((BranchSCMRevision) revision).getHash();
            statusContext += "head";
        } else if (revision instanceof PullRequestSCMRevision) {
            listener.getLogger().format("[Gitea] Notifying pull request build status: %s %s%n",
                    status.getState().name(), status.getDescription());
            hash = ((PullRequestSCMRevision) revision).getOrigin().getHash();
            statusContext += getPrContextTarget(((PullRequestSCMRevision) revision).getTarget().getHead().getName());
        } else if (revision instanceof TagSCMRevision) {
            listener.getLogger().format("[Gitea] Notifying tag build status: %s %s%n",
                    status.getState().name(), status.getDescription());
            hash = ((TagSCMRevision) revision).getHash();
            statusContext += "tag";
        } else if (revision instanceof ReleaseSCMRevision) {
            listener.getLogger().format("[Gitea] Notifying release build status: %s %s%n",
                    status.getState().name(), status.getDescription());
            hash = ((ReleaseSCMRevision) revision).getHash();
            statusContext += "release";
        } else {
            return;
        }
        status.setContext(statusContext);
        JobScheduledListener jsl = ExtensionList.lookup(QueueListener.class).get(JobScheduledListener.class);
        if (jsl != null) {
            // we are setting the status, so don't let the queue listener background thread change it to pending
            synchronized (jsl.resolving) {
                jsl.resolving.remove(build.getParent());
            }
        }
        try (GiteaConnection c = source.gitea().open()) {
            int tries = 3;
            while (true){
                tries--;
                try {
                    c.createCommitStatus(source.getRepoOwner(), source.getRepository(), hash, status);
                    break;
                } catch (GiteaHttpStatusException e) {
                    if (e.getStatusCode() == 500 && tries > 0) {
                        // server may be overloaded
                        continue;
                    }
                    throw e;
                }
            }
            listener.getLogger().format("[Gitea] Notified%n");
        }
    }

    /**
     * Strips the name of the job the remove either the name of the branch or e.g. PR-1
     * @param job Job with a full name in form of Organisation/Repository/Branch
     * @return Stripped name in form of Organisation/Repository
     */
    private static String stripBranchName(Job job) {
        return job.getFullName().substring(0, job.getFullName().lastIndexOf('/'));
    }

    private static String getPrContextTarget(String targetbranch) {
        return "pr-" + targetbranch;
    }

    /**
     * Listener to catch jobs being added to the build queue.
     */
    @Extension
    public static class JobScheduledListener extends QueueListener {
        /**
         * Track requests so that we can know if there is a newer request.
         */
        private final AtomicLong nonce = new AtomicLong();
        /**
         * Active resolution of likely revisions for queued jobs.
         */
        private final Map<Job, Long> resolving = new HashMap<>();

        /**
         * {@inheritDoc}
         */
        @Override
        public void onEnterWaiting(final Queue.WaitingItem wi) {
            if (!(wi.task instanceof Job)) {
                return;
            }
            final Job<?, ?> job = (Job) wi.task;
            final SCMSource src = SCMSource.SourceByItem.findSource(job);
            if (!(src instanceof GiteaSCMSource)) {
                return;
            }
            final GiteaSCMSource source = (GiteaSCMSource) src;
            if (new GiteaSCMSourceContext(null, SCMHeadObserver.none())
                    .withTraits(source.getTraits())
                    .notificationsDisabled()) {
                return;
            }
            final SCMHead head = SCMHead.HeadByItem.findHead(job);
            if (head == null) {
                return;
            }
            final Long nonce = this.nonce.incrementAndGet();
            synchronized (resolving) {
                resolving.put(job, nonce);
            }
            // prevent delays in the queue when updating Gitea
            Computer.threadPoolForRemoting.submit(new Runnable() {
                @Override
                public void run() {
                    try(ACLContext context = ACL.as(Tasks.getAuthenticationOf(wi.task))) {
                        // we need to determine the revision that *should* be built so that we can tag that as pending
                        SCMRevision revision = source.fetch(head, new LogTaskListener(LOGGER, Level.INFO));
                        String hash;
                        String statusContext = stripBranchName(job) + "/pipeline/";
                        if (revision instanceof BranchSCMRevision) {
                            LOGGER.log(Level.INFO, "Notifying branch pending build {0}", job.getFullName());
                            hash = ((BranchSCMRevision) revision).getHash();
                            statusContext += "head";
                        } else if (revision instanceof PullRequestSCMRevision) {
                            LOGGER.log(Level.INFO, "Notifying pull request pending build {0}", job.getFullName());
                            hash = ((PullRequestSCMRevision) revision).getOrigin().getHash();
                            statusContext += getPrContextTarget(((PullRequestSCMRevision) revision).getTarget().getHead().getName());
                        } else if (revision instanceof TagSCMRevision) {
                            LOGGER.log(Level.INFO, "Notifying tag pending build {0}", job.getFullName());
                            statusContext += "tag";
                            hash = ((TagSCMRevision) revision).getHash();
                        } else if (revision instanceof ReleaseSCMRevision) {
                            LOGGER.log(Level.INFO, "Notifying release pending build {0}", job.getFullName());
                            statusContext += "release";
                            hash = ((ReleaseSCMRevision) revision).getHash();
                        } else {
                            return;
                        }
                        String url;
                        try {
                            url = DisplayURLProvider.get().getJobURL(job);
                        } catch (IllegalStateException e) {
                            // no root url defined, cannot notify, let's get out of here
                            return;
                        }
                        GiteaCommitStatus status = new GiteaCommitStatus();
                        status.setTargetUrl(url);
                        status.setContext(statusContext);
                        status.setDescription("Build queued...");
                        status.setState(GiteaCommitState.PENDING);

                        // now it may have taken some time to collect the information, we only want to set the status
                        // if our status would still be relevant... so if another build was scheduled by say another
                        // commit, or if the build was actually flagged as started, then in those cases we should
                        // skip setting the status... this is where the nonce comes in... if anything else
                        // has set the status for this job, they will have removed the nonce and the job is re-submitted
                        // to the build queue then the nonce will have been changed from our tasks value
                        //
                        // Note: we could have some race conditions on the pending status if a job is scheduled
                        // while already running, as in that case if the running job completes while our request
                        // is in-flight then the pending would not get set... but that will ultimately be resolved
                        // once the second job completes, so not seen as important enough to worry about
                        try (GiteaConnection c = source.gitea().open()) {
                            // check are we still the task to set pending
                            synchronized (resolving) {
                                if (!nonce.equals(resolving.get(job))) {
                                    // it's not our nonce, so drop
                                    LOGGER.log(Level.INFO,
                                            "{0} has already started, skipping notification of queued",
                                            job.getFullName());
                                    return;
                                }
                                // it is our nonce, so remove it
                                resolving.remove(job);
                            }
                            c.createCommitStatus(source.getRepoOwner(), source.getRepository(), hash, status);
                            LOGGER.log(Level.INFO, "{0} Notified", job.getFullName());
                        }
                    } catch (IOException | InterruptedException e) {
                        LOGGER.log(Level.INFO,
                                "Could not send commit status notification for " + job.getFullName() + " to " + source
                                        .getServerUrl(), e);
                    }
                }
            });
        }

    }

    @Extension
    public static class JobCheckOutListener extends SCMListener {
        @Override
        public void onCheckout(Run<?, ?> build, SCM scm, FilePath workspace, TaskListener listener, File changelogFile,
                               SCMRevisionState pollingBaseline) throws Exception {
            try {
                sendNotifications(build, listener);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace(listener.error("Could not send notifications"));
            }
        }

    }

    /**
     * Sends notifications to Bitbucket on Run completed.
     */
    @Extension
    public static class JobCompletedListener extends RunListener<Run<?, ?>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCompleted(Run<?, ?> build, TaskListener listener) {
            try {
                sendNotifications(build, listener);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace(listener.error("Could not send notifications"));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onStarted(Run<?, ?> run, TaskListener listener) {
            try {
                sendNotifications(run, listener);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace(listener.error("Could not send notifications"));
            }
        }
    }

}
