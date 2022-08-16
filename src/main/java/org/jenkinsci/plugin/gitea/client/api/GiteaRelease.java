/*
 * The MIT License
 *
 * Copyright (c) 2017-2022, CloudBees, Inc.
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
public class GiteaRelease extends GiteaObject<GiteaRelease> {
    private long id;
    private String tagName;
    private String targetCommitish;
    private String name;
    private String body;
    private String url;
    private String htmlUrl;
    private String tarballUrl;
    private String zipballUrl;
    private boolean draft;
    private boolean prerelease;
    private Date createdAt;
    private Date publishedAt;
    private GiteaOwner author;
    private List<Attachment> assets = new ArrayList<>();

    public GiteaRelease() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    @JsonProperty("tag_name")
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTargetCommitish() {
        return targetCommitish;
    }

    @JsonProperty("target_commitish")
    public void setTargetCommitish(String targetCommitish) {
        this.targetCommitish = targetCommitish;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    @JsonProperty("html_url")
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getTarballUrl() {
        return tarballUrl;
    }

    @JsonProperty("tarball_url")
    public void setTarballUrl(String tarballUrl) {
        this.tarballUrl = tarballUrl;
    }

    public String getZipballUrl() {
        return zipballUrl;
    }

    @JsonProperty("zipball_url")
    public void setZipballUrl(String zipballUrl) {
        this.zipballUrl = zipballUrl;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public boolean isPrerelease() {
        return draft;
    }

    public void setPrerelease(boolean prerelease) {
        this.prerelease = prerelease;
    }

    public Date getCreatedAt() {
        return createdAt == null ? null : (Date) createdAt.clone();
    }

    @JsonProperty("created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt == null ? null : (Date) createdAt.clone();
    }

    public Date getPublishedAt() {
        return publishedAt == null ? null : (Date) publishedAt.clone();
    }

    @JsonProperty("published_at")
    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt == null ? null : (Date) publishedAt.clone();
    }

    public GiteaOwner getAuthor() {
        return author == null ? null : author.clone();
    }

    public void setAuthor(GiteaOwner author) {
        this.author = author == null ? null : author.clone();
    }

    @Override
    public String toString() {
        return "GiteaRelease{" +
                "id=" + id +
                ", tagName='" + tagName + '\'' +
                ", targetCommitish='" + targetCommitish + '\'' +
                ", name='" + name + '\'' +
                ", body='" + body + '\'' +
                ", url='" + url + '\'' +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", tarballUrl='" + tarballUrl + '\'' +
                ", zipballUrl='" + zipballUrl + '\'' +
                ", draft=" + draft +
                ", prerelease=" + prerelease +
                ", createdAt=" + createdAt +
                ", publishedAt=" + publishedAt +
                ", author=" + author +
                ", assets=" + assets +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class Attachment extends GiteaObject<Attachment> {
        private long id;
        private String name;
        private long size;
        private long downloadCount;
        private Date createdAt;
        private String uuid;
        private String browserDownloadUrl;

        public Attachment() {
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getDownloadCount() {
            return downloadCount;
        }

        @JsonProperty("download_count")
        public void setDownloadCount(long downloadCount) {
            this.downloadCount = downloadCount;
        }

        public Date getCreatedAt() {
            return createdAt == null ? null : (Date) createdAt.clone();
        }

        @JsonProperty("created_at")
        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt == null ? null : (Date) createdAt.clone();
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getBrowserDownloadUrl() {
            return browserDownloadUrl;
        }

        @JsonProperty("browser_download_url")
        public void setBrowserDownloadUrl(String browserDownloadUrl) {
            this.browserDownloadUrl = browserDownloadUrl;
        }

        @Override
        public String toString() {
            return "Attachment{" +
                    "id=" + id +
                    ", admin='" + name + '\'' +
                    ", size=" + size +
                    ", downloadCount=" + downloadCount +
                    ", createdAt=" + createdAt +
                    ", uuid=" + uuid +
                    ", browserDownloadUrl=" + browserDownloadUrl +
                    '}';
        }

    }

}
