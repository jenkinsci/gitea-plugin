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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ReleaseDiscoveryTrait extends SCMSourceTrait {
    private boolean includeDrafts;
    private boolean includePreReleases;
    private boolean artifactToAssetMappingEnabled;

    @DataBoundConstructor
    public ReleaseDiscoveryTrait(boolean includeDrafts, boolean includePreReleases) {
        this.includeDrafts = includeDrafts;
        this.includePreReleases = includePreReleases;
    }

    public boolean getIncludeDrafts() {
        return includeDrafts;
    }

    @DataBoundSetter
    public final void setIncludeDrafts(boolean includeDrafts) {
        this.includeDrafts = includeDrafts;
    }

    public boolean getIncludePreReleases() {
        return includePreReleases;
    }

    @DataBoundSetter
    public final void setIncludePreReleases(boolean includePreReleases) {
        this.includePreReleases = includePreReleases;
    }

    public boolean getArtifactToAssetMappingEnabled() {
        return artifactToAssetMappingEnabled;
    }

    @DataBoundSetter
    public final void setArtifactToAssetMappingEnabled(boolean artifactToAssetMappingEnabled) {
        this.artifactToAssetMappingEnabled = artifactToAssetMappingEnabled;
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        GiteaSCMSourceContext ctx = (GiteaSCMSourceContext) context;
        ctx.wantReleases(true);
        ctx.includeDraftReleases(this.includeDrafts);
        ctx.includePreReleases(this.includePreReleases);
        ctx.withArtifactToAssetMappingEnabled(this.artifactToAssetMappingEnabled);
        //ctx.withAuthority(new TagSCMHeadAuthority());
        // TODO: implement ReleaseSCMHeadAuthority
    }

    @Override
    public boolean includeCategory(@NonNull SCMHeadCategory category) {
        return category instanceof ReleaseSCMHeadCategory;
    }

    @Extension
    @Discovery
    @Symbol("giteaReleaseDiscovery")
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.ReleaseDiscoveryTrait_displayName();
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
    }
}
