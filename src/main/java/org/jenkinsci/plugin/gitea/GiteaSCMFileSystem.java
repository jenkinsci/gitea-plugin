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

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Queue.Task;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.security.ACL;
import java.io.IOException;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;
import jenkins.scm.api.SCMSourceOwner;
import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugin.gitea.client.api.Gitea;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;

public class GiteaSCMFileSystem extends SCMFileSystem {
    private final GiteaConnection connection;
    private final GiteaRepository repo;
    private final String ref;

    protected GiteaSCMFileSystem(GiteaConnection connection, GiteaRepository repo, String ref,
                                 @CheckForNull SCMRevision rev) throws IOException {
        super(rev);
        this.connection = connection;
        this.repo = repo;
        if (rev != null) {
            if (rev.getHead() instanceof PullRequestSCMHead) {
                this.ref = ((PullRequestSCMRevision) rev).getOrigin().getHash();
            } else if (rev instanceof BranchSCMRevision) {
                this.ref = ((BranchSCMRevision) rev).getHash();
            } else if (rev instanceof TagSCMRevision) {
                this.ref = ((TagSCMRevision) rev).getHash();
            } else {
                this.ref = ref;
            }
        } else {
            this.ref = ref;
        }
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    @Override
    public long lastModified() throws IOException {
        // TODO once https://github.com/go-gitea/gitea/issues/1978
        return 0L;
    }

    @NonNull
    @Override
    public SCMFile getRoot() {
        return new GiteaSCMFile(connection, repo, ref);
    }

    @Extension
    public static class BuilderImpl extends Builder {

        @Override
        public boolean supports(SCM source) {
            // TODO implement a GiteaSCM so we can work for those
            return false;
        }

        @Override
        public boolean supports(SCMSource source) {
            return source instanceof GiteaSCMSource;
        }

        @Override
        protected boolean supportsDescriptor(SCMDescriptor scmDescriptor) {
            // TODO
            return false;
        }

        @Override
        protected boolean supportsDescriptor(SCMSourceDescriptor scmSourceDescriptor) {
            return scmSourceDescriptor instanceof GiteaSCMSource.DescriptorImpl;
        }

        @Override
        public SCMFileSystem build(@NonNull Item owner, @NonNull SCM scm, @CheckForNull SCMRevision rev) {
            return null;
        }

        @Override
        public SCMFileSystem build(@NonNull SCMSource source, @NonNull SCMHead head, @CheckForNull SCMRevision rev)
                throws IOException, InterruptedException {
            GiteaSCMSource src = (GiteaSCMSource) source;
            String repoOwner;
            String repository;
            String ref;
            if (head instanceof PullRequestSCMHead) {
                repoOwner = src.getRepoOwner();
                repository = src.getRepository();
                ref = head.getName();
            } else if (head instanceof BranchSCMHead) {
                repoOwner = src.getRepoOwner();
                repository = src.getRepository();
                ref = head.getName();
            } else if (head instanceof TagSCMHead) {
                repoOwner = src.getRepoOwner();
                repository = src.getRepository();
                ref = head.getName();
            } else {
                return null;
            }
            SCMSourceOwner owner = source.getOwner();
            String serverUrl = src.getServerUrl();
            String credentialsId = src.getCredentialsId();
            StandardCredentials credentials = null;
            if (!StringUtils.isBlank(credentialsId)) {
                Authentication authentication = owner instanceof Task
                    ? ((Task) owner).getDefaultAuthentication()
                    : ACL.SYSTEM;
                credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            StandardCredentials.class,
                            owner,
                            authentication,
                            URIRequirementBuilder.fromUri(serverUrl).build()
                    ),
                    CredentialsMatchers.allOf(
                            AuthenticationTokens.matcher(GiteaAuth.class),
                            CredentialsMatchers.withId(credentialsId)
                    )
                );
            }
            if (owner != null) {
                CredentialsProvider.track(owner, credentials);
            }
            GiteaConnection connection = Gitea.server(serverUrl)
                    .as(AuthenticationTokens.convert(GiteaAuth.class, credentials))
                    .open();
            try {
                return new GiteaSCMFileSystem(connection, connection.fetchRepository(repoOwner, repository), ref, rev);
            } catch (IOException | InterruptedException e) {
                try {
                    connection.close();
                } catch (IOException ioe) {
                    e.addSuppressed(ioe);
                }
                throw e;
            }
        }
    }
}
