package org.jenkinsci.plugin.gitea;

import java.util.concurrent.atomic.AtomicInteger;
import org.jenkinsci.plugin.gitea.client.api.GiteaHttpStatusException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GiteaNotifierTest {

    @Test
    void retriesBadGatewayThenSucceeds() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        long start = System.nanoTime();
        String result = GiteaNotifier.retryOnServerError(() -> {
            if (calls.incrementAndGet() == 1) {
                throw new GiteaHttpStatusException(502, "Bad Gateway");
            }
            return "ok";
        });
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertThat(result, is("ok"));
        assertThat(calls.get(), is(2));
        assertThat("backs off before retrying", elapsedMillis, greaterThanOrEqualTo(1000L));
    }

    @Test
    void stillRetriesInternalServerError() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        String result = GiteaNotifier.retryOnServerError(() -> {
            if (calls.incrementAndGet() == 1) {
                throw new GiteaHttpStatusException(500, "Internal Server Error");
            }
            return "ok";
        });
        assertThat(result, is("ok"));
        assertThat(calls.get(), is(2));
    }

    @Test
    void clientErrorIsThrownWithoutRetry() {
        AtomicInteger calls = new AtomicInteger();
        GiteaHttpStatusException e = assertThrows(GiteaHttpStatusException.class,
                () -> GiteaNotifier.retryOnServerError(() -> {
                    calls.incrementAndGet();
                    throw new GiteaHttpStatusException(404, "Not Found");
                }));
        assertThat(e.getStatusCode(), is(404));
        assertThat(calls.get(), is(1));
    }

    @Test
    void givesUpAfterThreeAttempts() {
        AtomicInteger calls = new AtomicInteger();
        long start = System.nanoTime();
        GiteaHttpStatusException e = assertThrows(GiteaHttpStatusException.class,
                () -> GiteaNotifier.retryOnServerError(() -> {
                    calls.incrementAndGet();
                    throw new GiteaHttpStatusException(503, "Service Unavailable");
                }));
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertThat(e.getStatusCode(), is(503));
        assertThat(calls.get(), is(3));
        assertThat("waits 1s then 2s between attempts", elapsedMillis, greaterThanOrEqualTo(3000L));
    }
}
