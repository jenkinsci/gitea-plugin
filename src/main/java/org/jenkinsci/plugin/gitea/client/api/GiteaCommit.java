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
import java.util.Date;

/**
 * Represents a commit.
 */
@JsonIgnoreProperties(
        value = {"added", "removed", "modified", "verification"},
        ignoreUnknown = GiteaObject.IGNORE_UNKNOWN_PROPERTIES
)
public final class GiteaCommit extends GiteaObject<GiteaCommit> {
    /**
     * The SHA1 of the commit.
     */
    private String id;
    private String message;
    private String url;
    private Actor author;
    private Actor committer;
    private Date timestamp;

    public GiteaCommit() {
    }

    public GiteaCommit(String id, String message, String url,
                       Actor author, Actor committer, Date timestamp) {
        this.id = id;
        this.message = message;
        this.url = url;
        this.author = author;
        this.committer = committer;
        this.timestamp = timestamp == null ? null : (Date) timestamp.clone();
    }

    /**
     * Gets the SHA1 of the commit.
     *
     * @return the SHA1 of the commit.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the SHA1 of the commit.
     *
     * @param id the SHA1 of the commit.
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Actor getAuthor() {
        return author == null ? null : author.clone();
    }

    public void setAuthor(Actor author) {
        this.author = author == null ? null : author.clone();
    }

    public Actor getCommitter() {
        return committer == null ? null : committer.clone();
    }

    public void setCommitter(Actor committer) {
        this.committer = committer == null ? null : committer.clone();
    }

    public Date getTimestamp() {
        return timestamp == null ? null : (Date) timestamp.clone();
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp == null ? null : (Date) timestamp.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GiteaCommit{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", url='" + url + '\'' +
                ", author=" + author +
                ", committer=" + committer +
                ", timestamp=" + timestamp +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = GiteaObject.IGNORE_UNKNOWN_PROPERTIES)
    public static class Actor extends GiteaObject<Actor> implements Cloneable {
        private String name;
        private String email;
        private String username;

        public Actor() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Actor{" +
                    "name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }


}
