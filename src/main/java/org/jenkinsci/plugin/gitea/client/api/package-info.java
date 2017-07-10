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
/**
 * Client API for Gitea.
 * Ideally this would be a separate non-Jenkins plugin dependency. This client is designed to not require any
 * Jenkins classes. (Note there is one Jenkins specific detail around classloaders in
 * {@link org.jenkinsci.plugin.gitea.client.api.Gitea#jenkinsPluginClassLoader()}).
 *
 * <h2>Usage</h2>
 * The entry point to this API is the {@link org.jenkinsci.plugin.gitea.client.api.Gitea} class. Use this class to
 * establish a {@link org.jenkinsci.plugin.gitea.client.api.GiteaConnection} to the server, e.g.
 * <pre>
 * try (GiteaConnection c = Gitea.server(...).as(...).open()) {
 *     // do stuff with the connection
 * }
 * </pre>
 */
package org.jenkinsci.plugin.gitea.client.api;
