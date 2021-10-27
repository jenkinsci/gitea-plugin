package org.jenkinsci.plugin.gitea.client.impl;


import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

public class GiteaConnection_PagedRequests extends DefaultGiteaConnection {
    private Map<String, HttpURLConnection> requestMocks;

    GiteaConnection_PagedRequests(@NonNull String serverUrl, @NonNull GiteaAuth authentication, Map<String, HttpURLConnection> requestMocks) {
        super(serverUrl, authentication);
        this.requestMocks = requestMocks;
    }

    @Override
    protected HttpURLConnection openConnection(String spec) throws IOException {
        return requestMocks.get(spec);
    }
}
