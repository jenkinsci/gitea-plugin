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
public final class GiteaIssue extends GiteaObject<GiteaIssue> {
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
    private Date createdAt;
    private Date updatedAt;
    private PullSummary pullRequest;

    public GiteaIssue() {
    }

    public GiteaIssue(String url, GiteaOwner user, String title, String body,
                      List<GiteaLabel> labels, GiteaMilestone milestone,
                      GiteaOwner assignee, GiteaIssueState state, long comments) {
        this.url = url;
        this.user = user;
        this.title = title;
        this.body = body;
        this.labels = labels;
        this.milestone = milestone;
        this.assignee = assignee;
        this.state = state;
        this.comments = comments;
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
        return labels == null ? new ArrayList<GiteaLabel>() : new ArrayList<>(labels);
    }

    public void setLabels(List<GiteaLabel> labels) {
        this.labels = labels == null ? new ArrayList<GiteaLabel>() : new ArrayList<>(labels);
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

    public PullSummary getPullRequest() {
        return pullRequest == null ? null : pullRequest.clone();
    }

    @JsonProperty("pull_request")
    public void setPullRequest(PullSummary pullRequest) {
        this.pullRequest = pullRequest == null ? null : pullRequest.clone();
    }

    @Override
    public String toString() {
        return "GiteaIssue{" +
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
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", pullRequest=" + pullRequest +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class PullSummary implements Cloneable {
        private boolean merged;

        private Date mergedAt;

        public PullSummary() {
        }

        public boolean isMerged() {
            return merged;
        }

        public void setMerged(boolean merged) {
            this.merged = merged;
        }

        @Override
        public PullSummary clone() {
            try {
                return (PullSummary) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }

        public Date getMergedAt() {
            return mergedAt == null ? null : (Date) mergedAt.clone();
        }

        @JsonProperty("merged_at")
        public void setMergedAt(Date mergedAt) {
            this.mergedAt = mergedAt == null ? null : (Date) mergedAt.clone();
        }
        @Override
        public String toString() {
            return "PullSummary{" +
                    "merged=" + merged +
                    ", mergedAt=" + mergedAt +
                    '}';
        }

    }


}
