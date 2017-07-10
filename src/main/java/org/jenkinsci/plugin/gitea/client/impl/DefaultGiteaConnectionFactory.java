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
package org.jenkinsci.plugin.gitea.client.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.Gitea;
import org.jenkinsci.plugin.gitea.client.spi.GiteaConnectionFactory;

/**
 * The SPI implementation of {@link GiteaConnectionFactory}.
 */
public class DefaultGiteaConnectionFactory extends GiteaConnectionFactory {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canOpen(@NonNull Gitea gitea) {
        return gitea.serverUrl().startsWith("http://") || gitea.serverUrl().startsWith("https://");
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public GiteaConnection open(@NonNull Gitea gitea) throws IOException {
        return new DefaultGiteaConnection(gitea.serverUrl(), gitea.as());
    }
}
