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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 * Represents a commit status.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = GiteaObject.IGNORE_UNKNOWN_PROPERTIES)
public final class GiteaCommitStatus extends GiteaObject<GiteaCommitStatus> implements Cloneable {
    private long id;
    private String url;
    private String context;
    private String description;
    private String targetUrl;
    /**
     * The state of the commit. NOTE: that Gitea's API is inconsistent in its JSON data model, some requests / responses
     * use this as {@code state} and others use this as {@code status}
     */
    private GiteaCommitState state;
    private GiteaUser creator;
    private Date createdAt;
    private Date updatedAt;

    public GiteaCommitStatus() {
    }

    public GiteaUser getCreator() {
        return creator == null ? null : creator.clone();
    }

    public void setCreator(GiteaUser creator) {
        this.creator = creator == null ? null : creator.clone();
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    @JsonProperty("target_url")
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public GiteaCommitState getState() {
        return state;
    }

    public void setState(GiteaCommitState state) {
        this.state = state;
    }

    public GiteaCommitState getStatus() {
        return state;
    }

    public void setStatus(GiteaCommitState status) {
        this.state = status;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GiteaCommitStatus{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", context='" + context + '\'' +
                ", description='" + description + '\'' +
                ", targetUrl='" + targetUrl + '\'' +
                ", state=" + state +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
