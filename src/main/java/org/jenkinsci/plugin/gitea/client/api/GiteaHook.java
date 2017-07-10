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
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = Gitea.IGNORE_UNKNOWN_PROPERTIES)
public final class GiteaHook extends GiteaObject<GiteaHook> {
    private long id;
    private GiteaHookType type;
    private Configuration config;
    private Set<GiteaEventType> events;
    private boolean active;
    private Date createdAt;
    private Date updatedAt;

    public GiteaHook() {
    }

    public GiteaHook(GiteaHookType type, Configuration config,
                     Set<GiteaEventType> events, boolean active) {
        this.type = type;
        this.config = config;
        this.events = events;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GiteaHookType getType() {
        return type;
    }

    public void setType(GiteaHookType type) {
        this.type = type;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public Set<GiteaEventType> getEvents() {
        return events;
    }

    public void setEvents(Set<GiteaEventType> events) {
        this.events = events;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static class Configuration implements Cloneable {
        private GiteaPayloadType contentType;
        private String url;
        private String color;
        private String channel;
        private String iconUrl;
        private String username;

        public Configuration() {
        }

        public GiteaPayloadType getContentType() {
            return contentType;
        }

        @JsonProperty("content_type")
        public void setContentType(GiteaPayloadType contentType) {
            this.contentType = contentType;
        }        @Override
        public Configuration clone() {
            try {
                return (Configuration) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        @JsonProperty("icon_url")
        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }




        @Override
        public String toString() {
            return "Configuration{" +
                    "contentType=" + contentType +
                    ", url='" + url + '\'' +
                    ", channel='" + channel + '\'' +
                    ", color='" + color + '\'' +
                    ", iconUrl='" + iconUrl + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GiteaHook{" +
                "id=" + id +
                ", type=" + type +
                ", config=" + config +
                ", events=" + events +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }


}
