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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.TaskListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMSourceRequest;
import org.jenkinsci.plugin.gitea.client.api.GiteaBranch;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaPullRequest;
import org.jenkinsci.plugin.gitea.client.api.GiteaTag;

public class GiteaSCMSourceRequest extends SCMSourceRequest {
    private final boolean fetchBranches;
    private final boolean fetchTags;
    private final boolean fetchOriginPRs;
    private final boolean fetchForkPRs;
    @NonNull
    private final Set<ChangeRequestCheckoutStrategy> originPRStrategies;
    @NonNull
    private final Set<ChangeRequestCheckoutStrategy> forkPRStrategies;
    @CheckForNull
    private final Set<Long> requestedPullRequestNumbers;
    @CheckForNull
    private final Set<String> requestedOriginBranchNames;
    @CheckForNull
    private final Set<String> requestedTagNames;
    @CheckForNull
    private Iterable<GiteaPullRequest> pullRequests;
    @CheckForNull
    private Iterable<GiteaBranch> branches;
    @CheckForNull
    private Iterable<GiteaTag> tags;
    /**
     * The repository collaborator names or {@code null} if not provided.
     */
    @CheckForNull
    private Set<String> collaboratorNames;
    @CheckForNull
    private GiteaConnection connection;

    /**
     * Constructor.
     *
     * @param source   the source.
     * @param context  the context.
     * @param listener the listener.
     */
    GiteaSCMSourceRequest(SCMSource source, GiteaSCMSourceContext context, TaskListener listener) {
        super(source, context, listener);
        fetchBranches = context.wantBranches();
        fetchTags = context.wantTags();
        fetchOriginPRs = context.wantOriginPRs();
        fetchForkPRs = context.wantForkPRs();
        originPRStrategies = fetchOriginPRs && !context.originPRStrategies().isEmpty()
                ? Collections.unmodifiableSet(EnumSet.copyOf(context.originPRStrategies()))
                : Collections.<ChangeRequestCheckoutStrategy>emptySet();
        forkPRStrategies = fetchForkPRs && !context.forkPRStrategies().isEmpty()
                ? Collections.unmodifiableSet(EnumSet.copyOf(context.forkPRStrategies()))
                : Collections.<ChangeRequestCheckoutStrategy>emptySet();
        Set<SCMHead> includes = context.observer().getIncludes();
        if (includes != null) {
            Set<Long> pullRequestNumbers = new HashSet<>(includes.size());
            Set<String> branchNames = new HashSet<>(includes.size());
            Set<String> tagNames = new HashSet<>(includes.size());
            for (SCMHead h : includes) {
                if (h instanceof BranchSCMHead) {
                    branchNames.add(h.getName());
                } else if (h instanceof PullRequestSCMHead) {
                    pullRequestNumbers.add(Long.parseLong(((PullRequestSCMHead) h).getId()));
                    if (SCMHeadOrigin.DEFAULT.equals(h.getOrigin())) {
                        branchNames.add(((PullRequestSCMHead) h).getOriginName());
                    }
                } else if (h instanceof TagSCMHead) {
                    tagNames.add(h.getName());
                }
            }
            this.requestedPullRequestNumbers = Collections.unmodifiableSet(pullRequestNumbers);
            this.requestedOriginBranchNames = Collections.unmodifiableSet(branchNames);
            this.requestedTagNames = Collections.unmodifiableSet(tagNames);
        } else {
            requestedPullRequestNumbers = null;
            requestedOriginBranchNames = null;
            requestedTagNames = null;
        }
    }

    /**
     * Returns {@code true} if branch details need to be fetched.
     *
     * @return {@code true} if branch details need to be fetched.
     */
    public final boolean isFetchBranches() {
        return fetchBranches;
    }

    /**
     * Returns {@code true} if tag details need to be fetched.
     *
     * @return {@code true} if tag details need to be fetched.
     */
    public final boolean isFetchTags() {
        return fetchTags;
    }

    /**
     * Returns {@code true} if pull request details need to be fetched.
     *
     * @return {@code true} if pull request details need to be fetched.
     */
    public final boolean isFetchPRs() {
        return isFetchOriginPRs() || isFetchForkPRs();
    }

    /**
     * Returns {@code true} if origin pull request details need to be fetched.
     *
     * @return {@code true} if origin pull request details need to be fetched.
     */
    public final boolean isFetchOriginPRs() {
        return fetchOriginPRs;
    }

    /**
     * Returns {@code true} if fork pull request details need to be fetched.
     *
     * @return {@code true} if fork pull request details need to be fetched.
     */
    public final boolean isFetchForkPRs() {
        return fetchForkPRs;
    }

    /**
     * Returns the {@link ChangeRequestCheckoutStrategy} to create for each origin pull request.
     *
     * @return the {@link ChangeRequestCheckoutStrategy} to create for each origin pull request.
     */
    @NonNull
    public final Set<ChangeRequestCheckoutStrategy> getOriginPRStrategies() {
        return originPRStrategies;
    }

    /**
     * Returns the {@link ChangeRequestCheckoutStrategy} to create for each fork pull request.
     *
     * @return the {@link ChangeRequestCheckoutStrategy} to create for each fork pull request.
     */
    @NonNull
    public final Set<ChangeRequestCheckoutStrategy> getForkPRStrategies() {
        return forkPRStrategies;
    }

