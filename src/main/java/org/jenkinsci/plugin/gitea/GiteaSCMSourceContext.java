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
import hudson.model.TaskListener;
import java.util.EnumSet;
import java.util.Set;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMSourceContext;

public class GiteaSCMSourceContext
        extends SCMSourceContext<GiteaSCMSourceContext, GiteaSCMSourceRequest> {
    private boolean wantBranches;
    private boolean wantTags;
    private boolean wantOriginPRs;
    private boolean wantForkPRs;
    @NonNull
    private Set<ChangeRequestCheckoutStrategy> originPRStrategies = EnumSet.noneOf(ChangeRequestCheckoutStrategy.class);
    @NonNull
    private Set<ChangeRequestCheckoutStrategy> forkPRStrategies = EnumSet.noneOf(ChangeRequestCheckoutStrategy.class);
    @NonNull
    private WebhookRegistration webhookRegistration = WebhookRegistration.SYSTEM;
    private boolean notificationsDisabled;

    public GiteaSCMSourceContext(@CheckForNull SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer) {
        super(criteria, observer);
    }

    public final boolean wantBranches() {
        return wantBranches;
    }

    public final boolean wantTags() {
        return wantTags;
    }

    public final boolean wantPRs() {
        return wantOriginPRs || wantForkPRs;
    }

    public final boolean wantOriginPRs() {
        return wantOriginPRs;
    }

    public final boolean wantForkPRs() {
        return wantForkPRs;
    }

    @NonNull
    public final Set<ChangeRequestCheckoutStrategy> originPRStrategies() {
        return originPRStrategies;
    }

    @NonNull
    public final Set<ChangeRequestCheckoutStrategy> forkPRStrategies() {
        return forkPRStrategies;
    }

    @NonNull
    public final WebhookRegistration webhookRegistration() {
        return webhookRegistration;
    }

    public final boolean notificationsDisabled() {
        return notificationsDisabled;
    }

    @NonNull
    public GiteaSCMSourceContext wantBranches(boolean include) {
        wantBranches = wantBranches || include;
        return this;
    }

    @NonNull
    public GiteaSCMSourceContext wantTags(boolean include) {
        wantTags = wantTags || include;
        return this;
    }

    @NonNull
    public GiteaSCMSourceContext wantOriginPRs(boolean include) {
        wantOriginPRs = wantOriginPRs || include;
        return this;
    }

    @NonNull
    public GiteaSCMSourceContext wantForkPRs(boolean include) {
        wantForkPRs = wantForkPRs || include;
        return this;
    }

    @NonNull
    public GiteaSCMSourceContext withOriginPRStrategies(Set<ChangeRequestCheckoutStrategy> strategies) {
        originPRStrategies.addAll(strategies);
        return this;
    }

    @NonNull
    public GiteaSCMSourceContext withForkPRStrategies(Set<ChangeRequestCheckoutStrategy> strategies) {
        forkPRStrategies.addAll(strategies);
        return this;
    }

    @NonNull
    public final GiteaSCMSourceContext webhookRegistration(WebhookRegistration mode) {
        webhookRegistration = mode;
        return this;
    }

    @NonNull
    public final GiteaSCMSourceContext withNotificationsDisabled(boolean disabled) {
        this.notificationsDisabled = disabled;
        return this;
    }

    @NonNull
    @Override
    public GiteaSCMSourceRequest newRequest(@NonNull SCMSource source, @CheckForNull TaskListener listener) {
        return new GiteaSCMSourceRequest(source, this, listener);
    }
}
