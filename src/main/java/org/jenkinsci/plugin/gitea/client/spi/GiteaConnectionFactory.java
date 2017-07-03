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
package org.jenkinsci.plugin.gitea.client.spi;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.ServiceLoader;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnectionBuilder;

/**
 * The SPI for instantiating {@link GiteaConnection} implementations from {@link GiteaConnectionBuilder}.
 * All the {@link ServiceLoader} registered implementations will be filtered to only those that
 * {@link #canOpen(GiteaConnectionBuilder)}. In the event of multiple implementations, the one with the highest
 * {@link #priority(GiteaConnectionBuilder)} will be selected and then {@link #open(GiteaConnectionBuilder)} will be
 * called.
 */
public abstract class GiteaConnectionFactory {
    /**
     * SPI: confirm that this factory can open connections to the supplied builder.
     *
     * @param builder the builder.
     * @return {@code true} if connections can be opened.
     */
    public abstract boolean canOpen(@NonNull GiteaConnectionBuilder builder);

    /**
     * SPI: return the priority with which this factory claims ownership of the supplied URL and authentication.
     * This method's return value is only valid after {@link #canOpen(GiteaConnectionBuilder)} has returned {@code true}.
     *
     * @param builder the builder.
     * @return the priority.
     */
    public long priority(@NonNull GiteaConnectionBuilder builder) {
        return 0L;
    }

    /**
     * SPI: open the connection to the supplied URL with the supplied authentication.
     *
     * @param builder the builder.
     * @return the connection.
     * @throws IOException if the connection could not be opened.
     */
    @NonNull
    public abstract GiteaConnection open(@NonNull GiteaConnectionBuilder builder)
            throws IOException;
}
