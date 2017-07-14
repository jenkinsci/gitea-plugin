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
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMHeadAuthority;
import jenkins.scm.api.trait.SCMHeadAuthorityDescriptor;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.plugin.gitea.client.api.GiteaPullRequest;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link Discovery} trait for Gitea that will discover branches on the repository.
 */
public class BranchDiscoveryTrait extends SCMSourceTrait {
    /**
     * The strategy encoded as a bit-field.
     */
    private int strategyId;

    /**
     * Constructor for stapler.
     *
     * @param strategyId the strategy id.
     */
    @DataBoundConstructor
    public BranchDiscoveryTrait(int strategyId) {
        this.strategyId = strategyId;
    }

    /**
     * Constructor for legacy code.
     *
     * @param buildBranch       build branches that are not filed as a PR.
     * @param buildBranchWithPr build branches that are also PRs.
     */
    public BranchDiscoveryTrait(boolean buildBranch, boolean buildBranchWithPr) {
        this.strategyId = (buildBranch ? 1 : 0) + (buildBranchWithPr ? 2 : 0);
    }

    /**
     * Returns the strategy id.
     *
     * @return the strategy id.
     */
    public int getStrategyId() {
        return strategyId;
    }

    /**
     * Returns {@code true} if building branches that are not filed as a PR.
     *
     * @return {@code true} if building branches that are not filed as a PR.
     */
    @Restricted(NoExternalUse.class)
    public boolean isBuildBranch() {
        return (strategyId & 1) != 0;

    }

    /**
     * Returns {@code true} if building branches that are filed as a PR.
     *
     * @return {@code true} if building branches that are filed as a PR.
     */
    @Restricted(NoExternalUse.class)
    public boolean isBuildBranchesWithPR() {
        return (strategyId & 2) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        GiteaSCMSourceContext ctx = (GiteaSCMSourceContext) context;
        ctx.wantBranches(true);
        ctx.withAuthority(new BranchSCMHeadAuthority());
        switch (strategyId) {
            case 1:
                ctx.wantOriginPRs(true);
                ctx.withFilter(new ExcludeOriginPRBranchesSCMHeadFilter());
                break;
            case 2:
                ctx.wantOriginPRs(true);
                ctx.withFilter(new OnlyOriginPRBranchesSCMHeadFilter());
                break;
            case 3:
            default:
                // we don't care if it is a PR or not, we're taking them all, no need to ask for PRs and no need
                // to filter
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean includeCategory(@NonNull SCMHeadCategory category) {
        return category.isUncategorized();
    }

    /**
     * Our descriptor.
     */
    @Extension
    @Discovery
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.BranchDiscoveryTrait_displayName();
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
            result.add(Messages.BranchDiscoveryTrait_excludePRs(), "1");
            result.add(Messages.BranchDiscoveryTrait_onlyPRs(), "2");
            result.add(Messages.BranchDiscoveryTrait_allBranches(), "3");
            return result;
        }
    }

    /**
     * Trusts branches from the origin repository.
     */
    public static class BranchSCMHeadAuthority extends SCMHeadAuthority<SCMSourceRequest, BranchSCMHead, SCMRevision> {
        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean checkTrusted(@NonNull SCMSourceRequest request, @NonNull BranchSCMHead head) {
            return true;
        }

        /**
         * Out descriptor.
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
                return Messages.BranchDiscoveryTrait_authorityDisplayName();
            }


        }
    }

    /**
     * Filter that excludes branches that are also filed as a pull request.
     */
    public static class ExcludeOriginPRBranchesSCMHeadFilter extends SCMHeadFilter {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest request, @NonNull SCMHead head) {
            if (head instanceof BranchSCMHead && request instanceof GiteaSCMSourceRequest) {
                for (GiteaPullRequest p : ((GiteaSCMSourceRequest) request).getPullRequests()) {
                    // only match if the pull request is an origin pull request
                    if (p.getBase().getRepo().getOwner().getUsername()
                            .equalsIgnoreCase(p.getHead().getRepo().getOwner().getUsername())
                            && p.getBase().getRepo().getName().equalsIgnoreCase(p.getHead().getRepo().getName())
                            && p.getHead().getRef().equals(head.getName())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Filter that excludes branches that are not also filed as a pull request.
     */
    public static class OnlyOriginPRBranchesSCMHeadFilter extends SCMHeadFilter {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isExcluded(@NonNull SCMSourceRequest request, @NonNull SCMHead head) {
            if (head instanceof BranchSCMHead && request instanceof GiteaSCMSourceRequest) {
                for (GiteaPullRequest p : ((GiteaSCMSourceRequest) request).getPullRequests()) {
                    if (p.getBase().getRepo().getOwner().getUsername()
                            .equalsIgnoreCase(p.getHead().getRepo().getOwner().getUsername())
                            && p.getBase().getRepo().getName().equalsIgnoreCase(p.getHead().getRepo().getName())
                            && !p.getHead().getRef().equals(head.getName())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }
}