    /**
     * Returns the {@link ChangeRequestCheckoutStrategy} to create for pull requests of the specified type.
     *
     * @param fork {@code true} to return strategies for the fork pull requests, {@code false} for origin pull requests.
     * @return the {@link ChangeRequestCheckoutStrategy} to create for each pull request.
     */
    @NonNull
    public final Set<ChangeRequestCheckoutStrategy> getPRStrategies(boolean fork) {
        if (fork) {
            return fetchForkPRs ? getForkPRStrategies() : Collections.<ChangeRequestCheckoutStrategy>emptySet();
        }
        return fetchOriginPRs ? getOriginPRStrategies() : Collections.<ChangeRequestCheckoutStrategy>emptySet();
    }

    /**
     * Returns the {@link ChangeRequestCheckoutStrategy} to create for each pull request.
     *
     * @return a map of the {@link ChangeRequestCheckoutStrategy} to create for each pull request keyed by whether the
     * strategy applies to forks or not ({@link Boolean#FALSE} is the key for origin pull requests)
     */
    public final Map<Boolean, Set<ChangeRequestCheckoutStrategy>> getPRStrategies() {
        Map<Boolean, Set<ChangeRequestCheckoutStrategy>> result = new HashMap<>();
        for (Boolean fork : new Boolean[]{Boolean.TRUE, Boolean.FALSE}) {
            result.put(fork, getPRStrategies(fork));
        }
        return result;
    }

    /**
     * Returns requested pull request numbers.
     *
     * @return the requested pull request numbers or {@code null} if the request was not scoped to a subset of pull
     * requests.
     */
    @CheckForNull
    public final Set<Long> getRequestedPullRequestNumbers() {
        return requestedPullRequestNumbers;
    }

    /**
     * Gets requested origin branch names.
     *
     * @return the requested origin branch names or {@code null} if the request was not scoped to a subset of branches.
     */
    @CheckForNull
    public final Set<String> getRequestedOriginBranchNames() {
        return requestedOriginBranchNames;
    }

    /**
     * Gets requested tag names.
     *
     * @return the requested tag names or {@code null} if the request was not scoped to a subset of tags.
     */
    @CheckForNull
    public final Set<String> getRequestedTagNames() {
        return requestedTagNames;
    }

    /**
     * Returns the pull request details or an empty list if either the request did not specify to {@link #isFetchPRs()}
     * or if the pull request details have not been provided by {@link #setPullRequests(Iterable)} yet.
     *
     * @return the details of pull requests, may be limited by {@link #getRequestedPullRequestNumbers()} or
     * may be empty if not {@link #isFetchPRs()}
     */
    @NonNull
    public Iterable<GiteaPullRequest> getPullRequests() {
        return Util.fixNull(pullRequests);
    }

    /**
     * Provides the requests with the pull request details.
     *
     * @param pullRequests the pull request details.
     */
    public void setPullRequests(@CheckForNull Iterable<GiteaPullRequest> pullRequests) {
        this.pullRequests = pullRequests;
    }

    /**
     * Returns the branch details or an empty list if either the request did not specify to {@link #isFetchBranches()}
     * or if the branch details have not been provided by {@link #setBranches(Iterable)} yet.
     *
     * @return the branch details (may be empty)
     */
    @NonNull
    public final Iterable<GiteaBranch> getBranches() {
        return Util.fixNull(branches);
    }

    /**
     * Provides the requests with the branch details.
     *
     * @param branches the branch details.
     */
    public final void setBranches(@CheckForNull Iterable<GiteaBranch> branches) {
        this.branches = branches;
    }

    /**
     * Returns the branch details or an empty list if either the request did not specify to {@link #isFetchBranches()}
     * or if the branch details have not been provided by {@link #setBranches(Iterable)} yet.
     *
     * @return the branch details (may be empty)
     */
    @NonNull
    public final Iterable<GiteaTag> getTags() {
        return Util.fixNull(tags);
    }

    /**
     * Provides the requests with the branch details.
     *
     * @param tags the branch details.
     */
    public final void setTags(@CheckForNull Iterable<GiteaTag> tags) {
        this.tags = tags;
    }

    /**
     * Returns the names of the repository collaborators or {@code null} if those details have not been provided yet.
     *
     * @return the names of the repository collaborators or {@code null} if those details have not been provided yet.
     */
    public final Set<String> getCollaboratorNames() {
        return collaboratorNames;
    }

    /**
     * Provides the request with the names of the repository collaborators.
     *
     * @param collaboratorNames the names of the repository collaborators.
     */
    public final void setCollaboratorNames(@CheckForNull Set<String> collaboratorNames) {
        this.collaboratorNames = collaboratorNames;
    }

    /**
     * Returns the {@link GiteaConnection} to use for the request.
     *
     * @return the {@link GiteaConnection} to use for the request or {@code null} if caller should establish
     * their own.
     */
    @CheckForNull
    public GiteaConnection getConnection() {
        return connection;
    }

    /**
     * Provides the {@link GiteaConnection} to use for the request.
     *
     * @param gitHub {@link GiteaConnection} to use for the request.
     */
    public void setConnection(@CheckForNull GiteaConnection gitHub) {
        this.connection = gitHub;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        IOException exception = null;
        for (Object o : Arrays.asList(pullRequests, branches, tags)) {
            if (o instanceof Closeable) {
                try {
                    ((Closeable) o).close();
                } catch (IOException e) {
                    if (exception == null) {
                        exception = e;
                    } else {
                        exception.addSuppressed(e);
                    }
                }
            }
        }
        try {
            super.close();
        } catch (IOException e) {
            if (exception == null) {
                throw e;
            }
            exception.addSuppressed(e);
        }
        if (exception != null) {
            throw exception;
        }
    }
}
