package org.jenkinsci.plugin.gitea.client.mock;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import org.jenkinsci.plugin.gitea.client.api.GiteaAnnotatedTag;
import org.jenkinsci.plugin.gitea.client.api.GiteaBranch;
import org.jenkinsci.plugin.gitea.client.api.GiteaCommitDetail;
import org.jenkinsci.plugin.gitea.client.api.GiteaCommitStatus;
import org.jenkinsci.plugin.gitea.client.api.GiteaConnection;
import org.jenkinsci.plugin.gitea.client.api.GiteaHook;
import org.jenkinsci.plugin.gitea.client.api.GiteaIssue;
import org.jenkinsci.plugin.gitea.client.api.GiteaIssueState;
import org.jenkinsci.plugin.gitea.client.api.GiteaObject;
import org.jenkinsci.plugin.gitea.client.api.GiteaOrganization;
import org.jenkinsci.plugin.gitea.client.api.GiteaOwner;
import org.jenkinsci.plugin.gitea.client.api.GiteaPullRequest;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.jenkinsci.plugin.gitea.client.api.GiteaTag;
import org.jenkinsci.plugin.gitea.client.api.GiteaUser;
import org.jenkinsci.plugin.gitea.client.api.GiteaVersion;

public class MockGiteaConnection implements GiteaConnection {

    private final AtomicLong nextId = new AtomicLong();
    private final String user;
    private final Map<String, GiteaUser> users = new TreeMap<>();
    private final Map<String, GiteaOrganization> organizations = new TreeMap<>();
    private final Map<String, GiteaRepository> repositories = new TreeMap<>();
    private final Map<String, Map<String, GiteaBranch>> branches = new TreeMap<>();
    private final Map<String, Map<Long, GiteaPullRequest>> pulls = new TreeMap<>();
    private final Map<String, Map<Long, GiteaIssue>> issues = new TreeMap<>();
    private final Map<String, Set<String>> collaborators = new TreeMap<>();
    private final Map<String, List<GiteaHook>> orgHooks = new TreeMap<>();
    private final Map<String, List<GiteaHook>> repoHooks = new TreeMap<>();
    private final Map<String, Map<String, Map<String,byte[]>>> files = new TreeMap<>();

    public MockGiteaConnection(String user) {
        this.user = user;
    }

    public MockGiteaConnection withUser(GiteaUser user) {
        GiteaUser clone = user.clone();
        clone.setId(nextId.incrementAndGet());
        this.users.put(user.getUsername(), clone);
        return this;
    }

    public MockGiteaConnection withOrg(GiteaOrganization org) {
        GiteaOrganization clone = org.clone();
        clone.setId(nextId.incrementAndGet());
        this.organizations.put(org.getUsername(), clone);
        this.orgHooks.put(org.getUsername(), new ArrayList<GiteaHook>());
        return this;
    }

    public MockGiteaConnection withRepo(GiteaRepository repo) {
        GiteaRepository clone = repo.clone();
        clone.setId(nextId.incrementAndGet());
        clone.setCreatedAt(new Date());
        clone.setUpdatedAt(new Date());
        this.repositories.put(keyOf(clone), clone);
        this.repoHooks.put(keyOf(clone), new ArrayList<GiteaHook>());
        this.branches.put(keyOf(clone), new HashMap<String, GiteaBranch>());
        this.pulls.put(keyOf(clone), new HashMap<Long, GiteaPullRequest>());
        this.issues.put(keyOf(clone), new HashMap<Long, GiteaIssue>());
        this.collaborators.put(keyOf(clone), new TreeSet<String>());
        this.files.put(keyOf(clone), new TreeMap<String, Map<String, byte[]>>());
        return this;
    }

