/*
 * The MIT License
 *
 * Copyright (c) 2020, CloudBees, Inc.
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

/**
 * Represents a Gitea Tag.
 */
@JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
public final class GiteaTag extends GiteaObject<GiteaTag> {
    private String name;
    private String id;
    private GiteaCommitHash commit;

    public GiteaTag() {
    }

    public GiteaTag(String name, String id, GiteaCommitHash commit) {
        this.name = name;
        this.commit = commit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GiteaCommitHash getCommit() {
        return commit == null ? null : commit.clone();
    }

    public void setCommit(GiteaCommitHash commit) {
        this.commit = commit == null ? null : commit.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GiteaTag{"
                + "name='" + name + '\''
                + ", id='" + getId() + '\''
                + ", commit=" + commit
                + '}';
    }

}
