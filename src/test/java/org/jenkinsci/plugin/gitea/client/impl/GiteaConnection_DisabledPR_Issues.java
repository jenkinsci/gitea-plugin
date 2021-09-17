package org.jenkinsci.plugin.gitea.client.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaHttpStatusException;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GiteaConnection_DisabledPR_Issues extends DefaultGiteaConnection {
    GiteaConnection_DisabledPR_Issues(@NonNull String serverUrl, @NonNull GiteaAuth authentication) {
        super(serverUrl, authentication);
    }

    @Override
    protected HttpURLConnection openConnection(String spec) throws IOException {
        throw new GiteaHttpStatusException(404, "TEST Case");
    }
}
