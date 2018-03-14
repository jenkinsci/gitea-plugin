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
import java.util.Collections;
import java.util.Map;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugin.gitea.client.api.GiteaEvent;
import org.jenkinsci.plugin.gitea.servers.GiteaServers;

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
            GiteaSCMNavigator nav = (GiteaSCMNavigator) navigator;
            return StringUtils.equalsIgnoreCase(getPayload().getRepository().getOwner().getUsername(), nav.getRepoOwner())
                    && GiteaServers.isEventFor(nav.getServerUrl(), getPayload().getRepository().getHtmlUrl());

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
            GiteaSCMSource src = (GiteaSCMSource) source;
            return StringUtils.equalsIgnoreCase(getPayload().getRepository().getOwner().getUsername(), src.getRepoOwner())
                    && StringUtils.equalsIgnoreCase(getPayload().getRepository().getName(), src.getRepository())
                    && GiteaServers.isEventFor(src.getServerUrl(), getPayload().getRepository().getHtmlUrl())
                    ? headsFor(src)
                    : Collections.<SCMHead, SCMRevision>emptyMap();
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
