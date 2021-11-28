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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an individual commit detail.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
public final class GiteaCommitDetail extends GiteaObject<GiteaCommitDetail> implements Cloneable {
    private GiteaUser author;
    private GitCommit commit;
    private GiteaUser committer;
    private String htmlUrl;
    private List<GiteaCommitHash> parents;
    private String sha;
    private String url;

    public GiteaCommitDetail() {
    }

    public GiteaUser getAuthor() {
        return author == null ? null : author.clone();
    }

    public void setAuthor(GiteaUser author) {
        this.author = author == null ? null : author.clone();
    }

    public GitCommit getCommit() {
        return commit == null ? null : commit.clone();
    }

    public void setCommit(GitCommit commit) {
        this.commit = commit == null ? null : commit.clone();
    }

    public GiteaUser getCommitter() {
        return committer == null ? null : committer.clone();
    }

    public void setCommitter(GiteaUser committer) {
        this.committer = committer == null ? null : committer.clone();
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    @JsonProperty("html_url")
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public List<GiteaCommitHash> getParents() {
        return parents == null ? null : parents.stream().map(GiteaCommitHash::clone).collect(Collectors.toList());
    }

    public void setParents(List<GiteaCommitHash> parents) {
        this.parents = parents == null ? null : parents.stream().map(GiteaCommitHash::clone).collect(Collectors.toList());
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GiteaCommitDetail{"
                + "author=" + author
                + ", commit=" + commit
                + ", committer=" + committer
                + ", htmlUrl='" + htmlUrl + '\''
                + ", parents=" + parents
                + ", sha='" + sha + '\''
                + ", url='" + url + '\''
                + '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class GitCommit extends GiteaObject<GitCommit> implements Cloneable {
        private GitActor author;
        private GitActor committer;
        private String message;
        private GiteaCommitHash tree;
        private String url;

        public GitCommit() {
        }

        public GitCommit(GitActor author, GitActor committer, String message,
                         GiteaCommitHash tree, String url) {
            this.author = author;
            this.committer = committer;
            this.message = message;
            this.tree = tree;
            this.url = url;
        }

        public GitActor getAuthor() {
            return author;
        }

        public void setAuthor(GitActor author) {
            this.author = author;
        }

        public GitActor getCommitter() {
            return committer;
        }

        public void setCommitter(GitActor committer) {
            this.committer = committer;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public GiteaCommitHash getTree() {
            return tree;
        }

        public void setTree(GiteaCommitHash tree) {
            this.tree = tree;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "GitCommit{"
                    + "author=" + author
                    + ", committer=" + committer
                    + ", message='" + message + '\''
                    + ", tree=" + tree
                    + ", url='" + url + '\''
                    + '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class GitActor extends GiteaObject<GitActor> implements Cloneable {
        private Date date;
        private String name;
        private String email;

        public GitActor() {
        }

        public GitActor(Date date, String name, String email) {
            this.date = date == null ? null : (Date) date.clone();
            this.name = name;
            this.email = email;
        }

        public Date getDate() {
            return date == null ? null : (Date) date.clone();
        }

        public void setDate(Date date) {
            this.date = date == null ? null : (Date) date.clone();
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

        @Override
        public String toString() {
            return "GitActor{"
                    + "date=" + date
                    + ", name='" + name + '\''
                    + ", email='" + email + '\''
                    + '}';
        }
    }
}
