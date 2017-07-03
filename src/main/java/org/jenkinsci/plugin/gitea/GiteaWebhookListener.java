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

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.damnhandy.uri.template.UriTemplate;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSourceOwner;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnectionBuilder;
import org.jenkinsci.plugin.gitea.client.api.GiteaEventType;
import org.jenkinsci.plugin.gitea.client.api.GiteaHook;
import org.jenkinsci.plugin.gitea.client.api.GiteaHookType;
import org.jenkinsci.plugin.gitea.client.api.GiteaOrganization;
import org.jenkinsci.plugin.gitea.client.api.GiteaPayloadType;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.jenkinsci.plugin.gitea.client.api.GiteaUser;
import org.jenkinsci.plugin.gitea.servers.GiteaServer;
import org.jenkinsci.plugin.gitea.servers.GiteaServers;

public class GiteaWebhookListener {
    public static final Logger LOGGER = Logger.getLogger(GiteaWebhookListener.class.getName());

    public static void register(SCMNavigatorOwner owner, GiteaSCMNavigator navigator,
                                WebhookRegistration mode, String credentialsId) {
        StandardCredentials credentials;
        String serverUrl = navigator.getServerUrl();
        switch (mode) {
            case DISABLE:
                return;
            case SYSTEM:
                GiteaServer server = GiteaServers.get().findServer(serverUrl);
                if (server == null || !server.isManageHooks()) {
                    return;
                }
                credentials = server.credentials();
                break;
            case ITEM:
                credentials = navigator.credentials(owner);
                break;
            default:
                return;
        }
        if (credentials == null) {
            return;
        }
        JenkinsLocationConfiguration locationConfiguration = JenkinsLocationConfiguration.get();
        if (locationConfiguration == null) {
            return;
        }
        String rootUrl = locationConfiguration.getUrl();
        if (StringUtils.isBlank(rootUrl) || rootUrl.startsWith("http://localhost:")) {
            return;
        }
        String hookUrl =
                UriTemplate.buildFromTemplate(rootUrl).literal("gitea-webhook").literal("/post").build().expand();
        try (GiteaConnection c = connect(serverUrl, credentials)) {
            GiteaUser user = c.fetchUser(navigator.getRepoOwner());
            if (StringUtils.isNotBlank(user.getEmail())) {
                // it's a user not an org
                return;
            }
            GiteaOrganization org = c.fetchOrganization(navigator.getRepoOwner());
            if (org == null) {
                return;
            }
            List<GiteaHook> hooks = c.fetchHooks(org);
            GiteaHook hook = null;
            for (GiteaHook h : hooks) {
                if (hookUrl.equals(h.getConfig().getUrl())) {
                    if (hook == null
                            && h.getType() == GiteaHookType.GITEA
                            && h.getConfig().getContentType() == GiteaPayloadType.JSON
                            && h.isActive()
                            && EnumSet.allOf(GiteaEventType.class).equals(h.getEvents())) {
                        hook = h;
                    } else {
                        c.deleteHook(org, h);
                    }
                }
            }
            if (hook == null) {
                hook = new GiteaHook();
                GiteaHook.Configuration configuration = new GiteaHook.Configuration();
                configuration.setContentType(GiteaPayloadType.JSON);
                configuration.setUrl(hookUrl);
                hook.setType(GiteaHookType.GITEA);
                hook.setConfig(configuration);
                hook.setEvents(EnumSet.allOf(GiteaEventType.class));
                hook.setActive(true);
                c.createHook(org, hook);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING,
                    "Could not manage organization hooks for " + navigator.getRepoOwner() + " on " + serverUrl, e);
        }
    }

    private static GiteaConnection connect(String serverUrl, StandardCredentials credentials) throws IOException {
        return GiteaConnectionBuilder.newBuilder(serverUrl)
                .authentication(AuthenticationTokens.convert(GiteaAuth.class, credentials))
                .open();
    }

    public static void register(SCMSourceOwner owner, GiteaSCMSource source,
                                WebhookRegistration mode, String credentialsId) {
        StandardCredentials credentials;
        String serverUrl = source.getServerUrl();
        switch (mode) {
            case DISABLE:
                return;
            case SYSTEM:
                GiteaServer server = GiteaServers.get().findServer(serverUrl);
                if (server == null || !server.isManageHooks()) {
                    return;
                }
                credentials = server.credentials();
                break;
            case ITEM:
                credentials = source.credentials();
                break;
            default:
                return;
        }
        if (credentials == null) {
            return;
        }
        JenkinsLocationConfiguration locationConfiguration = JenkinsLocationConfiguration.get();
        if (locationConfiguration == null) {
            return;
        }
        String rootUrl = locationConfiguration.getUrl();
        if (StringUtils.isBlank(rootUrl) || rootUrl.startsWith("http://localhost:")) {
            return;
        }
        String hookUrl =
                UriTemplate.buildFromTemplate(rootUrl).literal("gitea-webhook").literal("/post").build().expand();
        try (GiteaConnection c = connect(serverUrl, credentials)) {
            GiteaRepository repo = c.fetchRepository(source.getRepoOwner(), source.getRepository());
            if (repo == null) {
                return;
            }
            List<GiteaHook> hooks = c.fetchHooks(repo);
            GiteaHook hook = null;
            for (GiteaHook h : hooks) {
                if (hookUrl.equals(h.getConfig().getUrl())) {
                    if (hook == null
                            && h.getType() == GiteaHookType.GITEA
                            && h.getConfig().getContentType() == GiteaPayloadType.JSON
                            && h.isActive()
                            && EnumSet.allOf(GiteaEventType.class).equals(h.getEvents())) {
                        hook = h;
                    } else {
                        c.deleteHook(repo, h);
                    }
                }
            }
            if (hook == null) {
                hook = new GiteaHook();
                GiteaHook.Configuration configuration = new GiteaHook.Configuration();
                configuration.setContentType(GiteaPayloadType.JSON);
                configuration.setUrl(hookUrl);
                hook.setType(GiteaHookType.GITEA);
                hook.setConfig(configuration);
                hook.setEvents(EnumSet.allOf(GiteaEventType.class));
                hook.setActive(true);
                c.createHook(repo, hook);
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING,
                    "Could not manage repository hooks for " + source.getRepoOwner() + "/" + source.getRepository()
                            + " on " + serverUrl, e);
        }

    }
}
