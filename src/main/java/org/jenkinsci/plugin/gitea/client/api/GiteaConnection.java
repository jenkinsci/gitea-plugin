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
package org.jenkinsci.plugin.gitea.client.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * The Gitea Client connection, use {@link Gitea} to open a connection. Example:
 * <pre>
 *     try (GiteaConnection c = Gitea.server(...).as(...).open()) {
 *         // use the connection
 *     }
 * </pre>
 */
public interface GiteaConnection extends AutoCloseable {

    GiteaVersion fetchVersion() throws IOException, InterruptedException;

    GiteaUser fetchCurrentUser() throws IOException, InterruptedException;

    GiteaUser fetchUser(String name) throws IOException, InterruptedException;

    GiteaOrganization fetchOrganization(String name) throws IOException, InterruptedException;

    GiteaRepository fetchRepository(String username, String name) throws IOException, InterruptedException;

    GiteaRepository fetchRepository(GiteaOwner owner, String name) throws IOException, InterruptedException;

    List<GiteaRepository> fetchCurrentUserRepositories() throws IOException, InterruptedException;

    List<GiteaRepository> fetchRepositories(String username) throws IOException, InterruptedException;

    List<GiteaRepository> fetchRepositories(GiteaOwner owner) throws IOException, InterruptedException;

    GiteaBranch fetchBranch(String username, String repository, String name) throws IOException, InterruptedException;

    GiteaBranch fetchBranch(GiteaRepository repository, String name) throws IOException, InterruptedException;

    List<GiteaBranch> fetchBranches(String username, String name) throws IOException, InterruptedException;

    List<GiteaBranch> fetchBranches(GiteaRepository repository) throws IOException, InterruptedException;

    List<GiteaUser> fetchCollaborators(String username, String name) throws IOException, InterruptedException;

    List<GiteaUser> fetchCollaborators(GiteaRepository repository) throws IOException, InterruptedException;

    boolean checkCollaborator(String username, String name, String collaboratorName)
            throws IOException, InterruptedException;

    boolean checkCollaborator(GiteaRepository repository, String collaboratorName)
            throws IOException, InterruptedException;

    List<GiteaHook> fetchHooks(String organizationName) throws IOException, InterruptedException;

    List<GiteaHook> fetchHooks(GiteaOrganization organization) throws IOException, InterruptedException;

    GiteaHook createHook(GiteaOrganization organization, GiteaHook hook) throws IOException, InterruptedException;

    void deleteHook(GiteaOrganization organization, GiteaHook hook) throws IOException, InterruptedException;

    void deleteHook(GiteaOrganization organization, long id) throws IOException, InterruptedException;

    void updateHook(GiteaOrganization organization, GiteaHook hook) throws IOException, InterruptedException;

    List<GiteaHook> fetchHooks(String username, String name) throws IOException, InterruptedException;

    List<GiteaHook> fetchHooks(GiteaRepository repository) throws IOException, InterruptedException;

    GiteaHook createHook(GiteaRepository repository, GiteaHook hook) throws IOException, InterruptedException;

    void deleteHook(GiteaRepository repository, GiteaHook hook) throws IOException, InterruptedException;

    void deleteHook(GiteaRepository repository, long id) throws IOException, InterruptedException;

    void updateHook(GiteaRepository repository, GiteaHook hook) throws IOException, InterruptedException;

    List<GiteaCommitStatus> fetchCommitStatuses(GiteaRepository repository, String sha)
            throws IOException, InterruptedException;

    GiteaCommitStatus createCommitStatus(String username, String repository, String sha, GiteaCommitStatus status)
            throws IOException, InterruptedException;

    GiteaCommitStatus createCommitStatus(GiteaRepository repository, String sha, GiteaCommitStatus status)
            throws IOException, InterruptedException;

    GiteaPullRequest fetchPullRequest(String username, String name, long id) throws IOException, InterruptedException;

    GiteaPullRequest fetchPullRequest(GiteaRepository repository, long id) throws IOException, InterruptedException;

    List<GiteaPullRequest> fetchPullRequests(String username, String name) throws IOException, InterruptedException;

    List<GiteaPullRequest> fetchPullRequests(GiteaRepository repository) throws IOException, InterruptedException;

    List<GiteaPullRequest> fetchPullRequests(String username, String name, Set<GiteaIssueState> states)
            throws IOException, InterruptedException;

    List<GiteaPullRequest> fetchPullRequests(GiteaRepository repository, Set<GiteaIssueState> states)
            throws IOException, InterruptedException;

    List<GiteaIssue> fetchIssues(String username, String name) throws IOException, InterruptedException;

    List<GiteaIssue> fetchIssues(GiteaRepository repository) throws IOException, InterruptedException;

    List<GiteaIssue> fetchIssues(String username, String name, Set<GiteaIssueState> states)
            throws IOException, InterruptedException;

    List<GiteaIssue> fetchIssues(GiteaRepository repository, Set<GiteaIssueState> states)
            throws IOException, InterruptedException;

    byte[] fetchFile(GiteaRepository repository, String ref, String path) throws IOException, InterruptedException;

    boolean checkFile(GiteaRepository repository, String ref, String path) throws IOException, InterruptedException;

    /**
     * {@inheritDoc}
     */
    @Override
    void close() throws IOException;
}
