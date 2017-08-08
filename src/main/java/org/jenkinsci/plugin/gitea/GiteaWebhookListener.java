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
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Item;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.git.extensions.impl.IgnoreNotifyCommit;
import hudson.scm.SCM;
import hudson.triggers.SCMTrigger;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.scm.api.SCM2;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.triggers.SCMTriggerItem;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.Gitea;
import org.jenkinsci.plugin.gitea.client.api.GiteaEventType;
import org.jenkinsci.plugin.gitea.client.api.GiteaHook;
import org.jenkinsci.plugin.gitea.client.api.GiteaHookType;
import org.jenkinsci.plugin.gitea.client.api.GiteaOrganization;
import org.jenkinsci.plugin.gitea.client.api.GiteaPayloadType;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.jenkinsci.plugin.gitea.client.api.GiteaUser;
import org.jenkinsci.plugin.gitea.servers.GiteaServer;
import org.jenkinsci.plugin.gitea.servers.GiteaServers;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;

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

    private static GiteaConnection connect(String serverUrl, StandardCredentials credentials)
            throws IOException, InterruptedException {
        return Gitea.server(serverUrl)
                .as(AuthenticationTokens.convert(GiteaAuth.class, credentials))
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

    public static void register(SCMTriggerItem item, GitSCM scm) {
        JenkinsLocationConfiguration locationConfiguration = JenkinsLocationConfiguration.get();
        if (locationConfiguration == null) {
            // Jenkins URL not configured, can't register hooks
            return;
        }
        String rootUrl = locationConfiguration.getUrl();
        if (StringUtils.isBlank(rootUrl) || rootUrl.startsWith("http://localhost:")) {
            // Jenkins URL not configured, can't register hooks
            return;
        }
        if (scm.getExtensions().get(IgnoreNotifyCommit.class) != null) {
            // GitSCM special case to ignore commits, so no point registering hooks
            return;
        }
        List<GiteaServer> serverList = new ArrayList<>(GiteaServers.get().getServers());
        if (serverList.isEmpty()) {
            // Not configured to register hooks, so no point registering.
            return;
        }
        // track attempts to register in case there are multiple remotes for the same repo
        Set<String> registered = new HashSet<>();
        String hookUrl =
                UriTemplate.buildFromTemplate(rootUrl).literal("gitea-webhook").literal("/post").build().expand();
        for (RemoteConfig repository : scm.getRepositories()) {
            REMOTES:
            for (URIish remoteURL : repository.getURIs()) {
                for (Iterator<GiteaServer> iterator = serverList.iterator(); iterator.hasNext(); ) {
                    GiteaServer server = iterator.next();
                    if (!server.isManageHooks()) {
                        // not allowed to manage hooks for this server, don't check it against any other remotes
                        iterator.remove();
                        continue;
                    }
                    URIish serverURL;
                    try {
                        serverURL = new URIish(server.getServerUrl());
                    } catch (URISyntaxException e) {
                        continue;
                    }
                    if (!StringUtils.equals(serverURL.getHost(), remoteURL.getHost())) {
                        // different hosts, so none of our business
                        continue;
                    }
                    if (serverURL.getPort() != -1 && remoteURL.getPort() != -1
                            && serverURL.getPort() != remoteURL.getPort()) {
                        // different explicit ports, so none of our business
                        continue;
                    }
                    String serverPath = serverURL.getPath();
                    if (!serverPath.startsWith("/")) {
                        serverPath = "/" + serverPath;
                    }
                    if (!serverPath.endsWith("/")) {
                        serverPath = serverPath + "/";
                    }
                    String remotePath = remoteURL.getPath();
                    if (!remotePath.startsWith("/")) {
                        remotePath = "/" + remotePath;
                    }
                    if (!remotePath.startsWith(serverPath)) {
                        // different context path, so none of our business
                        continue;
                    }
                    remotePath = remotePath.substring(serverPath.length());
                    int index = remotePath.indexOf('/');
                    if (index == -1) {
                        // not matching expected structure of repoOwner/repository[.git]
                        continue REMOTES;
                    }
                    String repoOwner = remotePath.substring(0, index);
                    String repoName = StringUtils.removeEnd(remotePath.substring(index + 1), ".git");
                    String registeredKey = server.getServerUrl() + "::" + repoOwner + "::" + repoName;
                    if (registered.contains(registeredKey)) {
                        // have already tried to register this repo during this method, so don't repeat
                        continue REMOTES;
                    }
                    registered.add(registeredKey);
                    try (GiteaConnection c = connect(server.getServerUrl(), server.credentials())) {
                        GiteaRepository repo = c.fetchRepository(repoOwner, repoName);
                        if (repo == null) {
                            continue REMOTES;
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
                                "Could not manage repository hooks for " + repoOwner + "/" + repoName
                                        + " on " + server.getServerUrl(), e);
                    }
                }
                if (serverList.isEmpty()) {
                    // none of the servers are allowed manage hooks, no point checking the remaining remotes
                    return;
                }
            }
        }
    }

    // TODO remove class once GitSCM implements jenkins.scm.SCM2
    @Restricted(DoNotUse.class)
    @Extension
    public static class GitSCMOnSaveNotifier extends SaveableListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onChange(Saveable o, XmlFile file) {
            if (!(o instanceof Item)) {
                // must be an Item
                return;
            }
            SCMTriggerItem item = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem((Item) o);
            if (item == null) {
                // more specifically must be an SCMTriggerItem
                return;
            }
            SCMTrigger trigger = item.getSCMTrigger();
            if (trigger == null || trigger.isIgnorePostCommitHooks()) {
                // must have the trigger enabled and not opted out of post commit hooks
                return;
            }
            for (SCM scm : item.getSCMs()) {
                if (scm instanceof GitSCM) {
                    // we have a winner
                    GiteaWebhookListener.register(item, (GitSCM) scm);
                }
            }
        }
    }

}
