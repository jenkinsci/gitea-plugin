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

import com.cloudbees.jenkins.plugins.sshcredentials.SSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.UriTemplateBuilder;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.plugins.git.GitSCM;
import hudson.security.ACL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import jenkins.plugins.git.GitSCMBuilder;
import jenkins.plugins.git.MergeWithGitSCMExtension;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.RefSpec;
import org.jenkinsci.plugin.gitea.credentials.PersonalAccessToken;

/**
 * Builds a {@link GitSCM} for {@link GiteaSCMSource}.
 */
public class GiteaSCMBuilder extends GitSCMBuilder<GiteaSCMBuilder> {
    /**
     * The context within which credentials should be resolved.
     */
    @CheckForNull
    private final SCMSourceOwner context;
    /**
     * The server URL
     */
    @NonNull
    private final String serverUrl;
    /**
     * The repository owner.
     */
    @NonNull
    private final String repoOwner;
    /**
     * The repository name.
     */
    @NonNull
    private final String repository;
    private final String sshRemote;

    /**
     * Constructor.
     *
     * @param source   the {@link GiteaSCMSource}.
     * @param head     the {@link SCMHead}
     * @param revision the (optional) {@link SCMRevision}
     */
    public GiteaSCMBuilder(@NonNull GiteaSCMSource source,
                           @NonNull SCMHead head, @CheckForNull SCMRevision revision) {
        super(
                head,
                revision,
                checkoutUriTemplate(null, source.getServerUrl(), null, null)
                        .set("owner", source.getRepoOwner())
                        .set("repository", source.getRepository())
                        .expand(),
                source.getCredentialsId()
        );
        this.context = source.getOwner();
        serverUrl = source.getServerUrl();
        repoOwner = source.getRepoOwner();
        repository = source.getRepository();
        sshRemote = source.getSshRemote();
        // now configure the ref specs
        withoutRefSpecs();
        String repoUrl;
        if (head instanceof PullRequestSCMHead) {
            PullRequestSCMHead h = (PullRequestSCMHead) head;
            withRefSpec("+refs/pull/" + h.getId() + "/head:refs/remotes/@{remote}/" + head
                    .getName());
            repoUrl = repositoryUrl(h.getOriginOwner(), h.getOriginRepository());
        } else if (head instanceof TagSCMHead) {
            withRefSpec("+refs/tags/" + head.getName() + ":refs/tags/@{remote}/" + head.getName());
            repoUrl = repositoryUrl(repoOwner, repository);
        } else {
            withRefSpec("+refs/heads/" + head.getName() + ":refs/remotes/@{remote}/" + head.getName());
            repoUrl = repositoryUrl(repoOwner, repository);
        }
        // pre-configure the browser
        withBrowser(new GiteaBrowser(repoUrl));
    }

