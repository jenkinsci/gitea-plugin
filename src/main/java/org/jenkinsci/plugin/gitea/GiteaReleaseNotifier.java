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
package org.jenkinsci.plugin.gitea;

import java.io.IOException;

import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMSource;
import jenkins.util.VirtualFile;

public class GiteaReleaseNotifier {

    public static void publishArtifacts(Run<?, ?> build, TaskListener listener)
            throws IOException, InterruptedException {

        if (build.getResult() != Result.SUCCESS) {
            // do not push assets when the pipeline wasn't a success
            listener.getLogger().format("[Gitea] do not publish assets due to build being non-Successfully%n");
            return;
        }

        final SCMSource s = SCMSource.SourceByItem.findSource(build.getParent());
        if (!(s instanceof GiteaSCMSource)) {
            listener.getLogger().format("[Gitea] do not publish assets due to source being no GiteaSCMSource%n");
            return;
        }
        final GiteaSCMSource source = (GiteaSCMSource) s;
        if (!new GiteaSCMSourceContext(null, SCMHeadObserver.none())
                .withTraits(source.getTraits())
                .artifactToAssetMappingEnabled()) {
            return;
        }
        final SCMHead head = SCMHead.HeadByItem.findHead(build.getParent());
        if (head == null || !(head instanceof ReleaseSCMHead)) {
            listener.getLogger().format("[Gitea] do not publish assets due to head either being null or no ReleaseSCMHead%n");
            return;
        }

        try (GiteaConnection c = source.gitea().open()) {
            GiteaRepository repository = c.fetchRepository(source.getRepoOwner(), source.getRepository());
            long releaseId = ((ReleaseSCMHead) head).getId();

            for (Run<?, ?>.Artifact artifact : build.getArtifacts()) {
                VirtualFile file = build.getArtifactManager().root().child(artifact.relativePath);
                c.createReleaseAttachment(repository, releaseId, artifact.getFileName(), file.open());
                listener.getLogger().format("[Gitea] Published asset from archived artifact %s%n", artifact.getFileName());
            }
        }
    }

    @Extension
    public static class JobCompletedListener extends RunListener<Run<?, ?>> {

        @Override
        public void onCompleted(Run<?, ?> build, TaskListener listener) {
            try {
                publishArtifacts(build, listener);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace(listener.error("Could not upload assets for release"));
            }
        }
    }
}
