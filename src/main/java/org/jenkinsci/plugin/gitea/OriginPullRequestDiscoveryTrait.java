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
import hudson.util.ListBoxModel;
import java.util.EnumSet;
import java.util.Set;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.mixin.ChangeRequestSCMHead2;
import jenkins.scm.api.trait.SCMHeadAuthority;
import jenkins.scm.api.trait.SCMHeadAuthorityDescriptor;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link Discovery} trait for Gitea that will discover pull requests originating from a branch in the repository
 * itself.
 */
public class OriginPullRequestDiscoveryTrait extends SCMSourceTrait {
    /**
     * The strategy encoded as a bit-field.
     */
    private final int strategyId;

    /**
     * Constructor for stapler.
     *
     * @param strategyId the strategy id.
     */
    @DataBoundConstructor
    public OriginPullRequestDiscoveryTrait(int strategyId) {
        this.strategyId = strategyId;
    }

    /**
     * Constructor for programmatic instantiation.
     *
     * @param strategies the {@link ChangeRequestCheckoutStrategy} instances.
     */
    public OriginPullRequestDiscoveryTrait(Set<ChangeRequestCheckoutStrategy> strategies) {
        this((strategies.contains(ChangeRequestCheckoutStrategy.MERGE) ? 1 : 0)
                + (strategies.contains(ChangeRequestCheckoutStrategy.HEAD) ? 2 : 0));
    }

    /**
     * Gets the strategy id.
     *
     * @return the strategy id.
     */
    public int getStrategyId() {
        return strategyId;
    }

    /**
     * Returns the strategies.
     *
     * @return the strategies.
     */
    @NonNull
    public Set<ChangeRequestCheckoutStrategy> getStrategies() {
        switch (strategyId) {
            case 1:
                return EnumSet.of(ChangeRequestCheckoutStrategy.MERGE);
            case 2:
                return EnumSet.of(ChangeRequestCheckoutStrategy.HEAD);
            case 3:
                return EnumSet.of(ChangeRequestCheckoutStrategy.HEAD, ChangeRequestCheckoutStrategy.MERGE);
            default:
                return EnumSet.noneOf(ChangeRequestCheckoutStrategy.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        GiteaSCMSourceContext ctx = (GiteaSCMSourceContext) context;
        ctx.wantOriginPRs(true);
        ctx.withAuthority(new OriginChangeRequestSCMHeadAuthority());
        ctx.withOriginPRStrategies(getStrategies());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean includeCategory(@NonNull SCMHeadCategory category) {
        return category instanceof ChangeRequestSCMHeadCategory;
    }

    @Symbol("giteaPullREquestDiscovery") // why the gitea prefix? because symbol support doesn't understand types!
    @Extension
    @Discovery
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Discover pull requests from origin";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends SCMSourceContext> getContextClass() {
            return GiteaSCMSourceContext.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<? extends SCMSource> getSourceClass() {
            return GiteaSCMSource.class;
        }

        /**
         * Populates the strategy options.
         *
         * @return the stategy options.
         */
        @NonNull
        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // stapler
        public ListBoxModel doFillStrategyIdItems() {
            ListBoxModel result = new ListBoxModel();
            result.add(Messages.ForkPullRequestDiscoveryTrait_mergeOnly(), "1");
            result.add(Messages.ForkPullRequestDiscoveryTrait_headOnly(), "2");
            result.add(Messages.ForkPullRequestDiscoveryTrait_headAndMerge(), "3");
            return result;
        }
    }

    /**
     * A {@link SCMHeadAuthority} that trusts origin pull requests
     */
    public static class OriginChangeRequestSCMHeadAuthority
            extends SCMHeadAuthority<SCMSourceRequest, ChangeRequestSCMHead2, SCMRevision> {
        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean checkTrusted(@NonNull SCMSourceRequest request, @NonNull ChangeRequestSCMHead2 head) {
            return SCMHeadOrigin.DEFAULT.equals(head.getOrigin());
        }

        /**
         * Our descriptor.
         */
        @Extension
        public static class DescriptorImpl extends SCMHeadAuthorityDescriptor {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isApplicableToOrigin(@NonNull Class<? extends SCMHeadOrigin> originClass) {
                return SCMHeadOrigin.Default.class.isAssignableFrom(originClass);
            }            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return Messages.OriginPullRequestDiscoveryTrait_authorityDisplayName();
            }


        }
    }
}
