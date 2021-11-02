/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.git.extensions.impl.IgnoreNotifyCommit;
import hudson.scm.SCM;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugin.gitea.client.api.GiteaCreateEvent;

/**
 * A {@link SCMHeadEvent} for a {@link GiteaCreateEvent}.
 */
public class GiteaCreateSCMEvent extends AbstractGiteaSCMHeadEvent<GiteaCreateEvent> {
    /**
     * Constructor.
     *
     * @param payload the payload.
     * @param origin  the origin.
     */
    public GiteaCreateSCMEvent(@NonNull GiteaCreateEvent payload, @CheckForNull String origin) {
        super(Type.CREATED, payload, origin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String descriptionFor(@NonNull SCMNavigator navigator) {
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        ref = ref.startsWith(Constants.R_TAGS) ? ref.substring(Constants.R_TAGS.length()) : ref;
        String refType = getPayload().getRefType();
        return "Create event for " + refType + " " + ref + " in repository " + getPayload().getRepository().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String descriptionFor(SCMSource source) {
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        ref = ref.startsWith(Constants.R_TAGS) ? ref.substring(Constants.R_TAGS.length()) : ref;
        String refType = getPayload().getRefType();
        return "Create event for " + refType + " " + ref;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        ref = ref.startsWith(Constants.R_TAGS) ? ref.substring(Constants.R_TAGS.length()) : ref;
        String refType = getPayload().getRefType();
        return "Create event for " + refType + " " + ref + " in repository " +
                getPayload().getRepository().getOwner().getUsername() + "/" +
                getPayload().getRepository().getName();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Map<SCMHead, SCMRevision> headsFor(GiteaSCMSource source) {
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        ref = ref.startsWith(Constants.R_TAGS) ? ref.substring(Constants.R_TAGS.length()) : ref;
        String refType = getPayload().getRefType();
        if ("tag".equals(refType)) {
            TagSCMHead h = new TagSCMHead(ref, System.currentTimeMillis());
            return Collections.<SCMHead, SCMRevision>singletonMap(h, new TagSCMRevision(h, getPayload().getSha()));
        } else {
            BranchSCMHead h = new BranchSCMHead(ref);
            return Collections.<SCMHead, SCMRevision>singletonMap(h, new BranchSCMRevision(h, getPayload().getSha()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMatch(@NonNull SCM scm) {
        URIish uri;
        try {
            uri = new URIish(getPayload().getRepository().getHtmlUrl());
        } catch (URISyntaxException e) {
            return false;
        }
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        if (scm instanceof GitSCM) {
            GitSCM git = (GitSCM) scm;
            if (git.getExtensions().get(IgnoreNotifyCommit.class) != null) {
                return false;
            }
            for (RemoteConfig repository : git.getRepositories()) {
                for (URIish remoteURL : repository.getURIs()) {
                    if (GitStatus.looselyMatches(uri, remoteURL)) {
                        for (BranchSpec branchSpec : git.getBranches()) {
                            if (branchSpec.getName().contains("$")) {
                                // If the branchspec is parametrized, always run the polling
                                return true;
                            } else {
                                if (branchSpec.matches(repository.getName() + "/" + ref)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Our handler.
     */
    @Extension
    public static class HandlerImpl extends GiteaWebhookHandler<GiteaCreateSCMEvent, GiteaCreateEvent> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected GiteaCreateSCMEvent createEvent(GiteaCreateEvent payload, String origin) {
            return new GiteaCreateSCMEvent(payload, origin);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void process(GiteaCreateSCMEvent event) {
            SCMHeadEvent.fireNow(event);
        }
    }
}
