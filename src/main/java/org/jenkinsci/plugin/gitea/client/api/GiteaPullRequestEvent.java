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

@JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
public class GiteaPullRequestEvent extends GiteaEvent {
    private GiteaPullRequestEventType action;
    private long number;
    private GiteaPullRequest pullRequest;

    @Override
    public GiteaPullRequestEvent clone() {
        return (GiteaPullRequestEvent) super.clone();
    }

    @Override
    public String toString() {
        return "GiteaPullRequestEvent{" +
                super.toString() +
                ", action=" + action +
                ", number=" + number +
                ", pullRequest=" + pullRequest +
                '}';
    }

    public GiteaPullRequestEventType getAction() {
        return action;
    }

    public void setAction(GiteaPullRequestEventType action) {
        this.action = action;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public GiteaPullRequest getPullRequest() {
        return pullRequest == null ? null : pullRequest.clone();
    }

    @JsonProperty("pull_request")
    public void setPullRequest(GiteaPullRequest pullRequest) {
        this.pullRequest = pullRequest == null ? null : pullRequest.clone();
    }
}
