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

public class GiteaOrganization extends GiteaOwner {
    private String description;
    private String website;
    private String location;

    public GiteaOrganization() {
    }

    public GiteaOrganization(String login, String fullName, String avatarUrl, String description,
                             String website, String location) {
        super(login, fullName, "", avatarUrl);
        this.description = description;
        this.website = website;
        this.location = location;
    }

    @Override
    public GiteaOrganization clone() {
        return (GiteaOrganization) super.clone();
    }

    @Override
    public String toString() {
        return "GiteaOrganization{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", description='" + description + '\'' +
                ", website='" + website + '\'' +
                ", username='" + getUsername() + '\'' +
                ", avatarUrl='" + getAvatarUrl() + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public String getLogin() {
        return getUsername();
    }

    @Override
    public void setLogin(String login) {
        setUsername(login);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
