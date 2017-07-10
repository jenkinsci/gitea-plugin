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
import hudson.Util;
import java.util.ArrayList;
import java.util.List;

/**
 * Gitea {@link GiteaEventType#PUSH} event.
 */
@JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
public class GiteaPushEvent extends GiteaEvent {
    private String ref;
    private String before;
    private String after;
    private String compareUrl;
    private List<GiteaCommit> commits;
    private GiteaOwner pusher;

    @Override
    public GiteaPushEvent clone() {
        return (GiteaPushEvent) super.clone();
    }

    @Override
    public String toString() {
        return "GiteaPushEvent{" +
                super.toString() +
                ", ref='" + ref + '\'' +
                ", before='" + before + '\'' +
                ", after='" + after + '\'' +
                ", compareUrl='" + compareUrl + '\'' +
                ", commits=" + commits +
                ", pusher=" + pusher +
                '}';
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getCompareUrl() {
        return compareUrl;
    }

    @JsonProperty("compare_url")
    public void setCompareUrl(String compareUrl) {
        this.compareUrl = compareUrl;
    }

    public List<GiteaCommit> getCommits() {
        List<GiteaCommit> result = new ArrayList<>();
        for (GiteaCommit c : Util.fixNull(commits)) {
            result.add(c.clone());
        }
        return result;
    }

    public void setCommits(List<GiteaCommit> commits) {
        List<GiteaCommit> result = new ArrayList<>();
        for (GiteaCommit c : Util.fixNull(commits)) {
            result.add(c.clone());
        }
        this.commits = result;
    }

    public GiteaOwner getPusher() {
        return pusher == null ? null : pusher.clone();
    }

    public void setPusher(GiteaOwner pusher) {
        this.pusher = pusher == null ? null : pusher.clone();
    }
}
