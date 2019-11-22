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
package org.jenkinsci.plugin.gitea.servers;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.util.ListBoxModel;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Represents the global configuration of Gitea servers.
 */
@Extension
public class GiteaServers extends GlobalConfiguration {

    /**
     * The list of {@link GiteaServer}, this is subject to the constraint that there can only ever be
     * one entry for each {@link GiteaServer#getServerUrl()}.
     */
    private List<GiteaServer> servers;

    /**
     * Constructor.
     */
    public GiteaServers() {
        load();
    }

    /**
     * Gets the {@link GiteaServers} singleton.
     *
     * @return the {@link GiteaServers} singleton.
     */
    public static GiteaServers get() {
        return ExtensionList.lookup(GlobalConfiguration.class).get(GiteaServers.class);
    }

    /**
     * Fix a serverUrl.
     *
     * @param serverUrl the server URL.
     * @return the normalized server URL.
     */
    @NonNull
    public static String normalizeServerUrl(@CheckForNull String serverUrl) {
        serverUrl = StringUtils.defaultString(serverUrl);
        try {
            URI uri = new URI(serverUrl).normalize();
            String scheme = uri.getScheme();
            if ("http".equals(scheme) || "https".equals(scheme)) {
                // we only expect http / https, but also these are the only ones where we know the authority
                // is server based, i.e. [userinfo@]server[:port]
                // DNS names must be US-ASCII and are case insensitive, so we force all to lowercase

                String host = uri.getHost() == null ? null : uri.getHost().toLowerCase(Locale.ENGLISH);
                int port = uri.getPort();
                if ("http".equals(scheme) && port == 80) {
                    port = -1;
                } else if ("https".equals(scheme) && port == 443) {
                    port = -1;
                }
                serverUrl = new URI(
                        scheme,
                        uri.getUserInfo(),
                        host,
                        port,
                        uri.getPath(),
                        uri.getQuery(),
                        uri.getFragment()
                ).toASCIIString();
            }
        } catch (URISyntaxException e) {
            // ignore, this was a best effort tidy-up
        }
        return serverUrl.replaceAll("/$", "");
    }

    /**
     * Checks if the supplied event url is for the specified server url (after consulting
     *
     * @param serverUrl the {@link GiteaServer#getServerUrl()}
     * @param eventUrl  the event url.
     * @return {@code true} if the event is a matching
     * {@link GiteaServer#getAliasUrl()} for registered {@link GiteaServer} instances)
     * @since 1.0.5
     */
    public static boolean isEventFor(String serverUrl, String eventUrl) {
        try {
            for (boolean alias : new boolean[]{false, true}) {
                URI serverUri;
                if (alias) {
                    GiteaServer server = GiteaServers.get().findServer(serverUrl);
                    if (server != null && StringUtils.isNotBlank(server.getAliasUrl())) {
                        serverUri = new URI(server.getAliasUrl());
                    } else {
                        continue;
                    }
                } else {
                    serverUri = new URI(serverUrl);
                }
                URI eventUri = new URI(eventUrl);
                if (!StringUtils.equalsIgnoreCase(serverUri.getHost(), eventUri.getHost())) {
                    continue;
                }
                if ("http".equals(serverUri.getScheme())) {
                    int serverPort = serverUri.getPort();
                    if (serverPort == -1) {
                        serverPort = 80;
                    }
                    if ("http".equals(eventUri.getScheme())) {
                        int eventPort = eventUri.getPort();
                        if (eventPort == -1) {
                            eventPort = 80;
                        }
                        if (serverPort != eventPort) {
                            continue;
                        }
                    } else if (!"https".equals(eventUri.getScheme())) {
                        continue;
                    }
                } else if ("https".equals(serverUri.getScheme())) {
                    int serverPort = serverUri.getPort();
                    if (serverPort == -1) {
                        serverPort = 443;
                    }
                    if ("https".equals(eventUri.getScheme())) {
                        int eventPort = eventUri.getPort();
                        if (eventPort == -1) {
                            eventPort = 443;
                        }
                        if (serverPort != eventPort) {
                            continue;
                        }
                    } else if (!"http".equals(eventUri.getScheme())) {
                        // may be the same just over plain
                        continue;
                    }
                }
                String serverPath = StringUtils.defaultIfBlank(serverUri.getPath(), "");
                String eventPath = StringUtils.defaultIfBlank(eventUri.getPath(), "/");
                if (eventPath.startsWith(serverPath + "/")) {
                    return true;
                }
            }
        } catch (URISyntaxException e) {
            return false;
        }
        return false;
    }

