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

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.damnhandy.uri.template.UriTemplate;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.console.HyperlinkNote;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import jenkins.model.Jenkins;
import jenkins.plugins.git.traits.GitBrowserSCMSourceTrait;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.SCMNavigator;
import jenkins.scm.api.SCMNavigatorDescriptor;
import jenkins.scm.api.SCMNavigatorEvent;
import jenkins.scm.api.SCMNavigatorOwner;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceObserver;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.trait.SCMNavigatorRequest;
import jenkins.scm.api.trait.SCMNavigatorTrait;
import jenkins.scm.api.trait.SCMNavigatorTraitDescriptor;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMTrait;
import jenkins.scm.api.trait.SCMTraitDescriptor;
import jenkins.scm.impl.form.NamedArrayList;
import jenkins.scm.impl.trait.Discovery;
import jenkins.scm.impl.trait.Selection;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugin.gitea.client.api.GiteaAuth;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.Gitea;
import org.jenkinsci.plugin.gitea.client.api.GiteaOrganization;
import org.jenkinsci.plugin.gitea.client.api.GiteaOwner;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.jenkinsci.plugin.gitea.servers.GiteaServer;
import org.jenkinsci.plugin.gitea.servers.GiteaServers;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class GiteaSCMNavigator extends SCMNavigator {
    private final String serverUrl;
    private final String repoOwner;
    private String credentialsId;
    private List<SCMTrait<?>> traits = new ArrayList<>();
    private GiteaOwner giteaOwner;

    @DataBoundConstructor
    public GiteaSCMNavigator(String serverUrl, String repoOwner) {
        this.serverUrl = serverUrl;
        this.repoOwner = repoOwner;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @NonNull
    public List<SCMTrait<?>> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    @DataBoundSetter
    public void setTraits(List<SCMTrait<?>> traits) {
        this.traits = new ArrayList<>(Util.fixNull(traits));
    }


    @NonNull
    @Override
    protected String id() {
        return serverUrl + "::" + repoOwner;
    }

    @Override
    public void visitSources(@NonNull final SCMSourceObserver observer) throws IOException, InterruptedException {
        try (GiteaSCMNavigatorRequest request = new GiteaSCMNavigatorContext()
                .withTraits(traits)
                .newRequest(this, observer);
             GiteaConnection c = gitea(observer.getContext()).open()) {
            giteaOwner = c.fetchUser(repoOwner);
            if (StringUtils.isBlank(giteaOwner.getEmail())) {
                giteaOwner = c.fetchOrganization(repoOwner);
            }
            List<GiteaRepository> repositories = c.fetchRepositories(giteaOwner);
            int count = 0;
            observer.getListener().getLogger().format("%n  Checking repositories...%n");
            Set<Long> seen = new HashSet<>();
            for (GiteaRepository r : repositories) {
                // TODO remove this hack for Gitea listing the repositories multiple times
                if (seen.contains(r.getId())) {
                    continue;
                } else {
                    seen.add(r.getId());
                }
                if (!StringUtils.equalsIgnoreCase(r.getOwner().getUsername(), repoOwner)) {
                    // this is the user repos which includes all organizations that they are a member of
                    continue;
                }
                count++;
                if (r.isEmpty()) {
                    observer.getListener().getLogger().format("%n    Ignoring empty repository %s%n",
                            HyperlinkNote.encodeTo(r.getHtmlUrl(), r.getName()));
                    continue;
                }
                observer.getListener().getLogger().format("%n    Checking repository %s%n",
                        HyperlinkNote.encodeTo(r.getHtmlUrl(), r.getName()));
                if (request.process(r.getName(), new SCMNavigatorRequest.SourceLambda() {
                    @NonNull
                    @Override
                    public SCMSource create(@NonNull String projectName) throws IOException, InterruptedException {
                        return new GiteaSCMSourceBuilder(
                                getId() + "::" + projectName,
                                serverUrl,
                                credentialsId,
                                repoOwner,
                                projectName
                        )
                                .withTraits(traits)
                                .build();
                    }
                }, null, new SCMNavigatorRequest.Witness() {
                    @Override
                    public void record(@NonNull String projectName, boolean isMatch) {
                        if (isMatch) {
                            observer.getListener().getLogger().format("      Proposing %s%n", projectName);
                        } else {
                            observer.getListener().getLogger().format("      Ignoring %s%n", projectName);
                        }
                    }
                })) {
                    observer.getListener().getLogger().format("%n  %d repositories were processed (query complete)%n",
                            count);
                    return;
                }
            }
            observer.getListener().getLogger().format("%n  %d repositories were processed%n", count);
        }
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@NonNull SCMNavigatorOwner owner, SCMNavigatorEvent event,
                                           @NonNull TaskListener listener) throws IOException, InterruptedException {
        if (this.giteaOwner == null) {
            try (GiteaConnection c = gitea(owner).open()) {
                this.giteaOwner = c.fetchUser(repoOwner);
                if (StringUtils.isBlank(giteaOwner.getEmail())) {
                    this.giteaOwner = c.fetchOrganization(repoOwner);
                }
            }
        }
        List<Action> result = new ArrayList<>();
        String objectUrl = UriTemplate.buildFromTemplate(serverUrl)
                .path("owner")
                .build()
                .set("owner", repoOwner)
                .expand();
        result.add(new ObjectMetadataAction(
                Util.fixEmpty(giteaOwner.getFullName()),
                null,
                objectUrl)
        );
        if (StringUtils.isNotBlank(giteaOwner.getAvatarUrl())) {
            result.add(new GiteaAvatar(giteaOwner.getAvatarUrl()));
        }
        result.add(new GiteaLink("icon-gitea-org", objectUrl));
        if (giteaOwner instanceof GiteaOrganization) {
            String website = ((GiteaOrganization) giteaOwner).getWebsite();
            if (StringUtils.isBlank(website)) {
                listener.getLogger().println("Organization URL: unspecified");
                listener.getLogger().printf("Organization URL: %s%n",
                        HyperlinkNote.encodeTo(website, StringUtils.defaultIfBlank(giteaOwner.getFullName(), website)));
            }
        }
        return result;
    }

    @Override
    public void afterSave(@NonNull SCMNavigatorOwner owner) {
        WebhookRegistration mode = new GiteaSCMSourceContext(null, SCMHeadObserver.none())
                .withTraits(new GiteaSCMNavigatorContext().withTraits(traits).traits())
                .webhookRegistration();
        GiteaWebhookListener.register(owner, this, mode, credentialsId);
    }

    private Gitea gitea(SCMSourceOwner owner) throws AbortException {
        GiteaServer server = GiteaServers.get().findServer(serverUrl);
        if (server == null) {
            throw new AbortException("Unknown server: " + serverUrl);
        }
        StandardCredentials credentials = credentials(owner);
        CredentialsProvider.track(owner, credentials);
        return Gitea.server(serverUrl)
                .as(AuthenticationTokens.convert(GiteaAuth.class, credentials));
    }

    public StandardCredentials credentials(SCMSourceOwner owner) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StandardCredentials.class,
                        owner,
                        Jenkins.getAuthentication(),
                        URIRequirementBuilder.fromUri(serverUrl).build()
                ),
                CredentialsMatchers.allOf(
                        AuthenticationTokens.matcher(GiteaAuth.class),
                        CredentialsMatchers.withId(credentialsId)
                )
        );
    }

    @Extension
    public static class DescriptorImpl extends SCMNavigatorDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.GiteaSCMNavigator_displayName();
        }

        public ListBoxModel doFillServerUrlItems(@AncestorInPath SCMSourceOwner context,
                                                 @QueryParameter String serverUrl) {
            if (context == null) {
                if (!Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)) {
                    // must have admin if you want the list without a context
                    ListBoxModel result = new ListBoxModel();
                    result.add(serverUrl);
                    return result;
                }
            } else {
                if (!context.hasPermission(Item.EXTENDED_READ)) {
                    // must be able to read the configuration the list
                    ListBoxModel result = new ListBoxModel();
                    result.add(serverUrl);
                    return result;
                }
            }
            return GiteaServers.get().getServerItems();
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath SCMSourceOwner context,
                                                     @QueryParameter String serverUrl,
                                                     @QueryParameter String credentialsId) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (context == null) {
                if (!Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)) {
                    // must have admin if you want the list without a context
                    result.includeCurrentValue(credentialsId);
                    return result;
                }
            } else {
                if (!context.hasPermission(Item.EXTENDED_READ)
                        && !context.hasPermission(CredentialsProvider.USE_ITEM)) {
                    // must be able to read the configuration or use the item credentials if you want the list
                    result.includeCurrentValue(credentialsId);
                    return result;
                }
            }
            result.includeEmptyValue();
            result.includeMatchingAs(
                    context instanceof Queue.Task ?
                            Tasks.getDefaultAuthenticationOf((Queue.Task) context)
                            : ACL.SYSTEM,
                    context,
                    StandardCredentials.class,
                    URIRequirementBuilder.fromUri(serverUrl).build(),
                    AuthenticationTokens.matcher(GiteaAuth.class)
            );
            return result;
        }

        public FormValidation doCheckCredentialsId(@AncestorInPath SCMSourceOwner context,
                                                   @QueryParameter String serverUrl,
                                                   @QueryParameter String value)
                throws IOException, InterruptedException {
            if (context == null) {
                if (!Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else {
                if (!context.hasPermission(Item.EXTENDED_READ)
                        && !context.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }
            GiteaServer server = GiteaServers.get().findServer(serverUrl);
            if (server == null) {
                return FormValidation.ok();
            }
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            if (CredentialsProvider.listCredentials(
                    StandardCredentials.class,
                    context,
                    context instanceof Queue.Task ?
                            Tasks.getDefaultAuthenticationOf((Queue.Task) context)
                            : ACL.SYSTEM,
                    URIRequirementBuilder.fromUri(serverUrl).build(),
                    CredentialsMatchers.allOf(
                            CredentialsMatchers.withId(value),
                            AuthenticationTokens.matcher(GiteaAuth.class)

                    )).isEmpty()) {
                return FormValidation.error(Messages.GiteaSCMNavigator_selectedCredentialsMissing());
            }
            return FormValidation.ok();
        }

        @NonNull
        @Override
        public String getDescription() {
            return Messages.GiteaSCMNavigator_description();
        }

        @Override
        public String getIconClassName() {
            return "icon-gitea-org";
        }

        @Override
        public String getPronoun() {
            return Messages.GiteaSCMNavigator_pronoun();
        }

        @Override
        public SCMNavigator newInstance(String name) {
            List<GiteaServer> servers = GiteaServers.get().getServers();
            GiteaSCMNavigator navigator =
                    new GiteaSCMNavigator(servers.isEmpty() ? null : servers.get(0).getServerUrl(), name);
            navigator.setTraits(getTraitsDefaults());
            return navigator;
        }

        @SuppressWarnings("unused") // jelly
        public List<NamedArrayList<? extends SCMTraitDescriptor<?>>> getTraitsDescriptorLists() {
            GiteaSCMSource.DescriptorImpl sourceDescriptor =
                    Jenkins.getActiveInstance().getDescriptorByType(GiteaSCMSource.DescriptorImpl.class);
            List<SCMTraitDescriptor<?>> all = new ArrayList<>();
            all.addAll(SCMNavigatorTrait._for(this, GiteaSCMNavigatorContext.class, GiteaSCMSourceBuilder.class));
            all.addAll(SCMSourceTrait._for(sourceDescriptor, GiteaSCMSourceContext.class, null));
            all.addAll(SCMSourceTrait._for(sourceDescriptor, null, GiteaSCMBuilder.class));
            Set<SCMTraitDescriptor<?>> dedup = new HashSet<>();
            for (Iterator<SCMTraitDescriptor<?>> iterator = all.iterator(); iterator.hasNext(); ) {
                SCMTraitDescriptor<?> d = iterator.next();
                if (dedup.contains(d)
                        || d instanceof GitBrowserSCMSourceTrait.DescriptorImpl) {
                    // remove any we have seen already and ban the browser configuration as it will always be github
                    iterator.remove();
                } else {
                    dedup.add(d);
                }
            }
            List<NamedArrayList<? extends SCMTraitDescriptor<?>>> result = new ArrayList<>();
            NamedArrayList.select(all, Messages.GiteaSCMNavigator_traitSection_repositories(), new NamedArrayList.Predicate<SCMTraitDescriptor<?>>() {
                        @Override
                        public boolean test(SCMTraitDescriptor<?> scmTraitDescriptor) {
                            return scmTraitDescriptor instanceof SCMNavigatorTraitDescriptor;
                        }
                    },
                    true, result);
            NamedArrayList.select(all, Messages.GiteaSCMNavigator_traitSection_withinRepo(), NamedArrayList
                            .anyOf(NamedArrayList.withAnnotation(Discovery.class),
                                    NamedArrayList.withAnnotation(Selection.class)),
                    true, result);
            NamedArrayList.select(all, Messages.GiteaSCMNavigator_traitSection_additional(), null, true, result);
            return result;
        }

        public List<SCMTrait<? extends SCMTrait<?>>> getTraitsDefaults() {
            GiteaSCMSource.DescriptorImpl descriptor =
                    ExtensionList.lookup(Descriptor.class).get(GiteaSCMSource.DescriptorImpl.class);
            if (descriptor == null) {
                throw new AssertionError();
            }
            List<SCMTrait<? extends SCMTrait<?>>> result = new ArrayList<>();
            result.addAll(descriptor.getTraitsDefaults());
            return result;
        }

    }

}
