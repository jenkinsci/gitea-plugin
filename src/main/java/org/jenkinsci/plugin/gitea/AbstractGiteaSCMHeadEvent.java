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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.scm.SCM;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugin.gitea.client.api.GiteaEvent;

/**
 * Base class for {@link SCMHeadEvent} from Gitea.
 *
 * @param <E> the {@link GiteaEvent}.
 */
public abstract class AbstractGiteaSCMHeadEvent<E extends GiteaEvent> extends SCMHeadEvent<E> {
    /**
     * Constructor.
     *
     * @param type    the type of event.
     * @param payload the payload.
     * @param origin  the origin.
     */
    public AbstractGiteaSCMHeadEvent(@NonNull Type type, @NonNull E payload, @CheckForNull String origin) {
        super(type, payload, origin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMatch(@NonNull SCMNavigator navigator) {
        if (navigator instanceof GiteaSCMNavigator) {
            // check the owner, we don't care about the event if the owner isn't a match
            if (!getPayload().getRepository().getOwner().getUsername()
                    .equalsIgnoreCase(((GiteaSCMNavigator) navigator).getRepoOwner())) {
                return false;
            }

            try {
                URI serverUri = new URI(((GiteaSCMNavigator) navigator).getServerUrl());
                URI eventUri = new URI(getPayload().getRepository().getHtmlUrl());
                if (!serverUri.getHost().equalsIgnoreCase(eventUri.getHost())) {
                    return false;
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
                            return false;
                        }
                    } else if (!"https".equals(eventUri.getScheme())) {
                        return false;
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
                            return false;
                        }
                    } else if (!"http".equals(eventUri.getScheme())) {
                        return false;
                    }
                }
                String serverPath = StringUtils.defaultIfBlank(serverUri.getPath(), "");
                String eventPath = StringUtils.defaultIfBlank(eventUri.getPath(), "/");
                return eventPath.startsWith(serverPath + "/");
            } catch (URISyntaxException e) {
                // ignore
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getSourceName() {
        return getPayload().getRepository().getName();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Map<SCMHead, SCMRevision> heads(@NonNull SCMSource source) {
        if (source instanceof GiteaSCMSource) {
            // check the owner, we don't care about the event if the owner isn't a match
            if (!getPayload().getRepository().getOwner().getUsername()
                    .equalsIgnoreCase(((GiteaSCMSource) source).getRepoOwner())) {
                return Collections.emptyMap();
            }
            // check the repository, we don't care about the event if the repository isn't a match
            if (!getPayload().getRepository().getName()
                    .equalsIgnoreCase(((GiteaSCMSource) source).getRepository())) {
                return Collections.emptyMap();
            }

            try {
                URI serverUri = new URI(((GiteaSCMSource) source).getServerUrl());
                URI eventUri = new URI(getPayload().getRepository().getHtmlUrl());
                if (!serverUri.getHost().equalsIgnoreCase(eventUri.getHost())) {
                    return Collections.emptyMap();
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
                            return Collections.emptyMap();
                        }
                    } else if (!"https".equals(eventUri.getScheme())) {
                        return Collections.emptyMap();
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
                            return Collections.emptyMap();
                        }
                    } else if (!"http".equals(eventUri.getScheme())) {
                        // may be the same just over plain
                        return Collections.emptyMap();
                    }
                }
                String serverPath = StringUtils.defaultIfBlank(serverUri.getPath(), "");
                String eventPath = StringUtils.defaultIfBlank(eventUri.getPath(), "/");
                if (!eventPath.startsWith(serverPath + "/")) {
                    return Collections.emptyMap();
                }
                return headsFor((GiteaSCMSource) source);
            } catch (URISyntaxException e) {
                // ignore
            }
        }
        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMatch(@NonNull SCM scm) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    protected abstract Map<SCMHead, SCMRevision> headsFor(GiteaSCMSource source);

}
