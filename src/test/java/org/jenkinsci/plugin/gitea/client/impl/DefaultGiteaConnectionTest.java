package org.jenkinsci.plugin.gitea.client.impl;

import com.damnhandy.uri.template.UriTemplate;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuthNone;
import org.jenkinsci.plugin.gitea.client.api.GiteaHttpStatusException;
import org.jenkinsci.plugin.gitea.client.api.GiteaIssue;
import org.jenkinsci.plugin.gitea.client.api.GiteaIssueState;
import org.jenkinsci.plugin.gitea.client.api.GiteaOwner;
import org.jenkinsci.plugin.gitea.client.api.GiteaPullRequest;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;

/**
 * Test cases for fetching pull requests or issues of a repository where those options are disabled
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DefaultGiteaConnection.class)
public class DefaultGiteaConnectionTest {

    private DefaultGiteaConnection mockedDefaultGiteaConnection = new DefaultGiteaConnection("", new GiteaAuthNone());
    private GiteaRepository giteaRepository;

    @Before
    public void setupMock() throws Exception {
        // This is needed because openConnection is a static method
        mockStatic(DefaultGiteaConnection.class);

        // Set matcher for the argument of openConnection when to mock
        // We can't use this inside the when clause because the method gets called once with this argument causing a exception
        // Therefore call it here to inform the mockito framework but pass some valid arguments to the method to be mocked
        ArgumentMatchers.notNull();

        // Create mock
        when(mockedDefaultGiteaConnection, method(DefaultGiteaConnection.class, "openConnection", UriTemplate.class))
                // Pass valid argument needed to setup mock (the original method gets called once)
                .withArguments(UriTemplate.buildFromTemplate("http://127.0.0.1").build())
                // If this method gets called again it will throw a GiteaHttpStatusException
                // simulating the response of the API if issues or PRs are disabled
                .thenThrow(new GiteaHttpStatusException(404, "TEST CASE FAILED!"));

        giteaRepository = new GiteaRepository(
                new GiteaOwner("", "", "", ""),
                null, "", "", "",
                true, false, false, false,
                "", "", "", "",
                0L, 0L, 0L, 0L, "",
                null
        );
    }

    @Test
    public void fetchPullRequests() throws Exception {
        List<GiteaPullRequest> pr = mockedDefaultGiteaConnection.fetchPullRequests("", "", Collections.singleton(GiteaIssueState.OPEN));
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
        pr = mockedDefaultGiteaConnection.fetchPullRequests("", "", Collections.singleton(GiteaIssueState.CLOSED));
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
    }

    @Test
    public void testFetchPullRequests() throws IOException, InterruptedException {
        List<GiteaPullRequest> pr = mockedDefaultGiteaConnection.fetchPullRequests("", "");
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
    }

    @Test
    public void testFetchPullRequests1() throws IOException, InterruptedException {
        List<GiteaPullRequest> pr = mockedDefaultGiteaConnection.fetchPullRequests(giteaRepository);
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
    }

    @Test
    public void testFetchPullRequests2() throws IOException, InterruptedException {
        List<GiteaPullRequest> pr = mockedDefaultGiteaConnection.fetchPullRequests(giteaRepository, Collections.singleton(GiteaIssueState.OPEN));
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
        pr = mockedDefaultGiteaConnection.fetchPullRequests(giteaRepository, Collections.singleton(GiteaIssueState.CLOSED));
        assertNotNull(pr);
        assertTrue(pr.isEmpty());
    }

    @Test
    public void fetchIssues() throws IOException, InterruptedException {
        List<GiteaIssue> issues = mockedDefaultGiteaConnection.fetchIssues("", "", Collections.singleton(GiteaIssueState.OPEN));
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
        issues = mockedDefaultGiteaConnection.fetchIssues("", "", Collections.singleton(GiteaIssueState.CLOSED));
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testFetchIssues() throws IOException, InterruptedException {
        List<GiteaIssue> issues = mockedDefaultGiteaConnection.fetchIssues(giteaRepository, Collections.singleton(GiteaIssueState.OPEN));
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
        issues = mockedDefaultGiteaConnection.fetchIssues(giteaRepository, Collections.singleton(GiteaIssueState.CLOSED));
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testFetchIssues1() throws IOException, InterruptedException {
        List<GiteaIssue> issues = mockedDefaultGiteaConnection.fetchIssues(giteaRepository);
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testFetchIssues2() throws IOException, InterruptedException {
        List<GiteaIssue> issues = mockedDefaultGiteaConnection.fetchIssues("", "");
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }
}