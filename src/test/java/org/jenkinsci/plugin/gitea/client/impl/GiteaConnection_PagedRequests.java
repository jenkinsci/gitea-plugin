package org.jenkinsci.plugin.gitea.client.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.HttpURLConnection;
import java.util.Map;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;

public class GiteaConnection_PagedRequests extends DefaultGiteaConnection {
    private final Map<String, HttpURLConnection> requestMocks;

    GiteaConnection_PagedRequests(@NonNull String serverUrl, @NonNull GiteaAuth authentication, Map<String, HttpURLConnection> requestMocks) {
        super(serverUrl, authentication);
        this.requestMocks = requestMocks;
    }

    @Override
    protected HttpURLConnection openConnection(String spec) {
        return requestMocks.get(spec);
    }
}