    public MockGiteaConnection withBranch(GiteaRepository repo, GiteaBranch branch) {
        GiteaBranch clone = branch.clone();
        this.branches.get(keyOf(repo)).put(clone.getName(), clone);
        Map<String, byte[]> content = this.files.get(keyOf(repo)).get(clone.getCommit().getId());
        if (content == null) {
            this.files.get(keyOf(repo)).put(clone.getCommit().getId(), new HashMap<String, byte[]>());
        }
        return this;
    }

    public MockGiteaConnection withPull(GiteaRepository repo, GiteaPullRequest pull) {
        GiteaPullRequest clone = pull.clone();
        clone.setId(nextId.incrementAndGet());
        clone.setNumber(clone.getId());
        clone.setCreatedAt(new Date());
        clone.setUpdatedAt(new Date());
        pulls.get(keyOf(repo)).put(clone.getId(), clone);
        Map<String, byte[]> content = this.files.get(keyOf(repo)).get(pull.getHead().getSha());
        if (content == null) {
            this.files.get(keyOf(repo)).put(pull.getHead().getSha(), new HashMap<String, byte[]>());
        }
        content = this.files.get(keyOf(repo)).get(pull.getBase().getSha());
        if (content == null) {
            this.files.get(keyOf(repo)).put(pull.getBase().getSha(), new HashMap<String, byte[]>());
        }
        GiteaIssue issue = new GiteaIssue();
        issue.setUrl(clone.getUrl());
        issue.setNumber(clone.getNumber());
        issue.setUser(clone.getUser());
        issue.setTitle(clone.getTitle());
        issue.setBody(clone.getBody());
        issue.setLabels(clone.getLabels());
        issue.setMilestone(clone.getMilestone());
        issue.setAssignee(clone.getAssignee());
        issue.setState(clone.getState());
        issue.setComments(clone.getComments());
        issue.setCreatedAt(clone.getCreatedAt());
        issue.setUpdatedAt(clone.getUpdatedAt());
        GiteaIssue.PullSummary summary = new GiteaIssue.PullSummary();
        summary.setMerged(clone.isMerged());
        summary.setMergedAt(clone.getMergedAt());
        issue.setPullRequest(summary);
        issues.get(keyOf(repo)).put(issue.getId(), issue);
        return this;
    }

    public MockGiteaConnection withIssue(GiteaRepository repo, GiteaIssue issue) {
        GiteaIssue clone = issue.clone();
        clone.setId(nextId.incrementAndGet());
        clone.setNumber(clone.getId());
        clone.setCreatedAt(new Date());
        clone.setUpdatedAt(new Date());
        issues.get(keyOf(repo)).put(issue.getId(), issue);
        return this;
    }

    @Override
    public GiteaVersion fetchVersion() throws IOException, InterruptedException {
        GiteaVersion result = new GiteaVersion();
        result.setVersion("mock");
        return result;
    }

    @Override
    public GiteaUser fetchCurrentUser() throws IOException, InterruptedException {
        return notFoundIfNull(users.get(user)).clone();
    }

    @Override
    public GiteaOwner fetchOwner(String name) throws IOException, InterruptedException {
        GiteaOrganization organization = organizations.get(name);
        return organization != null ? organization : notFoundIfNull(users.get(name));
    }

    @Override
    public GiteaUser fetchUser(String name) throws IOException, InterruptedException {
        return notFoundIfNull(users.get(name)).clone();
    }

    @Override
    public GiteaOrganization fetchOrganization(String name) throws IOException, InterruptedException {
        return notFoundIfNull(organizations.get(name)).clone();
    }

    @Override
    public GiteaRepository fetchRepository(String username, String name) throws IOException, InterruptedException {
        return notFoundIfNull(repositories.get(keyOf(username, name))).clone();
    }

    @Override
    public GiteaRepository fetchRepository(GiteaOwner owner, String name) throws IOException, InterruptedException {
        return fetchRepository(owner.getUsername(), name);
    }

    @Override
    public List<GiteaRepository> fetchCurrentUserRepositories() throws IOException, InterruptedException {
        return fetchRepositories(user);
    }

