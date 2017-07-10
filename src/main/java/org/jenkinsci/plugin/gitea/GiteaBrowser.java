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
package org.jenkinsci.plugin.gitea;

import com.damnhandy.uri.template.UriTemplate;
import com.damnhandy.uri.template.UriTemplateBuilder;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import java.io.IOException;
import java.net.URL;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugin.gitea.servers.GiteaServers;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * A {@link GitRepositoryBrowser} for Gitea.
 */
public class GiteaBrowser extends GitRepositoryBrowser {

    /**
     * Standardize serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * @param repoUrl the repository URL.
     */
    @DataBoundConstructor
    public GiteaBrowser(String repoUrl) {
        super(GiteaServers.normalizeServerUrl(repoUrl));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        return new URL(
                UriTemplate.buildFromTemplate(getRepoUrl())
                        .literal("/commit")
                        .path(UriTemplateBuilder.var("changeSet"))
                        .build()
                        .set("changeSet", changeSet.getId())
                        .expand()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getDiffLink(Path path) throws IOException {
        if (path.getEditType() != EditType.EDIT || path.getSrc() == null || path.getDst() == null
                || path.getChangeSet().getParentCommit() == null) {
            return null;
        }
        return diffLink(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getFileLink(Path path) throws IOException {
        if (path.getEditType().equals(EditType.DELETE)) {
            return diffLink(path);
        } else {
            return new URL(
                    UriTemplate.buildFromTemplate(getRepoUrl())
                            .literal("/src")
                            .path(UriTemplateBuilder.var("changeSet"))
                            .path(UriTemplateBuilder.var("path", true))
                            .build()
                            .set("changeSet", path.getChangeSet().getId())
                            .set("path", StringUtils.split(path.getPath(), '/'))
                            .expand()
            );
        }
    }

    /**
     * Generates a diff link for the supplied path.
     * @param path the path.
     * @return the diff link.
     * @throws IOException if there was an error parsing the index of the path from the changeset.
     */
    private URL diffLink(Path path) throws IOException {
        return new URL(
                UriTemplate.buildFromTemplate(getRepoUrl())
                        .literal("/commit")
                        .path(UriTemplateBuilder.var("changeSet"))
                        .fragment(UriTemplateBuilder.var("diff"))
                        .build()
                        .set("changeSet", path.getChangeSet().getId())
                        .set("diff", "diff-" + Integer.toString(getIndexOfPath(path) + 1))
                        .expand()
        );
    }

    /**
     * Our descriptor.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.GiteaBrowser_displayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public GiteaBrowser newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            return req.bindJSON(GiteaBrowser.class, jsonObject);
        }
    }
}
