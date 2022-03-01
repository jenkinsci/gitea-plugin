package org.jenkinsci.plugin.gitea.client.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuthNone;
import org.jenkinsci.plugin.gitea.client.api.GiteaBranch;
import org.jenkinsci.plugin.gitea.client.api.GiteaCommitStatus;
import org.jenkinsci.plugin.gitea.client.api.GiteaHook;
import org.jenkinsci.plugin.gitea.client.api.GiteaIssue;
import org.jenkinsci.plugin.gitea.client.api.GiteaOrganization;
import org.jenkinsci.plugin.gitea.client.api.GiteaOwner;
import org.jenkinsci.plugin.gitea.client.api.GiteaPullRequest;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.jenkinsci.plugin.gitea.client.api.GiteaTag;
import org.jenkinsci.plugin.gitea.client.api.GiteaUser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DefaultGiteaConnection_PagedRequests_Test {

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
    public void test_fetchOrganizationRepositories_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/orgs//repos";
        String page2Url = "http://server.com/api/v1/orgs//repos?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "repoResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "repoResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaRepository> repositories = giteaConnection
                    .fetchRepositories(new GiteaOrganization("", "", "", "", "", ""));
            assertThat(repositories.size(), is(2));
        }
    }

    @Test
    public void test_fetchUserRepositories_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/users//repos";
        String page2Url = "http://server.com/api/v1/users//repos?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "repoResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "repoResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaRepository> repositories = giteaConnection.fetchRepositories(new GiteaOwner("", "", "", ""));
            assertThat(repositories.size(), is(2));
        }
    }

    @Test
    public void test_fetchCurrentUserRepositories_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/user/repos";
        String page2Url = "http://server.com/api/v1/user/repos?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "repoResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "repoResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaRepository> repositories = giteaConnection.fetchCurrentUserRepositories();
            assertThat(repositories.size(), is(2));
        }
    }

    @Test
    public void test_fetchBranches_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/repos///branches";
        String page2Url = "http://server.com/api/v1/repos///branches?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "branchesResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "branchesResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaBranch> branches = giteaConnection.fetchBranches("", "");
            assertThat(branches.size(), is(2));
        }
    }

    @Test
    public void test_fetchTags_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/repos///tags";
        String page2Url = "http://server.com/api/v1/repos///tags?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "tagsResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "tagsResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaTag> tags = giteaConnection.fetchTags("", "");
            assertThat(tags.size(), is(2));
        }
    }

    @Test
    public void test_fetchCollaborators_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/repos///collaborators";
        String page2Url = "http://server.com/api/v1/repos///collaborators?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "usersResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "usersResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaUser> users = giteaConnection.fetchCollaborators("", "");
            assertThat(users.size(), is(2));
        }
    }

    @Test
    public void test_fetchHooks_from_user_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/repos///hooks";
        String page2Url = "http://server.com/api/v1/repos///hooks?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "hooksResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "hooksResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaHook> hooks = giteaConnection.fetchHooks("", "");
            assertThat(hooks.size(), is(2));
        }
    }

    @Test
    public void test_fetchHooks_from_org_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/orgs//hooks";
        String page2Url = "http://server.com/api/v1/orgs//hooks?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "hooksResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "hooksResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaHook> hooks = giteaConnection.fetchHooks("");
            assertThat(hooks.size(), is(2));
        }
    }

    @Test
    public void test_fetchCommitStatuses_from_org_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/repos///statuses/sha";
        String page2Url = "http://server.com/api/v1/repos///statuses/sha?page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "commitStatusResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "commitStatusResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaCommitStatus> commitStates = giteaConnection.fetchCommitStatuses(giteaRepository, "sha");
            assertThat(commitStates.size(), is(2));
        }
    }

    @Test
    public void test_fetchPullRequests_from_org_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/repos///pulls?state=open";
        String page2Url = "http://server.com/api/v1/repos///pulls?state=open&page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "pullRequestsResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "pullRequestsResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaPullRequest> pullRequests = giteaConnection.fetchPullRequests("", "");
            assertThat(pullRequests.size(), is(2));
        }
    }

    @Test
    public void test_fetchIssues_from_org_with_paged_response() throws Exception {
        HashMap<String, HttpURLConnection> mocks = new HashMap<>();
        String page1Url = "http://server.com/api/v1/repos///issues?state=open";
        String page2Url = "http://server.com/api/v1/repos///issues?state=open&page2";
        mocks.put(page1Url, createUrlConnectionMock(200, "issuesResponse.json", page2Url));
        mocks.put(page2Url, createUrlConnectionMock(200, "issuesResponse.json"));
        try (DefaultGiteaConnection giteaConnection = new GiteaConnection_PagedRequests("http://server.com",
                new GiteaAuthNone(), mocks)) {
            List<GiteaIssue> issues = giteaConnection.fetchIssues(giteaRepository);
            assertThat(issues.size(), is(2));
        }
    }

    private HttpURLConnection createUrlConnectionMock(int statusCode, String responseResource) throws IOException {
        return createUrlConnectionMock(statusCode, responseResource, null);
    }

    private HttpURLConnection createUrlConnectionMock(int statusCode, String responseResource, String nextPage)
            throws IOException {
        HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);
        Mockito.when(connection.getResponseCode()).thenReturn(statusCode);
        Mockito.when(connection.getInputStream()).thenReturn(this.getClass().getResourceAsStream(responseResource));
        if (nextPage != null) {
            Mockito.when(connection.getHeaderField("Link")).thenReturn(String.format("<%s>; rel=\"next\"", nextPage));
        }
        return connection;
    }

}
