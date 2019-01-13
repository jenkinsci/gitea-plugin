package org.jenkinsci.plugin.gitea.authentication;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuthToken;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

@Extension
public class GiteaAuthStringCredential extends AuthenticationTokenSource<GiteaAuthToken, StringCredentials> {
    /**
     * Constructor.
     */
    public GiteaAuthStringCredential() {
        super(GiteaAuthToken.class, StringCredentials.class);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public GiteaAuthToken convert(@NonNull StringCredentials credential) throws AuthenticationTokenException {
        return new GiteaAuthToken(credential.getSecret().getPlainText());
    }
}
