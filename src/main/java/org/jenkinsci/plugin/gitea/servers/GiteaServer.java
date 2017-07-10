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
package org.jenkinsci.plugin.gitea.servers;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.google.common.net.InternetDomainName;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.annotation.Nonnull;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.Gitea;
import org.jenkinsci.plugin.gitea.client.api.GiteaUser;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Represents a Gitea Server instance.
 */
public class GiteaServer extends AbstractDescribableImpl<GiteaServer> {

    /**
     * Common prefixes that we should remove when inferring a display name.
     */
    private static final String[] COMMON_PREFIX_HOSTNAMES = {
            "git.",
            "gitea.",
            "gogs.",
            "vcs.",
            "scm.",
            "source."
    };

    /**
     * Optional name to use to describe the end-point.
     */
    @CheckForNull
    private final String displayName;

    /**
     * The URL of this Bitbucket Server.
     */
    @NonNull
    private final String serverUrl;
    /**
     * {@code true} if and only if Jenkins is supposed to auto-manage hooks for this end-point.
     */
    private final boolean manageHooks;
    /**
     * The {@link StandardUsernamePasswordCredentials#getId()} of the credentials to use for auto-management of hooks.
     */
    @CheckForNull
    private final String credentialsId;

    /**
     * @param displayName   Optional name to use to describe the end-point.
     * @param serverUrl     The URL of this Bitbucket Server
     * @param manageHooks   {@code true} if and only if Jenkins is supposed to auto-manage hooks for this end-point.
     * @param credentialsId The {@link StandardUsernamePasswordCredentials#getId()} of the credentials to use for
     *                      auto-management of hooks.
     */
    @DataBoundConstructor
    public GiteaServer(@CheckForNull String displayName, @NonNull String serverUrl, boolean manageHooks,
                       @CheckForNull String credentialsId) {
        this.manageHooks = manageHooks && StringUtils.isNotBlank(credentialsId);
        this.credentialsId = manageHooks ? credentialsId : null;
        this.serverUrl = GiteaServers.normalizeServerUrl(serverUrl);
        if (displayName == null || StringUtils.isBlank(displayName)) {
            // try to infer the display name
            String hostName = null;
            try {
                hostName = inferDisplayName(serverUrl);
            } catch (LinkageError e) {
                // guava changed their @Beta API that we have compiled against
            }
            this.displayName = hostName;
        } else {
            this.displayName = displayName;
        }
    }

    /**
     * Makes best effort to guess a "sensible" display name from the hostname in the server URL.
     *
     * @param serverUrl the server URL.
     * @return the display name or {@code null}
     * @throws LinkageError if Guava changes their API that we have depended on.
     */
    @CheckForNull
    /*package*/ static String inferDisplayName(@NonNull String serverUrl) throws LinkageError {
        String hostName;
        try {
            URI serverUri = new URI(serverUrl);
            hostName = serverUri.getHost();
            if (hostName != null) {
                // let's see if we can make this more "friendly"
                InternetDomainName host = InternetDomainName.from(hostName);
                if (host.hasPublicSuffix()) {
                    String publicName = host.publicSuffix().name();
                    hostName = StringUtils.removeEnd(StringUtils.removeEnd(host.name(), publicName), ".")
                            .toLowerCase(Locale.ENGLISH);
                } else {
                    hostName = StringUtils.removeEnd(host.name(), ".").toLowerCase(Locale.ENGLISH);
                }
                for (String prefix : COMMON_PREFIX_HOSTNAMES) {
                    if (hostName.startsWith(prefix)) {
                        hostName = hostName.substring(prefix.length());
                        break;
                    }
                }
            }
        } catch (URISyntaxException e) {
            // ignore, best effort
            hostName = null;
        }
        return hostName;
    }

    /**
     * {@inheritDoc}
     */
    @CheckForNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Returns {@code true} if and only if Jenkins is supposed to auto-manage hooks for this end-point.
     *
     * @return {@code true} if and only if Jenkins is supposed to auto-manage hooks for this end-point.
     */
    public final boolean isManageHooks() {
        return manageHooks;
    }

