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
import com.damnhandy.uri.template.UriTemplateBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.AbortException;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.console.HyperlinkNote;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.TaskListener;
import hudson.model.queue.Tasks;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.authentication.tokens.api.AuthenticationTokens;
import jenkins.model.Jenkins;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.traits.GitBrowserSCMSourceTrait;
import jenkins.scm.api.*;
import jenkins.scm.api.metadata.ObjectMetadataAction;
import jenkins.scm.api.metadata.PrimaryInstanceMetadataAction;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMTraitDescriptor;
import jenkins.scm.impl.ChangeRequestSCMHeadCategory;
import jenkins.scm.impl.UncategorizedSCMHeadCategory;
import jenkins.scm.impl.form.NamedArrayList;
import jenkins.scm.impl.trait.Discovery;
import jenkins.scm.impl.trait.Selection;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkinsci.plugin.gitea.client.api.*;
import org.jenkinsci.plugin.gitea.servers.GiteaServer;
import org.jenkinsci.plugin.gitea.servers.GiteaServers;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GiteaSCMSource extends AbstractGitSCMSource {
    private static final Logger LOGGER = Logger.getLogger(GiteaSCMSource.class.getName());
    private final String serverUrl;
    private final String repoOwner;
    private final String repository;
    private String credentialsId;
    private List<SCMSourceTrait> traits = new ArrayList<>();
    private transient String sshRemote;
    private transient GiteaRepository giteaRepository;

    @DataBoundConstructor
    public GiteaSCMSource(String serverUrl, String repoOwner, String repository) {
        this.serverUrl = serverUrl;
        this.repoOwner = repoOwner;
        this.repository = repository;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getRepository() {
        return repository;
    }

    public String getSshRemote() {
        return sshRemote;
    }

    public void setSshRemote(String sshRemote) {
        this.sshRemote = sshRemote;
    }

    @Override
    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public String getRemote() {
        return GiteaSCMBuilder.checkoutUriTemplate(getOwner(), serverUrl, getSshRemote(), getCredentialsId())
                .set("owner", repoOwner).set("repository", repository).expand();
    }

    @NonNull
    @Override
    public List<SCMSourceTrait> getTraits() {
        return Collections.unmodifiableList(traits);
    }

    @DataBoundSetter
    public void setTraits(List<SCMSourceTrait> traits) {
        this.traits = new ArrayList<>(Util.fixNull(traits));
    }

    @Override
    protected SCMRevision retrieve(@NonNull SCMHead head, @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        try (GiteaConnection c = gitea().open()) {
            if (head instanceof BranchSCMHead) {
                listener.getLogger().format("Querying the current revision of branch %s...%n", head.getName());
                String revision = c.fetchBranch(repoOwner, repository, head.getName()).getCommit().getId();
                listener.getLogger().format("Current revision of branch %s is %s%n", head.getName(), revision);
                return new BranchSCMRevision((BranchSCMHead) head, revision);
            } else if (head instanceof PullRequestSCMHead) {
                PullRequestSCMHead h = (PullRequestSCMHead) head;
                listener.getLogger().format("Querying the current revision of pull request #%s...%n", h.getId());
                GiteaPullRequest pr =
                        c.fetchPullRequest(repoOwner, repository, Long.parseLong(h.getId()));
                if (pr.getState() == GiteaIssueState.OPEN) {
                    listener.getLogger().format("Current revision of pull request #%s is %s%n",
                            h.getId(), pr.getHead().getSha());
                    return new PullRequestSCMRevision(
                            h,
                            new BranchSCMRevision(
                                    h.getTarget(),
                                    pr.getBase().getSha()
                            ),
                            new BranchSCMRevision(
                                    new BranchSCMHead(h.getOriginName()),
                                    pr.getHead().getSha()
                            )
                    );
                } else {
                    listener.getLogger().format("Pull request #%s is CLOSED%n", h.getId());
                    return null;
                }
            } else {
                listener.getLogger().format("Unknown head: %s of type %s%n", head.getName(), head.getClass().getName());
                return null;
            }
        }
    }

    @Override
    protected void retrieve(SCMSourceCriteria criteria, @NonNull SCMHeadObserver observer, SCMHeadEvent<?> event,
                            @NonNull final TaskListener listener) throws IOException, InterruptedException {
        try (GiteaConnection c = gitea().open()) {
            listener.getLogger().format("Looking up repository %s/%s%n", repoOwner, repository);
            giteaRepository = c.fetchRepository(repoOwner, repository);
            sshRemote = giteaRepository.getSshUrl();
            if (giteaRepository.isEmpty()) {
                listener.getLogger().format("Repository %s is empty%n", repository);
                return;
            }
            try (GiteaSCMSourceRequest request = new GiteaSCMSourceContext(criteria, observer)
                    .withTraits(getTraits())
                    .newRequest(this, listener)) {
                request.setConnection(c);
                if (request.isFetchBranches()) {
                    request.setBranches(c.fetchBranches(giteaRepository));
                }
                if (request.isFetchPRs()) {
                    if (giteaRepository.isMirror()) {
                        listener.getLogger().format("%n  Ignoring pull requests as repository is a mirror...%n");
                    } else {
                        request.setPullRequests(c.fetchPullRequests(giteaRepository));
                    }
                }
                // TODO if (request.isFetchTags()) { ... }

                if (request.isFetchBranches()) {
                    int count = 0;
                    listener.getLogger().format("%n  Checking branches...%n");
                    for (final GiteaBranch b : c.fetchBranches(giteaRepository)) {
                        count++;
                        listener.getLogger().format("%n    Checking branch %s%n",
                                HyperlinkNote.encodeTo(
                                        UriTemplate.buildFromTemplate(giteaRepository.getHtmlUrl())
                                                .literal("/src")
                                                .path("branch")
                                                .build()
                                                .set("branch", b.getName())
                                                .expand(),
                                        b.getName()
                                )
                        );
                        if (request.process(new BranchSCMHead(b.getName()),
                                new SCMSourceRequest.RevisionLambda<BranchSCMHead, BranchSCMRevision>() {
                                    @NonNull
                                    @Override
                                    public BranchSCMRevision create(@NonNull BranchSCMHead head)
                                            throws IOException, InterruptedException {
                                        return new BranchSCMRevision(head, b.getCommit().getId());
                                    }
                                }, new SCMSourceRequest.ProbeLambda<BranchSCMHead, BranchSCMRevision>() {
                                    @NonNull
                                    @Override
                                    public SCMSourceCriteria.Probe create(@NonNull BranchSCMHead head,
                                                                          @Nullable BranchSCMRevision revision)
                                            throws IOException, InterruptedException {
                                        return createProbe(head, revision);
                                    }
                                }, new SCMSourceRequest.Witness() {
                                    @Override
                                    public void record(@NonNull SCMHead head, SCMRevision revision, boolean isMatch) {
                                        if (isMatch) {
                                            listener.getLogger().format("    Met criteria%n");
                                        } else {
                                            listener.getLogger().format("    Does not meet criteria%n");
                                        }
                                    }
                                })) {
                            listener.getLogger().format("%n  %d branches were processed (query completed)%n", count);
                            return;
                        }
                    }
                    listener.getLogger().format("%n  %d branches were processed%n", count);
                }
                if (request.isFetchPRs() && !giteaRepository.isMirror() && !(request.getForkPRStrategies().isEmpty()
                        && request.getOriginPRStrategies().isEmpty())) {
                    int count = 0;
                    listener.getLogger().format("%n  Checking pull requests...%n");
                    for (final GiteaPullRequest p : c
                            .fetchPullRequests(giteaRepository, EnumSet.of(GiteaIssueState.OPEN))) {
                        if (p == null) {
                            continue;
                        }
                        count++;
                        listener.getLogger().format("%n  Checking pull request %s%n",
                                HyperlinkNote.encodeTo(
                                        UriTemplate.buildFromTemplate(giteaRepository.getHtmlUrl())
                                                .literal("/pulls")
                                                .path("number")
                                                .build()
                                                .set("number", p.getNumber())
                                                .expand(),
                                        "#" + p.getNumber()
                                )
                        );
                        String originOwner = p.getHead().getRepo().getOwner().getUsername();
                        String originRepository = p.getHead().getRepo().getName();
                        Set<ChangeRequestCheckoutStrategy> strategies = request.getPRStrategies(
                                !StringUtils.equalsIgnoreCase(repoOwner, originOwner)
                                        && StringUtils.equalsIgnoreCase(repository, originRepository)
                        );
                        for (ChangeRequestCheckoutStrategy strategy : strategies) {
                            if (request.process(new PullRequestSCMHead(
                                            "PR-" + p.getNumber() + (strategies.size() > 1 ? "-" + strategy.name()
                                                    .toLowerCase(Locale.ENGLISH) : ""),
                                            p.getNumber(),
                                            new BranchSCMHead(p.getBase().getRef()),
                                            strategy,
                                            StringUtils.equalsIgnoreCase(originOwner, repoOwner)
                                                    && StringUtils.equalsIgnoreCase(originRepository, repository)
                                                    ? SCMHeadOrigin.DEFAULT
                                                    : new SCMHeadOrigin.Fork(originOwner + "/" + originRepository),
                                            originOwner,
                                            originRepository,
                                            p.getHead().getRef()),
                                    new SCMSourceRequest.RevisionLambda<PullRequestSCMHead, PullRequestSCMRevision>() {
                                        @NonNull
                                        @Override
                                        public PullRequestSCMRevision create(@NonNull PullRequestSCMHead head)
                                                throws IOException, InterruptedException {
                                            return new PullRequestSCMRevision(
                                                    head,
                                                    new BranchSCMRevision(
                                                            head.getTarget(),
                                                            p.getBase().getSha()
                                                    ),
                                                    new BranchSCMRevision(
                                                            new BranchSCMHead(head.getOriginName()),
                                                            p.getHead().getSha()
                                                    )
                                            );
                                        }
                                    },
                                    new SCMSourceRequest.ProbeLambda<PullRequestSCMHead, PullRequestSCMRevision>() {
                                        @NonNull
                                        @Override
                                        public SCMSourceCriteria.Probe create(@NonNull PullRequestSCMHead h,
                                                                              @Nullable PullRequestSCMRevision r)
                                                throws IOException, InterruptedException {
                                            return createProbe(h, r);
                                        }
                                    }, new SCMSourceRequest.Witness() {
                                        @Override
                                        public void record(@NonNull SCMHead head, SCMRevision revision,
                                                           boolean isMatch) {
                                            if (isMatch) {
                                                listener.getLogger().format("    Met criteria%n");
                                            } else {
                                                listener.getLogger().format("    Does not meet criteria%n");
                                            }
                                        }
                                    }
                            )) {
                                listener.getLogger()
                                        .format("%n  %d pull requests were processed (query completed)%n", count);
                                return;
                            }

                        }
                    }
                    listener.getLogger().format("%n  %d pull requests were processed%n", count);
                }
            }
        }
    }

    @Override
    protected SCMRevision retrieve(@NonNull String thingName, @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        SCMHeadObserver.Named baptist = SCMHeadObserver.named(thingName);
        retrieve(null, baptist, null, listener);
        return baptist.result();
    }

    @NonNull
    @Override
    protected Set<String> retrieveRevisions(@NonNull TaskListener listener) throws IOException, InterruptedException {
        // don't pass through to git, instead use the super.super behaviour
        Set<String> revisions = new HashSet<String>();
        for (SCMHead head : retrieve(listener)) {
            revisions.add(head.getName());
        }
        return revisions;
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(SCMSourceEvent event, @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        if (giteaRepository == null) {
            try (GiteaConnection c = gitea().open()) {
                listener.getLogger().format("Looking up repository %s/%s%n", repoOwner, repository);
                giteaRepository = c.fetchRepository(repoOwner, repository);
            }
        }
        List<Action> result = new ArrayList<>();
        result.add(new ObjectMetadataAction(null, giteaRepository.getDescription(), giteaRepository.getWebsite()));
        result.add(new GiteaLink("icon-gitea-repo", UriTemplate.buildFromTemplate(serverUrl)
                .path(UriTemplateBuilder.var("owner"))
                .path(UriTemplateBuilder.var("repository"))
                .build()
                .set("owner", repoOwner)
                .set("repository", repository)
                .expand()
        ));
        return result;
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@NonNull SCMHead head, SCMHeadEvent event, @NonNull TaskListener listener)
            throws IOException, InterruptedException {
        if (giteaRepository == null) {
            try (GiteaConnection c = gitea().open()) {
                listener.getLogger().format("Looking up repository %s/%s%n", repoOwner, repository);
                giteaRepository = c.fetchRepository(repoOwner, repository);
            }
        }
        List<Action> result = new ArrayList<>();
        if (head instanceof BranchSCMHead) {
            String branchUrl = UriTemplate.buildFromTemplate(serverUrl)
                    .path(UriTemplateBuilder.var("owner"))
                    .path(UriTemplateBuilder.var("repository"))
                    .path("src")
                    .path(UriTemplateBuilder.var("branch"))
                    .build()
                    .set("owner", repoOwner)
                    .set("repository", repository)
                    .set("branch", head.getName())
                    .expand();
            result.add(new ObjectMetadataAction(
                    null,
                    null,
                    branchUrl
            ));
            result.add(new GiteaLink("icon-gitea-branch", branchUrl));
            if (head.getName().equals(giteaRepository.getDefaultBranch())) {
                result.add(new PrimaryInstanceMetadataAction());
            }
        } else if (head instanceof PullRequestSCMHead) {
            String pullUrl = UriTemplate.buildFromTemplate(serverUrl)
                    .path(UriTemplateBuilder.var("owner"))
                    .path(UriTemplateBuilder.var("repository"))
                    .path("pulls")
                    .path(UriTemplateBuilder.var("id"))
                    .build()
                    .set("owner", repoOwner)
                    .set("repository", repository)
                    .set("id", ((PullRequestSCMHead) head).getId())
                    .expand();
            result.add(new ObjectMetadataAction(
                    null,
                    null,
                    pullUrl
            ));
            result.add(new GiteaLink("icon-gitea-branch", pullUrl));
        }
        return result;
    }

    @NonNull
    @Override
    public SCM build(@NonNull SCMHead head, SCMRevision revision) {
        return new GiteaSCMBuilder(this, head, revision).withTraits(traits).build();
    }

    @NonNull
    @Override
    protected List<Action> retrieveActions(@NonNull SCMRevision revision, SCMHeadEvent event,
                                           @NonNull TaskListener listener) throws IOException, InterruptedException {
        return super.retrieveActions(revision, event, listener);
    }

    @NonNull
    @Override
    protected SCMProbe createProbe(@NonNull final SCMHead head, SCMRevision revision) throws IOException {
        try {
            GiteaSCMFileSystem.BuilderImpl builder =
                    ExtensionList.lookup(SCMFileSystem.Builder.class).get(GiteaSCMFileSystem.BuilderImpl.class);
            if (builder == null) {
                throw new AssertionError();
            }
            final SCMFileSystem fs = builder.build(this, head, revision);
            return new SCMProbe() {
                @NonNull
                @Override
                public SCMProbeStat stat(@NonNull String path) throws IOException {
                    try {
                        return SCMProbeStat.fromType(fs.child(path).getType());
                    } catch (InterruptedException e) {
                        throw new IOException("Interrupted", e);
                    }
                }

                @Override
                public void close() throws IOException {
                    fs.close();
                }

                @Override
                public String name() {
                    return head.getName();
                }

                @Override
                public long lastModified() {
                    try {
                        return fs.lastModified();
                    } catch (IOException e) {
                        return 0L;
                    } catch (InterruptedException e) {
                        return 0L;
                    }
                }

                @Override
                public SCMFile getRoot() {
                    return fs.getRoot();
                }
            };
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void afterSave() {
        WebhookRegistration mode = new GiteaSCMSourceContext(null, SCMHeadObserver.none())
                .withTraits(new GiteaSCMNavigatorContext().withTraits(traits).traits())
                .webhookRegistration();
        GiteaWebhookListener.register(getOwner(), this, mode, credentialsId);
    }

    /*package*/ Gitea gitea() throws AbortException {
        GiteaServer server = GiteaServers.get().findServer(serverUrl);
        if (server == null) {
            throw new AbortException("Unknown server: " + serverUrl);
        }
        StandardCredentials credentials = credentials();
        SCMSourceOwner owner = getOwner();
        if (owner != null) {
            CredentialsProvider.track(owner, credentials);
        }
        return Gitea.server(serverUrl)
                .as(AuthenticationTokens.convert(GiteaAuth.class, credentials));
    }

    public StandardCredentials credentials() {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StandardCredentials.class,
                        getOwner(),
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
    public static class DescriptorImpl extends SCMSourceDescriptor {

        static {
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-org icon-sm",
                            "plugin/gitea/images/16x16/gitea-org.png",
                            Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-org icon-md",
                            "plugin/gitea/images/24x24/gitea-org.png",
                            Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-org icon-lg",
                            "plugin/gitea/images/32x32/gitea-org.png",
                            Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-org icon-xlg",
                            "plugin/gitea/images/48x48/gitea-org.png",
                            Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(
                    new Icon("icon-gitea-logo icon-sm",
                            "plugin/gitea/images/16x16/gitea.png",
                            Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-logo icon-md",
                            "plugin/gitea/images/24x24/gitea.png",
                            Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-logo icon-lg",
                            "plugin/gitea/images/32x32/gitea.png",
                            Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-logo icon-xlg",
                            "plugin/gitea/images/48x48/gitea.png",
                            Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(
                    new Icon("icon-gitea-repo icon-sm",
                            "plugin/gitea/images/16x16/gitea-repo.png",
                            Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-repo icon-md",
                            "plugin/gitea/images/24x24/gitea-repo.png",
                            Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-repo icon-lg",
                            "plugin/gitea/images/32x32/gitea-repo.png",
                            Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-repo icon-xlg",
                            "plugin/gitea/images/48x48/gitea-repo.png",
                            Icon.ICON_XLARGE_STYLE));

            IconSet.icons.addIcon(
                    new Icon("icon-gitea-branch icon-sm",
                            "plugin/gitea/images/16x16/gitea-branch.png",
                            Icon.ICON_SMALL_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-branch icon-md",
                            "plugin/gitea/images/24x24/gitea-branch.png",
                            Icon.ICON_MEDIUM_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-branch icon-lg",
                            "plugin/gitea/images/32x32/gitea-branch.png",
                            Icon.ICON_LARGE_STYLE));
            IconSet.icons.addIcon(
                    new Icon("icon-gitea-branch icon-xlg",
                            "plugin/gitea/images/48x48/gitea-branch.png",
                            Icon.ICON_XLARGE_STYLE));
        }

        @Override
        public String getDisplayName() {
            return "Gitea";
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
                throws IOException, InterruptedException{
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
                return FormValidation.error(Messages.GiteaSCMSource_selectedCredentialsMissing());
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillRepositoryItems(@AncestorInPath SCMSourceOwner context,
                                                  @QueryParameter String serverUrl,
                                                  @QueryParameter String credentialsId,
                                                  @QueryParameter String repoOwner,
                                                  @QueryParameter String repository) throws IOException,
                InterruptedException {
            ListBoxModel result = new ListBoxModel();
            if (context == null) {
                if (!Jenkins.getActiveInstance().hasPermission(Jenkins.ADMINISTER)) {
                    // must have admin if you want the list without a context
                    result.add(repository);
                    return result;
                }
            } else {
                if (!context.hasPermission(Item.EXTENDED_READ)
                        && !context.hasPermission(CredentialsProvider.USE_ITEM)) {
                    // must be able to read the configuration or use the item credentials if you want the list
                    result.add(repository);
                    return result;
                }
            }
            if (StringUtils.isBlank(repoOwner)) {
                result.add(repository);
                return result;
            }
            GiteaServer server = GiteaServers.get().findServer(serverUrl);
            if (server == null) {
                // you can only get the list for registered servers
                result.add(repository);
                return result;
            }
            StandardCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            StandardCredentials.class,
                            context,
                            context instanceof Queue.Task ?
                                    Tasks.getDefaultAuthenticationOf((Queue.Task) context)
                                    : ACL.SYSTEM,
                            URIRequirementBuilder.fromUri(serverUrl).build()
                    ),
                    CredentialsMatchers.allOf(
                            AuthenticationTokens.matcher(GiteaAuth.class),
                            CredentialsMatchers.withId(credentialsId)
                    )
            );
            try (GiteaConnection c = Gitea.server(serverUrl)
                    .as(AuthenticationTokens.convert(GiteaAuth.class, credentials))
                    .open()) {
                for (GiteaRepository r : c.fetchRepositories(repoOwner)) {
                    result.add(r.getName());
                }
                return result;
            } catch (IOException e) {
                // TODO once enhanced <f:select> that can handle error responses, just throw
                LOGGER.log(Level.FINE, "Could not populate repositories", e);
                if (result.isEmpty()) {
                    result.add(repository);
                }
                return result;
            }
        }

        public List<NamedArrayList<? extends SCMTraitDescriptor<?>>> getTraitsDescriptorLists() {
            List<SCMTraitDescriptor<?>> all = new ArrayList<>();
            //all.addAll(SCMSourceTrait._for(this, GitHubSCMSourceContext.class, null));
            all.addAll(SCMSourceTrait._for(this, null, GiteaSCMBuilder.class));
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
            NamedArrayList.select(all, Messages.GiteaSCMSource_traitSection_withinRepo(), NamedArrayList
                            .anyOf(NamedArrayList.withAnnotation(Discovery.class),
                                    NamedArrayList.withAnnotation(Selection.class)),
                    true, result);
            NamedArrayList.select(all, Messages.GiteaSCMSource_traitSection_additional(), null, true, result);
            return result;
        }

        public List<SCMSourceTrait> getTraitsDefaults() {
            return Arrays.<SCMSourceTrait>asList( // TODO finalize
                    new BranchDiscoveryTrait(true, false),
                    new OriginPullRequestDiscoveryTrait(EnumSet.of(ChangeRequestCheckoutStrategy.MERGE)),
                    new ForkPullRequestDiscoveryTrait(EnumSet.of(ChangeRequestCheckoutStrategy.MERGE),
                            new ForkPullRequestDiscoveryTrait.TrustContributors())
            );
        }

        @Override
        public String getIconClassName() {
            return "icon-gitea-repo";
        }

        @NonNull
        @Override
        protected SCMHeadCategory[] createCategories() {
            return new SCMHeadCategory[]{
                    new UncategorizedSCMHeadCategory(Messages._GiteaSCMSource_UncategorizedCategory()),
                    new ChangeRequestSCMHeadCategory(Messages._GiteaSCMSource_ChangeRequestCategory())
                    // TODO add support for tags and maybe feature branch identification
            };
        }
    }
}
