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
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import org.jenkinsci.plugin.gitea.servers.GiteaServers;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A {@link SCMSourceTrait} for {@link GiteaSCMSource} that overrides the {@link GiteaServers}
 * settings for webhook registration.
 */
public class WebhookRegistrationTrait extends SCMSourceTrait {

    /**
     * The mode of registration to apply.
     */
    @NonNull
    private final WebhookRegistration mode;

    /**
     * Constructor.
     *
     * @param mode the mode of registration to apply.
     */
    @DataBoundConstructor
    public WebhookRegistrationTrait(@NonNull String mode) {
        this(WebhookRegistration.valueOf(mode));
    }

    /**
     * Constructor.
     *
     * @param mode the mode of registration to apply.
     */
    public WebhookRegistrationTrait(@NonNull WebhookRegistration mode) {
        this.mode = mode;
    }

    /**
     * Gets the mode of registration to apply.
     *
     * @return the mode of registration to apply.
     */
    @NonNull
    public final WebhookRegistration getMode() {
        return mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        ((GiteaSCMSourceContext) context).webhookRegistration(getMode());
    }

    /**
     * Our constructor.
     */
    @Extension
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.WebhookRegistrationTrait_displayName();
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
         * Form completion.
         *
         * @return the mode options.
         */
        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // stapler form binding
        public ListBoxModel doFillModeItems() {
            ListBoxModel result = new ListBoxModel();
            result.add(Messages.WebhookRegistrationTrait_disableHook(), WebhookRegistration.DISABLE.toString());
            result.add(Messages.WebhookRegistrationTrait_useItemHook(), WebhookRegistration.ITEM.toString());
            return result;
        }

    }
}