    /**
     * Returns the {@link StandardUsernamePasswordCredentials#getId()} of the credentials to use for auto-management
     * of hooks.
     *
     * @return the {@link StandardUsernamePasswordCredentials#getId()} of the credentials to use for auto-management
     * of hooks.
     */
    @CheckForNull
    public final String getCredentialsId() {
        return credentialsId;
    }

    /**
     * Looks up the {@link StandardCredentials} to use for auto-management of hooks.
     *
     * @return the credentials or {@code null}.
     */
    @CheckForNull
    public StandardCredentials credentials() {
        return StringUtils.isBlank(credentialsId) ? null : CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StandardCredentials.class,
                        Jenkins.getActiveInstance(),
                        ACL.SYSTEM,
                        URIRequirementBuilder.fromUri(serverUrl).build()
                ),
                CredentialsMatchers.allOf(
                        AuthenticationTokens.matcher(GiteaAuth.class),
                        CredentialsMatchers.withId(credentialsId)
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Our descriptor.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<GiteaServer> {
        /**
         * Checks that the supplied URL is valid.
         *
         * @param value the URL to check.
         * @return the validation results.
         */
        public static FormValidation doCheckServerUrl(@QueryParameter String value) {
            Jenkins.getActiveInstance().checkPermission(Jenkins.ADMINISTER);
            try (GiteaConnection c = Gitea.server(GiteaServers.normalizeServerUrl(value)).open()) {
                return FormValidation
                        .okWithMarkup(Messages.GiteaServer_serverVersion(Util.escape(c.fetchVersion().getVersion())));
            } catch (MalformedURLException e) {
                return FormValidation.errorWithMarkup(Messages.GiteaServer_invalidUrl(Util.escape(e.getMessage())));
            } catch (InterruptedException e) {
                return FormValidation.warning(Messages.GiteaServer_versionInterrupted());
            } catch (IOException e) {
                return FormValidation
                        .errorWithMarkup(Messages.GiteaServer_cannotConnect(Util.escape(e.getMessage())));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.GiteaServer_displayName();
        }

        /**
         * Stapler form completion.
         *
         * @param serverUrl the server URL.
         * @return the available credentials.
         */
        @Restricted(NoExternalUse.class) // stapler
        @SuppressWarnings("unused")
        public ListBoxModel doFillCredentialsIdItems(@QueryParameter String serverUrl) {
            Jenkins.getActiveInstance().checkPermission(Jenkins.ADMINISTER);
            StandardListBoxModel result = new StandardListBoxModel();
            serverUrl = GiteaServers.normalizeServerUrl(serverUrl);
            result.includeMatchingAs(
                    ACL.SYSTEM,
                    Jenkins.getActiveInstance(),
                    StandardCredentials.class,
                    URIRequirementBuilder.fromUri(serverUrl).build(),
                    AuthenticationTokens.matcher(GiteaAuth.class)
            );
            return result;
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String serverUrl, @QueryParameter String value) {
            Jenkins.getActiveInstance().checkPermission(Jenkins.ADMINISTER);
            serverUrl = GiteaServers.normalizeServerUrl(serverUrl);
            StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            StandardCredentials.class,
                            Jenkins.getActiveInstance(),
                            ACL.SYSTEM,
                            URIRequirementBuilder.fromUri(serverUrl).build()
                    ),
                    CredentialsMatchers.allOf(
                            AuthenticationTokens.matcher(GiteaAuth.class),
                            CredentialsMatchers.withId(value)
                    )
            );
            if (credentials == null) {
                return FormValidation.errorWithMarkup(Messages.GiteaServer_credentialsNotResolved(Util.escape(value)));
            }
            try (GiteaConnection c = Gitea.server(serverUrl)
                    .authentication(AuthenticationTokens.convert(GiteaAuth.class, credentials))
                    .open()) {
                GiteaUser user = c.fetchCurrentUser();
                return FormValidation.okWithMarkup(
                        Messages.GiteaServer_hookManagementAs(Util.escape(user.getUsername()))
                );
            } catch (InterruptedException e) {
                return FormValidation.warning(Messages.GiteaServer_validateInterrupted());
            } catch (IOException e) {
                return FormValidation.errorWithMarkup(Messages.GiteaServer_cannotConnect(Util.escape(e.getMessage())));
            }
        }
    }
}
