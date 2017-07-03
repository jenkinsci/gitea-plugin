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

/**
 * <h1>Jenkins SCM API implementation for <a href="https://gitea.io/">Gitea</a></h1>.
 *
 * The primary entry points for the implementation are:
 *
 * <ul>
 * <li>{@link org.jenkinsci.plugin.gitea.GiteaSCMSource} - the {@link jenkins.scm.api.SCMSource} implementation</li>
 * <li>{@link org.jenkinsci.plugin.gitea.GiteaSCMNavigator} - the {@link jenkins.scm.api.SCMNavigator}
 * implementation</li>
 * </ul>
 *
 * These implementations are {@link jenkins.scm.api.trait.SCMTrait} based and accept the traits for
 * {@link jenkins.plugins.git.AbstractGitSCMSource} as well as the Gitea specific traits:
 * <ul>
 * <li>{@link org.jenkinsci.plugin.gitea.BranchDiscoveryTrait}</li>
 * <li>{@link org.jenkinsci.plugin.gitea.ForkPullRequestDiscoveryTrait}</li>
 * <li>{@link org.jenkinsci.plugin.gitea.OriginPullRequestDiscoveryTrait}</li>
 * <li>{@link org.jenkinsci.plugin.gitea.SSHCheckoutTrait}</li>
 * <li>{@link org.jenkinsci.plugin.gitea.WebhookRegistrationTrait}</li>
 * </ul>
 *
 * Extension plugins wanting to add Gitea-specific traits should target at least one of:
 * <ul>
 * <li>{@link org.jenkinsci.plugin.gitea.GiteaSCMNavigatorContext} for
 * {@linkplain jenkins.scm.api.trait.SCMNavigatorTrait}s</li>
 * <li>{@link org.jenkinsci.plugin.gitea.GiteaSCMNavigatorRequest} for
 * {@linkplain jenkins.scm.api.trait.SCMNavigatorTrait}s</li>
 * <li>{@link org.jenkinsci.plugin.gitea.GiteaSCMSourceBuilder} for
 * {@linkplain jenkins.scm.api.trait.SCMNavigatorTrait}s</li>
 * <li>{@link org.jenkinsci.plugin.gitea.GiteaSCMSourceContext} for
 * {@linkplain jenkins.scm.api.trait.SCMSourceTrait}s</li>
 * <li>{@link org.jenkinsci.plugin.gitea.GiteaSCMSourceRequest} for
 * {@linkplain jenkins.scm.api.trait.SCMSourceTrait}s</li>
 * <li>{@link org.jenkinsci.plugin.gitea.GiteaSCMBuilder} for
 * {@linkplain jenkins.scm.api.trait.SCMSourceTrait}s</li>
 * </ul>
 *
 * Events from a Gitea server are propagated through the standard {@link jenkins.scm.api.SCMEvent} subsystem.
 * Plugins wanting to listen for Gitea events and take custom actions based on the Gitea payloads should
 * listen for the relevant {@link org.jenkinsci.plugin.gitea.AbstractGiteaSCMHeadEvent} subclass.
 * Adding support for new event types requires
 * <ol>
 * <li>implementing the required payload type (extending from
 * {@link org.jenkinsci.plugin.gitea.client.api.GiteaEvent})</li>
 * <li>implementing the required {@link jenkins.scm.api.SCMEvent} subclass</li>
 * <li>providing a {@link org.jenkinsci.plugin.gitea.GiteaWebhookHandler} extension to wire it all together</li>
 * </ol>
 */
package org.jenkinsci.plugin.gitea;
