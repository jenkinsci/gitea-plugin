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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
public final class GiteaPullRequest extends GiteaObject<GiteaPullRequest> {
    private long id;
    private String url;
    private long number;
    private GiteaOwner user;
    private String title;
    private String body;
    private List<GiteaLabel> labels = new ArrayList<>();
    private GiteaMilestone milestone;
    private GiteaOwner assignee;
    private GiteaIssueState state;
    private long comments;
    private String htmlUrl;
    private String diffUrl;
    private String patchUrl;
    private boolean mergeable;
    private boolean merged;
    private Date mergedAt;
    private String mergeCommitSha;
    private GiteaOwner mergedBy;
    private Reference base;
    private Reference head;
    private String mergeBase;
    private Date createdAt;
    private Date updatedAt;

    public GiteaPullRequest() {
    }

    public GiteaPullRequest(String url, GiteaOwner user, String title, String body,
                            List<GiteaLabel> labels, GiteaMilestone milestone,
                            GiteaOwner assignee, GiteaIssueState state, long comments, String htmlUrl,
                            String diffUrl, String patchUrl, boolean mergeable, boolean merged, Date mergedAt,
                            String mergeCommitSha, GiteaOwner mergedBy,
                            Reference base, Reference head, String mergeBase) {
        this.url = url;
        this.user = user;
        this.title = title;
        this.body = body;
        this.labels = labels;
        this.milestone = milestone;
        this.assignee = assignee;
        this.state = state;
        this.comments = comments;
        this.htmlUrl = htmlUrl;
        this.diffUrl = diffUrl;
        this.patchUrl = patchUrl;
        this.mergeable = mergeable;
        this.merged = merged;
        this.mergedAt = mergedAt == null ? null : (Date) mergedAt.clone();
        this.mergeCommitSha = mergeCommitSha;
        this.mergedBy = mergedBy;
        this.base = base;
        this.head = head;
        this.mergeBase = mergeBase;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public GiteaOwner getUser() {
        return user == null ? null : user.clone();
    }

    public void setUser(GiteaOwner user) {
        this.user = user == null ? null : user.clone();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<GiteaLabel> getLabels() {
        return labels == null ? new ArrayList<GiteaLabel>() : new ArrayList<GiteaLabel>(labels);
    }

    public void setLabels(List<GiteaLabel> labels) {
        this.labels = labels == null ? new ArrayList<GiteaLabel>() : new ArrayList<GiteaLabel>(labels);
    }

    public GiteaMilestone getMilestone() {
        return milestone == null ? null : milestone.clone();
    }

    public void setMilestone(GiteaMilestone milestone) {
        this.milestone = milestone == null ? null : milestone.clone();
    }

    public GiteaOwner getAssignee() {
        return assignee == null ? null : assignee.clone();
    }

    public void setAssignee(GiteaOwner assignee) {
        this.assignee = assignee == null ? null : assignee.clone();
    }

    public GiteaIssueState getState() {
        return state;
    }

    public void setState(GiteaIssueState state) {
        this.state = state;
    }

    public long getComments() {
        return comments;
    }

    public void setComments(long comments) {
        this.comments = comments;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    @JsonProperty("html_url")
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getDiffUrl() {
        return diffUrl;
    }

    @JsonProperty("diff_url")
    public void setDiffUrl(String diffUrl) {
        this.diffUrl = diffUrl;
    }

    public String getPatchUrl() {
        return patchUrl;
    }

    @JsonProperty("patch_url")
    public void setPatchUrl(String patchUrl) {
        this.patchUrl = patchUrl;
    }

    public boolean isMergeable() {
        return mergeable;
    }

    public void setMergeable(boolean mergeable) {
        this.mergeable = mergeable;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public Date getMergedAt() {
        return mergedAt == null ? null : (Date) mergedAt.clone();
    }

    @JsonProperty("merged_at")
    public void setMergedAt(Date mergedAt) {
        this.mergedAt = mergedAt == null ? null : (Date) mergedAt.clone();
    }

    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    @JsonProperty("merge_commit_sha")
    public void setMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    public GiteaOwner getMergedBy() {
        return mergedBy;
    }

    @JsonProperty("merged_by")
    public void setMergedBy(GiteaOwner mergedBy) {
        this.mergedBy = mergedBy;
    }

    public Reference getBase() {
        return base;
    }

    public void setBase(Reference base) {
        this.base = base;
    }

    public Reference getHead() {
        return head;
    }

    public void setHead(Reference head) {
        this.head = head;
    }

    public String getMergeBase() {
        return mergeBase;
    }

    @JsonProperty("merge_base")
    public void setMergeBase(String mergeBase) {
        this.mergeBase = mergeBase;
    }

    public Date getCreatedAt() {
        return createdAt == null ? null : (Date) createdAt.clone();
    }

    @JsonProperty("created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt == null ? null : (Date) createdAt.clone();
    }

    public Date getUpdatedAt() {
        return updatedAt == null ? null : (Date) updatedAt.clone();
    }

    @JsonProperty("updated_at")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt == null ? null : (Date) updatedAt.clone();
    }

    @Override
    public String toString() {
        return "GiteaPullRequest{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", number=" + number +
                ", user=" + user +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", labels=" + labels +
                ", milestone=" + milestone +
                ", assignee=" + assignee +
                ", state='" + state + '\'' +
                ", comments=" + comments +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", diffUrl='" + diffUrl + '\'' +
                ", patchUrl='" + patchUrl + '\'' +
                ", mergeable=" + mergeable +
                ", merged=" + merged +
                ", mergedAt=" + mergedAt +
                ", mergeCommitSha='" + mergeCommitSha + '\'' +
                ", mergedBy=" + mergedBy +
                ", base=" + base +
                ", head=" + head +
                ", mergeBase='" + mergeBase + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class Reference extends GiteaObject<Reference> {
        private String label;
        private String ref;
        private String sha;
        private long repoId;
        private GiteaRepository repo;

        public Reference() {
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getSha() {
            return sha;
        }

        public void setSha(String sha) {
            this.sha = sha;
        }

        public long getRepoId() {
            return repoId;
        }

        @JsonProperty("repo_id")
        public void setRepoId(long repoId) {
            this.repoId = repoId;
        }

        public GiteaRepository getRepo() {
            return repo == null ? null : repo.clone();
        }

        public void setRepo(GiteaRepository repo) {
            this.repo = repo == null ? null : repo.clone();
        }

        @Override
        public String toString() {
            return "Reference{" +
                    "label='" + label + '\'' +
                    ", ref='" + ref + '\'' +
                    ", sha='" + sha + '\'' +
                    ", repoId=" + repoId +
                    ", repo=" + repo +
                    '}';
        }
    }


}
