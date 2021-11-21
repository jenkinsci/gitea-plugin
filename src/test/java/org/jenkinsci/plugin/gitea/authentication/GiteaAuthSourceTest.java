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
package org.jenkinsci.plugin.gitea.authentication;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import hudson.util.Secret;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuthToken;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuthUser;
import org.jenkinsci.plugin.gitea.credentials.PersonalAccessToken;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GiteaAuthSourceTest {
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Test
    public void given__tokenCredential__when__convert__then__tokenAuth() throws Exception {
        // we use a mock to ensure that java.lang.reflect.Proxy implementations of the credential interface work
        PersonalAccessToken credential = Mockito.mock(PersonalAccessToken.class);
        Mockito.when(credential.getToken()).thenReturn(Secret.fromString("b5bc10f13665362bd61de931c731e3c74187acc4"));
        GiteaAuth auth = AuthenticationTokens.convert(GiteaAuth.class, credential);
        assertThat(auth, instanceOf(GiteaAuthToken.class));
        assertThat(((GiteaAuthToken)auth).getToken(), is("b5bc10f13665362bd61de931c731e3c74187acc4"));
    }

    @Test
    public void given__userPassCredential__when__convert__then__tokenAuth() throws Exception {
        // we use a mock to ensure that java.lang.reflect.Proxy implementations of the credential interface work
        UsernamePasswordCredentials credential = Mockito.mock(UsernamePasswordCredentials.class);
        Mockito.when(credential.getUsername()).thenReturn("bob");
        Mockito.when(credential.getPassword()).thenReturn(Secret.fromString("secret"));
        GiteaAuth auth = AuthenticationTokens.convert(GiteaAuth.class, credential);
        assertThat(auth, instanceOf(GiteaAuthUser.class));
        assertThat(((GiteaAuthUser)auth).getUsername(), is("bob"));
        assertThat(((GiteaAuthUser)auth).getPassword(), is("secret"));
    }

}
