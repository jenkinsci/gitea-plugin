/*
 * The MIT License
 *
 * Copyright (c) 2017-2022, CloudBees, Inc.
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

import hudson.Extension;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMRevision;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaReleaseEvent;
import org.jenkinsci.plugin.gitea.client.api.GiteaTag;

public class GiteaReleaseSCMEvent extends AbstractGiteaSCMHeadEvent<GiteaReleaseEvent> {

    public static final Logger LOGGER = Logger.getLogger(GiteaReleaseSCMEvent.class.getName());

    public GiteaReleaseSCMEvent(GiteaReleaseEvent payload, String origin) {
        super(Type.CREATED, payload, origin);
    }

    @Override
    protected Map<SCMHead, SCMRevision> headsFor(GiteaSCMSource source) {
        if (getPayload().getRelease().isDraft()) {
            // skip draft releases
            return Collections.<SCMHead, SCMRevision>emptyMap();
        }

        String ref = getPayload().getRelease().getTagName();
        String sha = null;

        try (GiteaConnection c = source.gitea().open()) {
            GiteaTag releaseTag = c.fetchTag(source.getRepoOwner(), source.getRepository(), ref);
            sha = releaseTag.getCommit().getSha();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        ReleaseSCMHead h = new ReleaseSCMHead(ref, getPayload().getRelease().getId());
        return Collections.<SCMHead, SCMRevision>singletonMap(h, new ReleaseSCMRevision(h, sha));
    }

    @Extension
    public static class HandlerImpl extends GiteaWebhookHandler<GiteaReleaseSCMEvent, GiteaReleaseEvent> {

        @Override
        protected GiteaReleaseSCMEvent createEvent(GiteaReleaseEvent payload, String origin) {
            return new GiteaReleaseSCMEvent(payload, origin);
        }

        @Override
        protected void process(GiteaReleaseSCMEvent event) {
            SCMHeadEvent.fireNow(event);
        }
    }
}
