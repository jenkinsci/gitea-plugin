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

@JsonIgnoreProperties(
        value = {"size"},
        ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES
)
public class GiteaRepository extends GiteaObject<GiteaRepository> {
    private long id;
    private GiteaOwner owner;
    private GiteaRepository parent;
    private String name;
    private String fullName;
    private String description;
    private boolean _private;
    private boolean fork;
    private boolean empty;
    private boolean mirror;
    private boolean archived;
    private String htmlUrl;
    private String sshUrl;
    private String cloneUrl;
    private String website;
    private long starsCount;
    private long forksCount;
    private long watchersCount;
    private long openIssuesCount;
    private String defaultBranch;
    private Date createdAt;
    private Date updatedAt;
    private String avatarUrl;
    private Permissions permissions;

    public GiteaRepository() {
    }

    public GiteaRepository(GiteaOwner owner, GiteaRepository parent, String name, String fullName,
                           String description, boolean _private, boolean fork, boolean empty, boolean mirror, boolean archived,
                           String htmlUrl, String sshUrl, String cloneUrl, String website, long starsCount,
                           long forksCount,
                           long watchersCount, long openIssuesCount, String defaultBranch, String avatarUrl,
                           Permissions permissions) {
        this.owner = owner;
        this.parent = parent;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this._private = _private;
        this.fork = fork;
        this.empty = empty;
        this.mirror = mirror;
        this.archived = archived;
        this.htmlUrl = htmlUrl;
        this.sshUrl = sshUrl;
        this.cloneUrl = cloneUrl;
        this.website = website;
        this.starsCount = starsCount;
        this.forksCount = forksCount;
        this.watchersCount = watchersCount;
        this.openIssuesCount = openIssuesCount;
        this.defaultBranch = defaultBranch;
        this.avatarUrl = avatarUrl;
        this.permissions = permissions;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GiteaOwner getOwner() {
        return owner == null ? null : owner.clone();
    }

    public void setOwner(GiteaOwner owner) {
        this.owner = owner == null ? null : owner.clone();
    }

    public GiteaRepository getParent() {
        return parent == null ? null : parent.clone();
    }

    public void setParent(GiteaRepository parent) {
        this.parent = parent == null ? null : parent.clone();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    @JsonProperty("full_name")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrivate() {
        return _private;
    }

    public void setPrivate(boolean _private) {
        this._private = _private;
    }

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean isMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    @JsonProperty("html_url")
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getSshUrl() {
        return sshUrl;
    }

    @JsonProperty("ssh_url")
    public void setSshUrl(String sshUrl) {
        this.sshUrl = sshUrl;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    @JsonProperty("clone_url")
    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public long getStarsCount() {
        return starsCount;
    }

    @JsonProperty("stars_count")
    public void setStarsCount(long starsCount) {
        this.starsCount = starsCount;
    }

    public long getForksCount() {
        return forksCount;
    }

    @JsonProperty("forks_count")
    public void setForksCount(long forksCount) {
        this.forksCount = forksCount;
    }

    public long getWatchersCount() {
        return watchersCount;
    }

    @JsonProperty("watchers_count")
    public void setWatchersCount(long watchersCount) {
        this.watchersCount = watchersCount;
    }

    public long getOpenIssuesCount() {
        return openIssuesCount;
    }

    @JsonProperty("open_issues_count")
    public void setOpenIssuesCount(long openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    @JsonProperty("default_branch")
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    @JsonProperty("avatar_url")
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Permissions getPermissions() {
        return permissions == null ? null : permissions.clone();
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions == null ? null : permissions.clone();
    }

    @Override
    public String toString() {
        return "GiteaRepository{" +
                "id=" + id +
                ", owner=" + owner +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", description='" + description + '\'' +
                ", private=" + _private +
                ", fork=" + fork +
                ", empty=" + empty +
                ", mirror=" + mirror +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", sshUrl='" + sshUrl + '\'' +
                ", cloneUrl='" + cloneUrl + '\'' +
                ", website='" + website + '\'' +
                ", starsCount=" + starsCount +
                ", forksCount=" + forksCount +
                ", watchersCount=" + watchersCount +
                ", openIssuesCount=" + openIssuesCount +
                ", defaultBranch='" + defaultBranch + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", permissions=" + permissions +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
    public static class Permissions implements Cloneable {
        private boolean admin;
        private boolean push;
        private boolean pull;

        public Permissions() {
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }        @Override
        public Permissions clone() {
            try {
                return (Permissions) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }

        public boolean isPush() {
            return push;
        }

        public void setPush(boolean push) {
            this.push = push;
        }

        public boolean isPull() {
            return pull;
        }

        public void setPull(boolean pull) {
            this.pull = pull;
        }

        @Override
        public String toString() {
            return "PullSummary{" +
                    "admin=" + admin +
                    ", push=" + push +
                    ", pull=" + pull +
                    '}';
        }
    }

}
