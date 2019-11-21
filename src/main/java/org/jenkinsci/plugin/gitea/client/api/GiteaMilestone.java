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
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
public final class GiteaMilestone extends GiteaObject<GiteaMilestone> {
    private long id;
    private String title;
    private String description;
    private String state;
    private long openIssues;
    private long closedIssues;
    private Date closedAt;
    private Date dueOn;

    public GiteaMilestone() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getOpenIssues() {
        return openIssues;
    }

    @JsonProperty("open_issues")
    public void setOpenIssues(long openIssues) {
        this.openIssues = openIssues;
    }

    public long getClosedIssues() {
        return closedIssues;
    }

    @JsonProperty("closed_issues")
    public void setClosedIssues(long closedIssues) {
        this.closedIssues = closedIssues;
    }

    public Date getClosedAt() {
        return closedAt == null ? null : (Date) closedAt.clone();
    }

    @JsonProperty("closed_at")
    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt == null ? null : (Date) closedAt.clone();
    }

    public Date getDueOn() {
        return dueOn == null ? null : (Date) dueOn.clone();
    }

    @JsonProperty("due_on")
    public void setDueOn(Date dueOn) {
        this.dueOn = dueOn == null ? null : (Date) dueOn.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GiteaMilestone{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", state='" + state + '\'' +
                ", openIssues=" + openIssues +
                ", closedIssues=" + closedIssues +
                ", closedAt=" + closedAt +
                ", dueOn=" + dueOn +
                '}';
    }
}
