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
package org.jenkinsci.plugin.gitea.tasks;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Future;
import hudson.remoting.Pipe;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.MasterToSlaveFileCallable;
import jenkins.SlaveToMasterFileCallable;
import jenkins.model.Jenkins;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMSource;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugin.gitea.GiteaSCMSource;
import org.jenkinsci.plugin.gitea.Messages;
import org.jenkinsci.plugin.gitea.ReleaseSCMHead;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

public class GiteaAssetPublisher implements SimpleBuildStep, Describable<GiteaAssetPublisher> {

    private static final Logger LOGGER = Logger.getLogger(GiteaAssetPublisher.class.getName());

    private String assets;
    private String excludes;
    private boolean onlyIfSuccessful;
    private Boolean defaultExcludes = true;
    private Boolean caseSensitive = true;
    private Boolean followSymlinks = true;

    @DataBoundConstructor
    public GiteaAssetPublisher(String assets) {
        this.assets = assets.trim();
    }

    public String getAssets() {
        return assets;
    }

    public @CheckForNull String getExcludes() {
        return excludes;
    }

    @DataBoundSetter
    public final void setExcludes(@CheckForNull String excludes) {
        this.excludes = Util.fixEmptyAndTrim(excludes);
    }

    public boolean isOnlyIfSuccessful() {
        return onlyIfSuccessful;
    }

    @DataBoundSetter
    public final void setOnlyIfSuccessful(boolean onlyIfSuccessful) {
        this.onlyIfSuccessful = onlyIfSuccessful;
    }

    public boolean isDefaultExcludes() {
        return defaultExcludes;
    }

    @DataBoundSetter
    public final void setDefaultExcludes(boolean defaultExcludes) {
        this.defaultExcludes = defaultExcludes;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @DataBoundSetter
    public final void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isFollowSymlinks() {
        return followSymlinks;
    }

    @DataBoundSetter
    public final void setFollowSymlinks(boolean followSymlinks) {
        this.followSymlinks = followSymlinks;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        throw new IllegalStateException("GiteaAssetPublisher.perform(AbstractBuild, Launcher, BuildListener) should not have been called...");
    }

    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {

        if(assets.length()==0) {
            throw new AbortException(Messages.GiteaAssetPublisher_NoIncludes());
        }

        Result result = build.getResult();
        if (onlyIfSuccessful && result != null && result.isWorseThan(Result.UNSTABLE)) {
            listener.getLogger().println(Messages.GiteaAssetPublisher_SkipBecauseOnlyIfSuccessful());
            return;
        }

        final SCMSource s = SCMSource.SourceByItem.findSource(build.getParent());
        if (!(s instanceof GiteaSCMSource)) {
            listener.getLogger().println("Did not publish gitea release assets due to source being no GiteaSCMSource");
            return;
        }
        final GiteaSCMSource source = (GiteaSCMSource) s;
        final SCMHead head = SCMHead.HeadByItem.findHead(build.getParent());
        if (head == null || !(head instanceof ReleaseSCMHead)) {
            listener.getLogger().println("Did not publish gitea release assets due to head either being null or no ReleaseSCMHead");
            return;
        }

        listener.getLogger().println(Messages.GiteaAssetPublisher_StartPublishing());
        EnvVars environment = build.getEnvironment(listener);

        try {
            String assets = this.assets;
            if (build instanceof AbstractBuild) { // no expansion in pipelines
                assets = environment.expand(assets);
            }

            Map<String, String> files = workspace.act(new ListFiles(assets, excludes, defaultExcludes, caseSensitive, followSymlinks));
            if (!files.isEmpty()) {
                // now publish all files...
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    String archivedPath = entry.getKey();
                    assert archivedPath.indexOf('\\') == -1;
                    String workspacePath = entry.getValue();
                    assert workspacePath.indexOf('\\') == -1;

                    listener.getLogger().format("GiteaAssetPublisher: %s -> %s%n", archivedPath, workspacePath);

                    if (workspace.isRemote()) {
                        final Pipe pipe = Pipe.createRemoteToLocal();
                        Future<Void> future = workspace.actAsync(new StreamFileRemoteToLocal(pipe, workspacePath));

                        try (GiteaConnection c = source.gitea().open()) {
                            c.createReleaseAttachment(
                                source.getRepoOwner(), source.getRepository(), ((ReleaseSCMHead) head).getId(),
                                new File(archivedPath).getName(), pipe.getIn());
                        }

                        try {
                            future.get();
                        } catch (ExecutionException e) {
                            Throwable cause = e.getCause();
                            if (cause == null) {
                                cause = e;
                            }
                            throw cause instanceof IOException ? (IOException) cause : new IOException(cause);
                        }
                    } else {
                        FileInputStream file = new FileInputStream(new File(workspace.getRemote(), workspacePath));
                        try (GiteaConnection c = source.gitea().open()) {
                            c.createReleaseAttachment(
                                source.getRepoOwner(), source.getRepository(), ((ReleaseSCMHead) head).getId(),
                                new File(archivedPath).getName(), file);
                        }
                    }
                }
            } else {
                result = build.getResult();
                if (result == null || result.isBetterOrEqualTo(Result.UNSTABLE)) {
                    listener.getLogger().println(Messages.GiteaAssetPublisher_NoMatchFound(assets));
                }
            }
        } catch (java.nio.file.AccessDeniedException e) {
            LOGGER.log(Level.FINE, "Diagnosing anticipated Exception", e);
            throw new AbortException(e.toString()); // Message is not enough as that is the filename only
        }
    }

