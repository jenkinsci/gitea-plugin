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
import hudson.Util;
import hudson.util.ListBoxModel;
import java.util.EnumSet;
import java.util.List;
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
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link Discovery} trait for Gitea that will discover pull requests from forks of the repository.
 */
public class ForkPullRequestDiscoveryTrait extends SCMSourceTrait {
    /**
     * The strategy encoded as a bit-field.
     */
    private final int strategyId;
    /**
     * The authority.
     */
    @NonNull
    private final SCMHeadAuthority<? super GiteaSCMSourceRequest, ? extends ChangeRequestSCMHead2, ? extends
            SCMRevision>
            trust;

    /**
     * Constructor for stapler.
     *
     * Note: in order to support the JobDSL plugin we cannot use a complex/generic type for the trust parameter.
     * See: https://issues.jenkins.io/browse/JENKINS-26535
     *
     * @param strategyId the strategy id.
     * @param trust      the authority to use.
     */
    @DataBoundConstructor
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ForkPullRequestDiscoveryTrait(int strategyId,
                                         @NonNull SCMHeadAuthority/*<? super GiteaSCMSourceRequest, ? extends
                                                  ChangeRequestSCMHead2, ? extends SCMRevision>*/ trust) {
        this.strategyId = strategyId;
        this.trust = trust;
    }

    /**
     * Constructor for programmatic instantiation.
     *
     * @param strategies the {@link ChangeRequestCheckoutStrategy} instances.
     * @param trust      the authority.
     */
    public ForkPullRequestDiscoveryTrait(@NonNull Set<ChangeRequestCheckoutStrategy> strategies,
                                         @NonNull SCMHeadAuthority<? super GiteaSCMSourceRequest, ? extends
                                                 ChangeRequestSCMHead2, ? extends SCMRevision> trust) {
        this((strategies.contains(ChangeRequestCheckoutStrategy.MERGE) ? 1 : 0)
                + (strategies.contains(ChangeRequestCheckoutStrategy.HEAD) ? 2 : 0), trust);
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
     * Gets the authority.
     *
     * @return the authority.
     */
    @NonNull
    public SCMHeadAuthority<? super GiteaSCMSourceRequest, ? extends ChangeRequestSCMHead2, ? extends SCMRevision>
    getTrust() {
        return trust;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        GiteaSCMSourceContext ctx = (GiteaSCMSourceContext) context;
        ctx.wantForkPRs(true);
        ctx.withAuthority(trust);
        ctx.withForkPRStrategies(getStrategies());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean includeCategory(@NonNull SCMHeadCategory category) {
        return category instanceof ChangeRequestSCMHeadCategory;
    }

    /**
     * Our descriptor.
     */
    @Symbol("giteaForkDiscovery") // I am a double sad panda with both the forkDiscovery name and the gitea prefix
    @Extension
    @Discovery
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.ForkPullRequestDiscoveryTrait_displayName();
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

        /**
         * Returns the list of appropriate {@link SCMHeadAuthorityDescriptor} instances.
         *
         * @return the list of appropriate {@link SCMHeadAuthorityDescriptor} instances.
         */
        @NonNull
        @SuppressWarnings("unused") // stapler
        public List<SCMHeadAuthorityDescriptor> getTrustDescriptors() {
            return SCMHeadAuthority._for(
                    GiteaSCMSourceRequest.class,
                    PullRequestSCMHead.class,
                    PullRequestSCMRevision.class,
                    SCMHeadOrigin.Fork.class
            );
        }

        /**
         * Returns the default trust for new instances of {@link ForkPullRequestDiscoveryTrait}.
         *
         * @return the default trust for new instances of {@link ForkPullRequestDiscoveryTrait}.
         */
        @NonNull
        @SuppressWarnings("unused") // stapler
        public SCMHeadAuthority<?, ?, ?> getDefaultTrust() {
            return new TrustContributors();
        }
    }


    /**
     * An {@link SCMHeadAuthority} that trusts nothing.
     */
    public static class TrustNobody extends SCMHeadAuthority<SCMSourceRequest, PullRequestSCMHead, PullRequestSCMRevision> {

        /**
         * Constructor.
         */
        @DataBoundConstructor
        public TrustNobody() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean checkTrusted(@NonNull SCMSourceRequest request, @NonNull PullRequestSCMHead head) {
            return false;
        }

        /**
         * Our descriptor.
         */
        @Symbol("giteaTrustNobody") // words fail me that we are reduced to gitea prefix as symbol cannot use type info!
        @Extension
        public static class DescriptorImpl extends SCMHeadAuthorityDescriptor {

            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return Messages.ForkPullRequestDiscoveryTrait_nobodyDisplayName();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isApplicableToOrigin(@NonNull Class<? extends SCMHeadOrigin> originClass) {
                return SCMHeadOrigin.Fork.class.isAssignableFrom(originClass);
            }
        }
    }

    /**
     * An {@link SCMHeadAuthority} that trusts contributors to the repository.
     */
    public static class TrustContributors
            extends SCMHeadAuthority<GiteaSCMSourceRequest, PullRequestSCMHead, PullRequestSCMRevision> {
        /**
         * Constructor.
         */
        @DataBoundConstructor
        public TrustContributors() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean checkTrusted(@NonNull GiteaSCMSourceRequest request, @NonNull PullRequestSCMHead head) {
            return !head.getOrigin().equals(SCMHeadOrigin.DEFAULT)
                    && Util.fixNull(request.getCollaboratorNames()).contains(head.getOriginOwner());
        }

        /**
         * Our descriptor.
         */
        @Symbol("giteaTrustContributors") // sad panda
        @Extension
        public static class DescriptorImpl extends SCMHeadAuthorityDescriptor {

            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return Messages.ForkPullRequestDiscoveryTrait_contributorsDisplayName();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isApplicableToOrigin(@NonNull Class<? extends SCMHeadOrigin> originClass) {
                return SCMHeadOrigin.Fork.class.isAssignableFrom(originClass);
            }

        }
    }

    /**
     * An {@link SCMHeadAuthority} that trusts everyone.
     */
    public static class TrustEveryone extends SCMHeadAuthority<SCMSourceRequest, PullRequestSCMHead, PullRequestSCMRevision> {
        /**
         * Constructor.
         */
        @DataBoundConstructor
        public TrustEveryone() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean checkTrusted(@NonNull SCMSourceRequest request, @NonNull PullRequestSCMHead head) {
            return true;
        }

        /**
         * Our descriptor.
         */
        @Symbol("giteaTrustEveryone")
        @Extension
        public static class DescriptorImpl extends SCMHeadAuthorityDescriptor {

            /**
             * {@inheritDoc}
             */
            @Override
            public String getDisplayName() {
                return Messages.ForkPullRequestDiscoveryTrait_everyoneDisplayName();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isApplicableToOrigin(@NonNull Class<? extends SCMHeadOrigin> originClass) {
                return SCMHeadOrigin.Fork.class.isAssignableFrom(originClass);
            }
        }
    }
}
