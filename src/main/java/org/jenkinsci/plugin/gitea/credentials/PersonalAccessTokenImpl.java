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
package org.jenkinsci.plugin.gitea.credentials;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Default implementation of {@link PersonalAccessToken} for use by {@link Jenkins} {@link CredentialsProvider}
 * instances that store {@link Secret} locally.
 */
public class PersonalAccessTokenImpl extends BaseStandardCredentials implements StandardUsernameCredentials, PersonalAccessToken, StringCredentials {
    /**
     * Our token.
     */
    @NonNull
    private final Secret token;

    /**
     * Constructor.
     *
     * @param scope       the credentials scope.
     * @param id          the credentials id.
     * @param description the description of the token.
     * @param token       the token itself (will be passed through {@link Secret#fromString(String)})
     */
    @DataBoundConstructor
    public PersonalAccessTokenImpl(@CheckForNull CredentialsScope scope, @CheckForNull String id,
                                   @CheckForNull String description, @NonNull String token) {
        super(scope, id, description);
        this.token = Secret.fromString(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public Secret getToken() {
        return token;
    }


    @Override
    public boolean isUsernameSecret() {
        return true;
    }

    @NonNull
    @Override
    public String getUsername() {
        return getToken().getPlainText();
    }

    @NonNull
    @Override
    public Secret getPassword() {
        return getToken();
    }

    @NonNull
    @Override
    public Secret getSecret() {
        return getToken();
    }

    /**
     * Our descriptor.
     */
    @Symbol("giteaAccessToken")
    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.PersonalAccessTokenImpl_displayName();
        }

        /**
         * Sanity check for a Gitea access token.
         *
         * @param value the token.
         * @return the resulst of the sanity check.
         */
        @Restricted(NoExternalUse.class) // stapler
        @SuppressWarnings("unused") // stapler
        public FormValidation doCheckToken(@QueryParameter String value) {
            if (value == null || value.isEmpty()) {
                return FormValidation.error(Messages.PersonalAccessTokenImpl_tokenRequired());
            }
            Secret secret = Secret.fromString(value);
            if (StringUtils.equals(value, secret.getPlainText())) {
                if (value.length() != 40) {
                    return FormValidation.error(Messages.PersonalAccessTokenImpl_tokenWrongLength());
                }
            } else if (secret.getPlainText().length() != 40) {
                return FormValidation.warning(Messages.PersonalAccessTokenImpl_tokenWrongLength());
            }
            return FormValidation.ok();
        }
    }
}
