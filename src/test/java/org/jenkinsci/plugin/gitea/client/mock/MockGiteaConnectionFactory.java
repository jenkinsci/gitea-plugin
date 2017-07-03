package org.jenkinsci.plugin.gitea.client.mock;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.HashMap;
import java.util.Map;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnectionBuilder;
import org.jenkinsci.plugin.gitea.client.spi.GiteaConnectionFactory;

public class MockGiteaConnectionFactory extends GiteaConnectionFactory {
    private static final Map<String, GiteaConnection> mocks = new HashMap<>();

    public static void reset() {
        synchronized (mocks) {
            mocks.clear();
        }
    }

    public static <T extends GiteaConnection> T register(@NonNull T mock, @NonNull String serverUrl) {
        mock.getClass(); // NPE if null
        synchronized (mocks) {
            mocks.put(serverUrl, mock);
        }
        return mock;
    }

    @Override
    public long priority(@NonNull GiteaConnectionBuilder builder) {
        return 1000L;
    }

    @Override
    public boolean canOpen(@NonNull GiteaConnectionBuilder builder) {
        synchronized (mocks) {
            return mocks.containsKey(builder.serverUrl());

        }
    }

    @Override
    public GiteaConnection open(@NonNull GiteaConnectionBuilder builder) {
        synchronized (mocks) {
            return mocks.get(builder.serverUrl());
        }
    }
}