    /**
     * Returns {@code true} if and only if there is more than one configured endpoint.
     *
     * @return {@code true} if and only if there is more than one configured endpoint.
     */
    public boolean isEndpointSelectable() {
        return getServers().size() > 1;
    }

    /**
     * Populates a {@link ListBoxModel} with the endpoints.
     *
     * @return A {@link ListBoxModel} with all the endpoints
     */
    public ListBoxModel getServerItems() {
        ListBoxModel result = new ListBoxModel();
        for (GiteaServer endpoint : getServers()) {
            String serverUrl = endpoint.getServerUrl();
            String displayName = endpoint.getDisplayName();
            result.add(StringUtils.isBlank(displayName) ? serverUrl : displayName + " (" + serverUrl + ")", serverUrl);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        return true;
    }

    /**
     * Gets the list of endpoints.
     *
     * @return the list of endpoints
     */
    @NonNull
    public synchronized List<GiteaServer> getServers() {
        return servers == null || servers.isEmpty()
                ? Collections.<GiteaServer>emptyList()
                : Collections.unmodifiableList(servers);
    }

    /**
     * Sets the list of endpoints.
     *
     * @param servers the list of endpoints.
     */
    public synchronized void setServers(@CheckForNull List<? extends GiteaServer> servers) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        List<GiteaServer> eps = new ArrayList<>(Util.fixNull(servers));
        // remove duplicates and empty urls
        Set<String> serverUrls = new HashSet<>();
        for (ListIterator<GiteaServer> iterator = eps.listIterator(); iterator.hasNext(); ) {
            GiteaServer endpoint = iterator.next();
            String serverUrl = endpoint.getServerUrl();
            if (StringUtils.isBlank(serverUrl) || serverUrls.contains(serverUrl)) {
                iterator.remove();
                continue;
            }
            serverUrls.add(serverUrl);
        }
        this.servers = eps;
        save();
    }

    /**
     * Adds an endpoint.
     *
     * @param endpoint the endpoint to add.
     * @return {@code true} if the list of endpoints was modified
     */
    public synchronized boolean addServer(@NonNull GiteaServer endpoint) {
        List<GiteaServer> endpoints = new ArrayList<>(getServers());
        for (GiteaServer ep : endpoints) {
            if (ep.getServerUrl().equals(endpoint.getServerUrl())) {
                return false;
            }
        }
        endpoints.add(endpoint);
        setServers(endpoints);
        return true;
    }

    /**
     * Updates an existing endpoint (or adds if missing).
     *
     * @param endpoint the endpoint to update.
     */
    public synchronized void updateServer(@NonNull GiteaServer endpoint) {
        List<GiteaServer> endpoints = new ArrayList<>(getServers());
        boolean found = false;
        for (int i = 0; i < endpoints.size(); i++) {
            GiteaServer ep = endpoints.get(i);
            if (ep.getServerUrl().equals(endpoint.getServerUrl())) {
                endpoints.set(i, endpoint);
                found = true;
                break;
            }
        }
        if (!found) {
            endpoints.add(endpoint);
        }
        setServers(endpoints);
    }

    /**
     * Removes an endpoint.
     *
     * @param endpoint the endpoint to remove.
     * @return {@code true} if the list of endpoints was modified
     */
    public boolean removeServer(@NonNull GiteaServer endpoint) {
        return removeServer(endpoint.getServerUrl());
    }

    /**
     * Removes an endpoint.
     *
     * @param serverUrl the server URL to remove.
     * @return {@code true} if the list of endpoints was modified
     */
    public synchronized boolean removeServer(@CheckForNull String serverUrl) {
        serverUrl = normalizeServerUrl(serverUrl);
        boolean modified = false;
        List<GiteaServer> endpoints = new ArrayList<>(getServers());
        for (Iterator<GiteaServer> iterator = endpoints.iterator(); iterator.hasNext(); ) {
            if (serverUrl.equals(iterator.next().getServerUrl())) {
                iterator.remove();
                modified = true;
            }
        }
        setServers(endpoints);
        return modified;
    }

    /**
     * Checks to see if the supplied server URL is defined in the global configuration.
     *
     * @param serverUrl the server url to check.
     * @return the global configuration for the specified server url or {@code null} if not defined.
     */
    @CheckForNull
    public synchronized GiteaServer findServer(@CheckForNull String serverUrl) {
        serverUrl = normalizeServerUrl(serverUrl);
        for (GiteaServer endpoint : getServers()) {
            if (serverUrl.equals(endpoint.getServerUrl())) {
                return endpoint;
            }
        }
        return null;
    }

}