    @Override
    public List<GiteaRepository> fetchRepositories(String username) throws IOException, InterruptedException {
        if (organizations.containsKey(username) || users.containsKey(username)) {
            List<GiteaRepository> result = new ArrayList<>();
            for (Map.Entry<String, GiteaRepository> entry : repositories.entrySet()) {
                if (entry.getKey().startsWith(username + "/")) {
                    result.add(entry.getValue().clone());
                }
            }
            return result;
        }
        return notFoundIfNull(null);
    }

    @Override
    public List<GiteaRepository> fetchRepositories(GiteaOwner owner) throws IOException, InterruptedException {
        return fetchRepositories(owner.getUsername());
    }

    @Override
    public List<GiteaRepository> fetchOrganizationRepositories(GiteaOwner owner) throws IOException, InterruptedException {
        return fetchRepositories(owner.getUsername());
    }

    @Override
    public GiteaBranch fetchBranch(String username, String repository, String name)
            throws IOException, InterruptedException {
        return notFoundIfNull(notFoundIfNull(branches.get(keyOf(username, repository))).get(name)).clone();
    }

    @Override
    public GiteaBranch fetchBranch(GiteaRepository repository, String name) throws IOException, InterruptedException {
        return fetchBranch(repository.getOwner().getUsername(), repository.getName(), name);
    }

    @Override
    public List<GiteaBranch> fetchBranches(String username, String name) throws IOException, InterruptedException {
        return clone(notFoundIfNull(branches.get(keyOf(username, name)).values()));
    }

    @Override
    public List<GiteaBranch> fetchBranches(GiteaRepository repository) throws IOException, InterruptedException {
        return fetchBranches(repository.getOwner().getUsername(), repository.getName());
    }

    @Override
    public List<GiteaUser> fetchCollaborators(String username, String name) throws IOException, InterruptedException {
        List<GiteaUser> result = new ArrayList<>();
        for (String user : notFoundIfNull(collaborators.get(keyOf(username, name)))) {
            GiteaUser u = users.get(user);
            if (u != null) {
                result.add(u.clone());
            }
        }
        return result;
    }

    @Override
    public List<GiteaUser> fetchCollaborators(GiteaRepository repository) throws IOException, InterruptedException {
        return fetchCollaborators(repository.getOwner().getUsername(), repository.getName());
    }

    @Override
    public boolean checkCollaborator(String username, String name, String collaboratorName)
            throws IOException, InterruptedException {
        return notFoundIfNull(collaborators.get(keyOf(username, name))).contains(collaboratorName);
    }

    @Override
    public boolean checkCollaborator(GiteaRepository repository, String collaboratorName)
            throws IOException, InterruptedException {
        return checkCollaborator(repository.getOwner().getUsername(), repository.getName(), collaboratorName);
    }

    @Override
    public List<GiteaHook> fetchHooks(String organizationName) throws IOException, InterruptedException {
        return clone(notFoundIfNull(orgHooks.get(organizationName)));
    }

    @Override
    public List<GiteaHook> fetchHooks(GiteaOrganization organization) throws IOException, InterruptedException {
        return fetchHooks(organization.getUsername());
    }

    @Override
    public GiteaHook createHook(GiteaOrganization organization, GiteaHook hook)
            throws IOException, InterruptedException {
        List<GiteaHook> list = notFoundIfNull(orgHooks.get(organization));
        hook = hook.clone();
        hook.setId(nextId.incrementAndGet());
        hook.setCreatedAt(new Date());
        hook.setUpdatedAt(hook.getCreatedAt());
        list.add(hook);
        return hook.clone();
    }

    @Override
    public void deleteHook(GiteaOrganization organization, GiteaHook hook) throws IOException, InterruptedException {
        deleteHook(organization, hook.getId());
    }