    private static final class StreamFileRemoteToLocal extends SlaveToMasterFileCallable<Void> {
        private final Pipe pipe;
        private final String workspacePath;

        StreamFileRemoteToLocal(Pipe pipe, String workspacePath) {
            this.pipe = pipe;
            this.workspacePath = workspacePath;
        }

        @Override
        public Void invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
            File file = new File(f, workspacePath);
            if (file.isDirectory()) {
                return null;
            }
            try (OutputStream out = pipe.getOut()) {
                IOUtils.copy(file, out);
            }
            return null;
        }
    }

    private static final class ListFiles extends MasterToSlaveFileCallable<Map<String,String>> {
        private final String includes, excludes;
        private final boolean defaultExcludes;
        private final boolean caseSensitive;
        private final boolean followSymlinks;

        ListFiles(String includes, String excludes, boolean defaultExcludes, boolean caseSensitive, boolean followSymlinks) {
            this.includes = includes;
            this.excludes = excludes;
            this.defaultExcludes = defaultExcludes;
            this.caseSensitive = caseSensitive;
            this.followSymlinks = followSymlinks;
        }

        @Override
        public Map<String, String> invoke(File basedir, VirtualChannel channel) throws IOException, InterruptedException {
            Map<String,String> r = new HashMap<>();

            FileSet fileSet = Util.createFileSet(basedir, includes, excludes);
            fileSet.setDefaultexcludes(defaultExcludes);
            fileSet.setCaseSensitive(caseSensitive);
            fileSet.setFollowSymlinks(followSymlinks);

            for (String f : fileSet.getDirectoryScanner().getIncludedFiles()) {
                f = f.replace(File.separatorChar, '/');
                r.put(f, f);
            }
            return r;
        }
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Collections.emptyList();
    }

    @Override
    public Descriptor<GiteaAssetPublisher> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(getClass());
    }

    @Extension @Symbol("publishGiteaAssets")
    public static final class DescriptorImpl extends BuildStepDescriptor<GiteaAssetPublisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public GiteaAssetPublisher newInstance(@NonNull StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(GiteaAssetPublisher.class, formData);
        }

        @Override
        public String getDisplayName() {
            return Messages.GiteaAssetPublisher_DisplayName();
        }
    }
}
