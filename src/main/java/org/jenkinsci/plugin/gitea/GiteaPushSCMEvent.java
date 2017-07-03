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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import java.util.Collections;
import java.util.Map;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.lib.Constants;
import org.jenkinsci.plugin.gitea.client.api.GiteaPushEvent;

public class GiteaPushSCMEvent extends AbstractGiteaSCMHeadEvent<GiteaPushEvent> {
    public GiteaPushSCMEvent(GiteaPushEvent pushEvent, String origin) {
        super(typeOf(pushEvent), pushEvent, origin);
    }

    private static Type typeOf(GiteaPushEvent pushEvent) {
        if (StringUtils.isBlank(pushEvent.getBefore())
                || "0000000000000000000000000000000000000000".equals(pushEvent.getBefore())) {
            return Type.CREATED;
        }
        if (StringUtils.isBlank(pushEvent.getAfter())
                || "0000000000000000000000000000000000000000".equals(pushEvent.getAfter())) {
            // TODO currently do not receive these ever: https://github.com/go-gitea/gitea/issues/2105
            return Type.REMOVED;
        }
        return Type.UPDATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String descriptionFor(@NonNull SCMNavigator navigator) {
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        return "Push event to branch " + ref + " in repository " + getPayload().getRepository().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String descriptionFor(SCMSource source) {
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        return "Push event to branch " + ref;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        return "Push event to branch " + ref + " in repository " +
                getPayload().getRepository().getOwner().getUsername() + "/" +
                getPayload().getRepository().getName();
    }

    @NonNull
    @Override
    public Map<SCMHead, SCMRevision> headsFor(GiteaSCMSource source) {
        String ref = getPayload().getRef();
        ref = ref.startsWith(Constants.R_HEADS) ? ref.substring(Constants.R_HEADS.length()) : ref;
        BranchSCMHead h = new BranchSCMHead(ref);
        return Collections.<SCMHead, SCMRevision>singletonMap(h,
                StringUtils.isNotBlank(getPayload().getAfter())
                        ? new BranchSCMRevision(h, getPayload().getAfter()) : null);
    }

    @Extension
    public static class HandlerImpl extends GiteaWebhookHandler<GiteaPushSCMEvent, GiteaPushEvent> {

        @Override
        protected GiteaPushSCMEvent createEvent(GiteaPushEvent payload, String origin) {
            return new GiteaPushSCMEvent(payload, origin);
        }

        @Override
        protected void process(GiteaPushSCMEvent event) {

        }
    }
}
