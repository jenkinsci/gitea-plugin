package org.jenkinsci.plugin.gitea;

import hudson.model.TaskListener;
import java.io.IOException;
import java.util.List;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMSourceObserver;
import org.jenkinsci.plugin.gitea.client.api.GiteaHttpStatusException;
import org.jenkinsci.plugin.gitea.client.api.GiteaOrganization;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.jenkinsci.plugin.gitea.client.mock.MockGiteaConnection;
import org.jenkinsci.plugin.gitea.client.mock.MockGiteaConnectionFactory;
import org.jenkinsci.plugin.gitea.servers.GiteaServer;
import org.jenkinsci.plugin.gitea.servers.GiteaServers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mockito;

@WithJenkins
class GiteaSCMNavigatorTest {

    private static final String SERVER_URL = "http://gitea.test/navigator";

    private final GiteaSCMNavigator navigator = new GiteaSCMNavigator(SERVER_URL, "acme");
    private SCMSourceObserver observer;

    @BeforeEach
    void setUp(JenkinsRule j) throws IOException {
        MockGiteaConnectionFactory.reset();
        GiteaServers.get().addServer(new GiteaServer("test", SERVER_URL, false, null));
        navigator.setCredentialsId("gitea");
        OrganizationFolder folder = j.jenkins.createProject(OrganizationFolder.class, "acme");
        observer = Mockito.mock(SCMSourceObserver.class, Mockito.RETURNS_MOCKS);
        Mockito.when(observer.getContext()).thenReturn(folder);
        Mockito.when(observer.getListener()).thenReturn(TaskListener.NULL);
        Mockito.when(observer.getIncludes()).thenReturn(null);
        Mockito.when(observer.isObserving()).thenReturn(true);
    }

    @Test
    void given__no_filter__when__visiting__then__all_repositories_are_observed() throws Exception {
        MockGiteaConnectionFactory.register(withRepositories(new MockGiteaConnection("bob")), SERVER_URL);
        navigator.visitSources(observer);
        Mockito.verify(observer).observe("one");
        Mockito.verify(observer).observe("two");
    }

    @Test
    void given__filter_on_one_repository__when__visiting__then__only_that_repository_is_fetched() throws Exception {
        MockGiteaConnectionFactory.register(withRepositories(new MockGiteaConnection("bob") {
            @Override
            public List<GiteaRepository> fetchRepositories(String username) {
                throw new AssertionError("listed the repositories of " + username);
            }
        }), SERVER_URL);
        navigator.visitSources(SCMSourceObserver.filter(observer, "two"));
        Mockito.verify(observer).observe("two");
    }

    @Test
    void given__filter_on_unknown_repository__when__visiting__then__nothing_is_observed() throws Exception {
        MockGiteaConnectionFactory.register(withRepositories(new MockGiteaConnection("bob") {
            @Override
            public GiteaRepository fetchRepository(String username, String name) throws IOException {
                throw new GiteaHttpStatusException(404, "Not Found");
            }
        }), SERVER_URL);
        navigator.visitSources(SCMSourceObserver.filter(observer, "missing"));
        Mockito.verify(observer, Mockito.never()).observe(Mockito.anyString());
    }

    private static MockGiteaConnection withRepositories(MockGiteaConnection connection) {
        GiteaOrganization acme = new GiteaOrganization("acme", "Acme", "", "", "", "");
        connection.withOrg(acme);
        for (String name : List.of("one", "two")) {
            GiteaRepository repo = new GiteaRepository();
            repo.setOwner(acme);
            repo.setName(name);
            repo.setFullName("acme/" + name);
            repo.setHtmlUrl(SERVER_URL + "/acme/" + name);
            connection.withRepo(repo);
        }
        return connection;
    }
}
