package org.jenkinsci.plugin.gitea.authentication;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuthUser;

@Extension
public class GiteaAuthUsernamePasswordCredential extends AuthenticationTokenSource<GiteaAuthUser, UsernamePasswordCredentials> {
    /**
     * Constructor.
     */
    public GiteaAuthUsernamePasswordCredential() {
        super(GiteaAuthUser.class, UsernamePasswordCredentials.class);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public GiteaAuthUser convert(@NonNull UsernamePasswordCredentials credential) throws AuthenticationTokenException {
        return new GiteaAuthUser(credential.getUsername(), credential.getPassword().getPlainText());
    }
}
