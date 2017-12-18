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
import hudson.Extension;
import jenkins.scm.api.SCMHeadEvent;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceEvent;
import org.jenkinsci.plugin.gitea.client.api.GiteaPushEvent;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepositoryEvent;

/**
 * A {@link SCMHeadEvent} for a {@link GiteaPushEvent}.
 */
public class GiteaRepositorySCMEvent extends AbstractGiteaSCMSourceEvent<GiteaRepositoryEvent> {
    /**
     * Constructor.
     *
     * @param payload the payload.
     * @param origin  the origin.
     */
    public GiteaRepositorySCMEvent(@NonNull GiteaRepositoryEvent payload, @CheckForNull String origin) {
        super(typeOf(payload), payload, origin);
    }

    /**
     * Determines the type of a repository event.
     *
     * @param event the event.
     * @return the type.
     */
    @NonNull
    private static Type typeOf(@NonNull GiteaRepositoryEvent event) {
        switch (event.getAction()) {
            case "deleted":
                return Type.REMOVED;
            case "created":
                return Type.CREATED;
            default:
                return Type.UPDATED;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String descriptionFor(@NonNull SCMNavigator navigator) {
        switch (getType()) {
            case CREATED:
                return "Creation of repository " + getPayload().getRepository().getName();
            case REMOVED:
                return "Deletion of repository " + getPayload().getRepository().getName();
            default:
                return super.descriptionFor(navigator);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String descriptionFor(SCMSource source) {
        switch (getType()) {
            case CREATED:
                return "Creation of repository";
            case REMOVED:
                return "Deletion of repository";
            default:
                return super.descriptionFor(source);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        switch (getType()) {
            case CREATED:
                return "Creation of repository " + getPayload().getRepository().getOwner().getUsername() + "/" +
                        getPayload().getRepository().getName();

            case REMOVED:
                return "Deletion of repository " + getPayload().getRepository().getOwner().getUsername() + "/" +
                        getPayload().getRepository().getName();

            default:
                return super.description();
        }
    }

    /**
     * Our handler.
     */
    @Extension
    public static class HandlerImpl extends GiteaWebhookHandler<GiteaRepositorySCMEvent, GiteaRepositoryEvent> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected GiteaRepositorySCMEvent createEvent(GiteaRepositoryEvent payload, String origin) {
            return new GiteaRepositorySCMEvent(payload, origin);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void process(GiteaRepositorySCMEvent event) {
            SCMSourceEvent.fireNow(event);
        }
    }
}