    @Override
    public void deleteHook(GiteaOrganization organization, long id) throws IOException, InterruptedException {
        GiteaHook target = null;
        for (Iterator<GiteaHook> iterator = notFoundIfNull(orgHooks.get(organization)).iterator();
             iterator.hasNext(); ) {
            GiteaHook h = iterator.next();
            if (h.getId() == id) {
                iterator.remove();
                target = h;
                break;
            }
        }
        notFoundIfNull(target);
    }

    @Override
    public void updateHook(GiteaOrganization organization, GiteaHook hook) throws IOException, InterruptedException {
        GiteaHook target = null;
        for (GiteaHook h : notFoundIfNull(orgHooks.get(organization))) {
            if (h.getId() == hook.getId()) {
                target = h;
                break;
            }
        }
        notFoundIfNull(target);
        target.setUpdatedAt(new Date());
        target.setEvents(hook.getEvents());
        target.setActive(hook.isActive());
        target.setConfig(hook.getConfig());
    }

    @Override
    public List<GiteaHook> fetchHooks(String username, String name) throws IOException, InterruptedException {
        return clone(notFoundIfNull(repoHooks.get(keyOf(username, name))));
    }

    @Override
    public List<GiteaHook> fetchHooks(GiteaRepository repository) throws IOException, InterruptedException {
        return fetchHooks(repository.getOwner().getUsername(), repository.getName());
    }

    @Override
    public GiteaHook createHook(GiteaRepository repository, GiteaHook hook) throws IOException, InterruptedException {
        List<GiteaHook> list =
                notFoundIfNull(repoHooks.get(keyOf(repository)));
        hook = hook.clone();
        hook.setId(nextId.incrementAndGet());
        hook.setCreatedAt(new Date());
        hook.setUpdatedAt(hook.getCreatedAt());
        list.add(hook);
        return hook.clone();
    }

    @Override
    public void deleteHook(GiteaRepository repository, GiteaHook hook) throws IOException, InterruptedException {
        deleteHook(repository, hook.getId());

    }

    @Override
    public void deleteHook(GiteaRepository repository, long id) throws IOException, InterruptedException {
        GiteaHook target = null;
        for (Iterator<GiteaHook> iterator = notFoundIfNull(
                repoHooks.get(keyOf(repository))).iterator();
             iterator.hasNext(); ) {
            GiteaHook h = iterator.next();
            if (h.getId() == id) {
                iterator.remove();
                target = h;
                break;
            }
        }
        notFoundIfNull(target);
    }

    @Override
    public void updateHook(GiteaRepository repository, GiteaHook hook) throws IOException, InterruptedException {
        GiteaHook target = null;
        for (GiteaHook h : notFoundIfNull(
                repoHooks.get(keyOf(repository)))) {
            if (h.getId() == hook.getId()) {
                target = h;
                break;
            }
        }
        notFoundIfNull(target);
        target.setUpdatedAt(new Date());
        target.setEvents(hook.getEvents());
        target.setActive(hook.isActive());
        target.setConfig(hook.getConfig());
    }

    @Override
    public List<GiteaCommitStatus> fetchCommitStatuses(GiteaRepository repository, String sha)
            throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public GiteaCommitStatus createCommitStatus(String username, String repository, String sha,
                                                GiteaCommitStatus status) throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public GiteaCommitStatus createCommitStatus(GiteaRepository repository, String sha, GiteaCommitStatus status)
            throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public GiteaAnnotatedTag fetchAnnotatedTag(String username, String repository, String sha1)
            throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public GiteaAnnotatedTag fetchAnnotatedTag(GiteaRepository repository, GiteaTag tag)
            throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public List<GiteaTag> fetchTags(String username, String name) throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public List<GiteaTag> fetchTags(GiteaRepository repository) throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public GiteaCommitDetail fetchCommit(String username, String repository, String sha1)
            throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public GiteaCommitDetail fetchCommit(GiteaRepository repository, String sha1)
            throws IOException, InterruptedException {
        // TODO
        return null;
    }