    /**
     * Returns a {@link UriTemplate} for checkout according to credentials configuration.
     * Expects the parameters {@code owner} and {@code repository} to be populated before expansion.
     *
     * @param context       the context within which to resolve the credentials.
     * @param serverUrl     the server url
     * @param sshRemote     any valid SSH remote URL for the server.
     * @param credentialsId the credentials.
     * @return a {@link UriTemplate}
     */
    public static UriTemplate checkoutUriTemplate(@CheckForNull Item context,
                                                  @NonNull String serverUrl,
                                                  @CheckForNull String sshRemote,
                                                  @CheckForNull String credentialsId) {
        if (credentialsId != null && sshRemote != null) {
            URIRequirementBuilder builder = URIRequirementBuilder.create();
            URI serverUri = URI.create(serverUrl);
            if (serverUri.getHost() != null) {
                builder.withHostname(serverUri.getHost());
            }
            StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            StandardCredentials.class,
                            context,
                            context instanceof Queue.Task
                                    ? ((Queue.Task) context).getDefaultAuthentication()
                                    : ACL.SYSTEM,
                            builder.build()
                    ),
                    CredentialsMatchers.allOf(
                            CredentialsMatchers.withId(credentialsId),
                            CredentialsMatchers.instanceOf(StandardCredentials.class)
                    )
            );
            if (credentials instanceof SSHUserPrivateKey) {
                int atIndex = sshRemote.indexOf('@');
                int colonIndex = sshRemote.indexOf(':');
                if (atIndex != -1 && colonIndex != -1 && atIndex < colonIndex) {
                    // this is an scp style url, we will translate to ssh style
                    return UriTemplate.buildFromTemplate("ssh://" + sshRemote.substring(0, colonIndex))
                            .path(UriTemplateBuilder.var("owner"))
                            .path(UriTemplateBuilder.var("repository"))
                            .literal(".git")
                            .build();
                }
                URI sshUri = URI.create(sshRemote);
                String username = ((SSHUserPrivateKey) credentials).getUsername();
                if (username.equals(System.getProperty("user.name"))) {
                    username = "git";
                }
                return UriTemplate.buildFromTemplate(
                                "ssh://" + username + "@" + sshUri.getHost() + (sshUri.getPort() != 22 && sshUri.getPort() != -1 ? ":" + sshUri.getPort() : "")
                        )
                        .path(UriTemplateBuilder.var("owner"))
                        .path(UriTemplateBuilder.var("repository"))
                        .literal(".git")
                        .build();
            }
            if (credentials instanceof PersonalAccessToken) {
                try {
                    // TODO is there a way we can get git plugin to redact the secret?
                    URI tokenUri = new URI(
                            serverUri.getScheme(),
                            ((PersonalAccessToken) credentials).getToken().getPlainText(),
                            serverUri.getHost(),
                            serverUri.getPort(),
                            serverUri.getPath(),
                            serverUri.getQuery(),
                            serverUri.getFragment()
                    );
                    return UriTemplate.buildFromTemplate(tokenUri.toASCIIString())
                            .path(UriTemplateBuilder.var("owner"))
                            .path(UriTemplateBuilder.var("repository"))
                            .literal(".git")
                            .build();
                } catch (URISyntaxException e) {
                    // ok we are at the end of the road
                }
            }
        }
        return UriTemplate.buildFromTemplate(serverUrl)
                .path(UriTemplateBuilder.var("owner"))
                .path(UriTemplateBuilder.var("repository"))
                .literal(".git")
                .build();
    }

    public final String repositoryUrl(String owner, String repository) {
        return UriTemplate.buildFromTemplate(serverUrl)
                .path(UriTemplateBuilder.var("owner"))
                .path(UriTemplateBuilder.var("repository"))
                .build()
                .set("owner", owner)
                .set("repository", repository)
                .expand();
    }

    /**
     * Returns a {@link UriTemplate} for checkout according to credentials configuration.
     * Expects the parameters {@code owner} and {@code repository} to be populated before expansion.
     *
     * @return a {@link UriTemplate}
     */
    @NonNull
    public final UriTemplate checkoutUriTemplate() {
        String credentialsId = credentialsId();
        return checkoutUriTemplate(context, serverUrl, sshRemote, credentialsId);
    }

    /**
     * Updates the {@link GitSCMBuilder#withRemote(String)} based on the current {@link #head()} and
     * {@link #revision()}.
     * Will be called automatically by {@link #build()} but exposed in case the correct remote is required after
     * changing the {@link #withCredentials(String)}.
     *
     * @return {@code this} for method chaining.
     */
    @NonNull
    public final GiteaSCMBuilder withGiteaRemote() {
        withRemote(checkoutUriTemplate().set("owner", repoOwner).set("repository", repository).expand());
        final SCMHead h = head();
        String repoUrl;
        if (h instanceof PullRequestSCMHead) {
            final PullRequestSCMHead head = (PullRequestSCMHead) h;
            repoUrl = repositoryUrl(head.getOriginOwner(), head.getOriginRepository());
        } else {
            repoUrl = repositoryUrl(repoOwner, repository);
        }
        if (repoUrl != null) {
            withBrowser(new GiteaBrowser(repoUrl));
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public GitSCM build() {
        final SCMHead h = head();
        final SCMRevision r = revision();
        try {
            withGiteaRemote();

            if (h instanceof PullRequestSCMHead) {
                PullRequestSCMHead head = (PullRequestSCMHead) h;
                if (head.getCheckoutStrategy() == ChangeRequestCheckoutStrategy.MERGE) {
                    // add the target branch to ensure that the revision we want to merge is also available
                    String name = head.getTarget().getName();
                    String localName = "remotes/" + remoteName() + "/" + name;
                    Set<String> localNames = new HashSet<>();
                    boolean match = false;
                    String targetSrc = Constants.R_HEADS + name;
                    String targetDst = Constants.R_REMOTES + remoteName() + "/" + name;
                    for (RefSpec b : asRefSpecs()) {
                        String dst = b.getDestination();
                        assert dst.startsWith(Constants.R_REFS)
                                : "All git references must start with refs/";
                        if (targetSrc.equals(b.getSource())) {
                            if (targetDst.equals(dst)) {
                                match = true;
                            } else {
                                // pick up the configured destination name
                                localName = dst.substring(Constants.R_REFS.length());
                                match = true;
                            }
                        } else {
                            localNames.add(dst.substring(Constants.R_REFS.length()));
                        }
                    }
                    if (!match) {
                        if (localNames.contains(localName)) {
                            // conflict with intended name
                            localName = "remotes/" + remoteName() + "/upstream-" + name;
                        }
                        if (localNames.contains(localName)) {
                            // conflict with intended alternative name
                            localName = "remotes/" + remoteName() + "/pr-" + head.getId() + "-upstream-" + name;
                        }
                        if (localNames.contains(localName)) {
                            // ok we're just going to mangle our way to something that works
                            while (localNames.contains(localName)) {
                                localName = "remotes/" + remoteName() + "/pr-" + head.getId() + "-upstream-" + name
                                        + "-" + Integer.toHexString(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
                            }
                        }
                        withRefSpec("+refs/heads/" + name + ":refs/" + localName);
                    }
                    withExtension(new MergeWithGitSCMExtension(
                                    localName,
                                    r instanceof PullRequestSCMRevision
                                            ? ((BranchSCMRevision) ((PullRequestSCMRevision) r).getTarget()).getHash()
                                            : null
                            )
                    );
                }
                if (r instanceof PullRequestSCMRevision) {
                    withRevision(((PullRequestSCMRevision) r).getOrigin());
                }
            }
            return super.build();
        } finally {
            withHead(h);
            withRevision(r);
        }
    }
}
