package org.jenkinsci.plugin.gitea.client.api;

import org.jenkinsci.plugin.gitea.client.mock.MockGiteaConnection;
import org.jenkinsci.plugin.gitea.client.mock.MockGiteaConnectionFactory;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class GiteaConnectionBuilderTest {
    @Test
    public void given__registered_mock__when__open__then__mock_returned() throws Exception {
        MockGiteaConnectionFactory.reset();
        MockGiteaConnectionFactory.register(new MockGiteaConnection("bob"), "http://gitea.test/open");
        assertThat(Gitea.server("http://gitea.test/open").open(), instanceOf(MockGiteaConnection.class));
    }

    @Test
    public void given__no_registered_mock__when__open__then__real_returned() throws Exception {
        MockGiteaConnectionFactory.reset();
        assertThat(Gitea.server("http://gitea.test/open").open(), not(instanceOf(MockGiteaConnection.class)));
    }

}