    @Override
    public GiteaPullRequest fetchPullRequest(String username, String name, long id)
            throws IOException, InterruptedException {
        return notFoundIfNull(notFoundIfNull(pulls.get(keyOf(username, name))).get(id));
    }

    @Override
    public GiteaPullRequest fetchPullRequest(GiteaRepository repository, long id)
            throws IOException, InterruptedException {
        return fetchPullRequest(repository.getOwner().getUsername(), repository.getName(), id);
    }

    @Override
    public List<GiteaPullRequest> fetchPullRequests(String username, String name)
            throws IOException, InterruptedException {
        return fetchPullRequests(username, name, EnumSet.of(GiteaIssueState.OPEN));
    }

    @Override
    public List<GiteaPullRequest> fetchPullRequests(GiteaRepository repository)
            throws IOException, InterruptedException {
        return fetchPullRequests(repository.getOwner().getUsername(), repository.getName());
    }

    @Override
    public List<GiteaPullRequest> fetchPullRequests(String username, String name, Set<GiteaIssueState> states)
            throws IOException, InterruptedException {
        List<GiteaPullRequest> result = new ArrayList<>();
        for (GiteaPullRequest r : notFoundIfNull(pulls.get(keyOf(username, name))).values()) {
            if (states.contains(r.getState())) {
                result.add(r.clone());
            }
        }
        return result;
    }

    @Override
    public List<GiteaPullRequest> fetchPullRequests(GiteaRepository repository, Set<GiteaIssueState> states)
            throws IOException, InterruptedException {
        return fetchPullRequests(repository.getOwner().getUsername(), repository.getName(), states);
    }

    @Override
    public List<GiteaIssue> fetchIssues(String username, String name) throws IOException, InterruptedException {
        return fetchIssues(username, name, EnumSet.of(GiteaIssueState.OPEN));
    }

    @Override
    public List<GiteaIssue> fetchIssues(GiteaRepository repository) throws IOException, InterruptedException {
        return fetchIssues(repository, EnumSet.of(GiteaIssueState.OPEN));
    }

    @Override
    public List<GiteaIssue> fetchIssues(String username, String name, Set<GiteaIssueState> states)
            throws IOException, InterruptedException {
        List<GiteaIssue> result = new ArrayList<>();
        for (GiteaIssue i : notFoundIfNull(issues.get(keyOf(username, name))).values()) {
            if (states.contains(i.getState())) {
                result.add(i.clone());
            }
        }
        return result;
    }

    @Override
    public List<GiteaIssue> fetchIssues(GiteaRepository repository, Set<GiteaIssueState> states)
            throws IOException, InterruptedException {
        return fetchIssues(repository.getOwner().getUsername(), repository.getName(), states);
    }

    @Override
    public byte[] fetchFile(GiteaRepository repository, String ref, String path)
            throws IOException, InterruptedException {
        return notFoundIfNull(notFoundIfNull(notFoundIfNull(files.get(keyOf(repository))).get(ref)).get(path)).clone();
    }

    @Override
    public boolean checkFile(GiteaRepository repository, String ref, String path)
            throws IOException, InterruptedException {
        return notFoundIfNull(notFoundIfNull(files.get(keyOf(repository))).get(ref)).containsKey(path);
    }

    @Override
    public void close() throws IOException {

    }

    @NonNull
    private static <T> T notFoundIfNull(@CheckForNull T value) throws IOException {
        if (value == null) {
            throw new IOException("HTTP/404 Not Found");
        }
        return value;
    }

    private static <T extends GiteaObject<T>> List<T> clone(Collection<? extends T> items) {
        List<T> result = new ArrayList<>(items.size());
        for (T item : items) {
            result.add(item.clone());
        }
        return result;
    }

    private static String keyOf(String username, String repoName) {
        return username + "/" + repoName;
    }

    private static String keyOf(GiteaRepository repository) {
        return keyOf(repository.getOwner().getUsername(), repository.getName());
    }

}
