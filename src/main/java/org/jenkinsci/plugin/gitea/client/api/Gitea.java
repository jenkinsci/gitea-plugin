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
package org.jenkinsci.plugin.gitea.client.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.ServiceLoader;
import jenkins.model.Jenkins;
import org.jenkinsci.plugin.gitea.client.spi.GiteaConnectionFactory;

/**
 * Entry point to the Gitea client API for opening a {@link GiteaConnection}.
 */
public final class Gitea {

    /**
     * The {@link JsonIgnoreProperties#ignoreUnknown()} to use. (For production use this should always be {@code true},
     * but during testing of API changes it can be changed to {@code false} so that the API can be verified as
     * correctly implemented.
     */
    public static final boolean IGNORE_UNKNOWN_PROPERTIES = true;

    /**
     * The URL of the Gitea server.
     */
    @NonNull
    private final String serverUrl;

    /**
     * The {@link ClassLoader} to resolve {@link GiteaConnectionFactory} implementations from.
     */
    @CheckForNull
    private ClassLoader classLoader;

    /**
     * The authentication to connect with.
     */
    @NonNull
    private GiteaAuth authentication = new GiteaAuthNone();

    /**
     * Private constructor.
     *
     * @param serverUrl the URL of the Gitea server.
     */
    private Gitea(@NonNull String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Creates a new {@link Gitea}.
     *
     * @param serverUrl URL of the Gitea server.
     * @return the {@link Gitea}.
     */
    @NonNull
    public static Gitea server(@NonNull String serverUrl) {
        return new Gitea(serverUrl).jenkinsPluginClassLoader();
    }

    /**
     * Specify the authentication to connect to the server with.
     * @param authentication the authentication.
     * @return {@code this} for method chaining.
     */
    @NonNull
    public Gitea as(@CheckForNull GiteaAuth authentication) {
        this.authentication = authentication == null ? new GiteaAuthNone() : authentication;
        return this;
    }

    @NonNull
    public String serverUrl() {
        return serverUrl;
    }

    @NonNull
    public GiteaAuth as() {
        return authentication;
    }

    @CheckForNull
    public ClassLoader classLoader() {
        return classLoader;
    }

    /**
     * Sets the {@link ClassLoader} that the SPI implementation will be resolved from.
     *
     * @param classLoader the {@link ClassLoader}
     * @return {@code this} for method chaining.
     */
    public Gitea classLoader(@CheckForNull ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * Opens a {@link GiteaConnection} to the Gitea server.
     * @return the connection.
     * @throws IOException if the connection could not be established.
     * @throws InterruptedException if interrupted while opening the connection.
     */
    @NonNull
    public GiteaConnection open() throws IOException, InterruptedException {
        ServiceLoader<GiteaConnectionFactory> loader = ServiceLoader.load(GiteaConnectionFactory.class, classLoader);
        long priority = 0L;
        GiteaConnectionFactory best = null;
        for (GiteaConnectionFactory factory : loader) {
            if (factory.canOpen(this)) {
                long p = factory.priority(this);
                if (best == null || p > priority) {
                    best = factory;
                    priority = p;
                }
            }
        }
        if (best != null) {
            return best.open(this);
        }
        throw new IOException("No implementation for connecting to " + serverUrl);
    }

    public Gitea jenkinsPluginClassLoader() {
        // HACK for Jenkins
        // by rights this should be the context classloader, but Jenkins does not expose plugins on that
        // so we need instead to use the uberClassLoader as that will have the implementations
        Jenkins instance = Jenkins.getInstanceOrNull();
        classLoader = instance == null ? getClass().getClassLoader() : instance.getPluginManager().uberClassLoader;
        // END HACK
        return this;
    }
}
