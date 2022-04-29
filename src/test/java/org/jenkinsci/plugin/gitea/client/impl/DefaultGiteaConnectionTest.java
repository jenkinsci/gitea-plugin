package org.jenkinsci.plugin.gitea.client.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuthNone;
import org.jenkinsci.plugin.gitea.client.api.GiteaIssue;
import org.jenkinsci.plugin.gitea.client.api.GiteaIssueState;
import org.jenkinsci.plugin.gitea.client.api.GiteaOwner;
import org.jenkinsci.plugin.gitea.client.api.GiteaPullRequest;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test cases for fetching pull requests or issues of a repository where those options are disabled
 */
public class DefaultGiteaConnectionTest {

    private DefaultGiteaConnection giteaConnection = new GiteaConnection_DisabledPR_Issues("", new GiteaAuthNone());
    private GiteaRepository giteaRepository;

    @Before
    public void reset() {
        giteaRepository = new GiteaRepository(
                new GiteaOwner("", "", "", ""),
                null, "", "", "",
                true, false, false, false, false,
                "", "", "", "",
                0L, 0L, 0L, 0L, "", "",
                null
        );
    }

    @Test
    public void test_fetchPullRequests_with_disabled_prs__given_username_repoName_state() throws Exception {
        List<GiteaPullRequest> pr = giteaConnection.fetchPullRequests("", "", Collections.singleton(GiteaIssueState.OPEN));
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
        pr = giteaConnection.fetchPullRequests("", "", Collections.singleton(GiteaIssueState.CLOSED));
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
    }

    @Test
    public void test_fetchPullRequests_with_disabled_prs__given_username_repoName() throws IOException, InterruptedException {
        List<GiteaPullRequest> pr = giteaConnection.fetchPullRequests("", "");
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
    }

    @Test
    public void test_fetchPullRequests_with_disabled_prs__given_repo() throws IOException, InterruptedException {
        List<GiteaPullRequest> pr = giteaConnection.fetchPullRequests(giteaRepository);
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
    }

    @Test
    public void test_fetchPullRequests_with_disabled_prs__given_repo_state() throws IOException, InterruptedException {
        List<GiteaPullRequest> pr = giteaConnection.fetchPullRequests(giteaRepository, Collections.singleton(GiteaIssueState.OPEN));
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
        pr = giteaConnection.fetchPullRequests(giteaRepository, Collections.singleton(GiteaIssueState.CLOSED));
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
    }

    @Test
    public void test_fetchIssues_with_disabled_issues__given_username_repoName_state() throws IOException, InterruptedException {
        List<GiteaIssue> issues = giteaConnection.fetchIssues("", "", Collections.singleton(GiteaIssueState.OPEN));
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
        issues = giteaConnection.fetchIssues("", "", Collections.singleton(GiteaIssueState.CLOSED));
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void test_fetchIssues_with_disabled_issues__given_repo_state() throws IOException, InterruptedException {
        List<GiteaIssue> issues = giteaConnection.fetchIssues(giteaRepository, Collections.singleton(GiteaIssueState.OPEN));
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
        issues = giteaConnection.fetchIssues(giteaRepository, Collections.singleton(GiteaIssueState.CLOSED));
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void test_fetchIssues_with_disabled_issues__given_repo() throws IOException, InterruptedException {
        List<GiteaIssue> issues = giteaConnection.fetchIssues(giteaRepository);
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void test_fetchIssues_with_disabled_issues__given_username_repoName() throws IOException, InterruptedException {
        List<GiteaIssue> issues = giteaConnection.fetchIssues("", "");
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }
}
