package org.jenkinsci.plugin.gitea;

import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.impl.mock.MockSCMNavigator;
import jenkins.scm.impl.mock.MockSCMSource;
import org.jenkinsci.plugin.gitea.client.api.GiteaCreateEvent;
import org.jenkinsci.plugin.gitea.client.api.GiteaOwner;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;

public class GiteaCreateSCMEventTest {

    GiteaSCMSource giteaSCMSource = new GiteaSCMSource("", "", "");
    MockSCMSource scmSource = new MockSCMSource("controllerId", "giteaRepo", new ArrayList<>());
    MockSCMNavigator scmNavigator = new MockSCMNavigator("controllerId", new ArrayList<>());
    GiteaCreateSCMEvent branchCreateEvent = new GiteaCreateSCMEvent(withGiteaCreateEvent("feature/branch", "branch"), "?");
    GiteaCreateSCMEvent tagCreateEvent = new GiteaCreateSCMEvent(withGiteaCreateEvent("mytag", "tag"), "?");

    @Test
    public void descriptionForSCMNavigator_withBranchCreateEvent() {
        assertThat(branchCreateEvent.descriptionFor(scmNavigator), is("Create event for branch feature/branch in repository giteaRepo"));
    }

    @Test
    public void descriptionForSCMNavigator_withTagCreateEvent() {
        assertThat(tagCreateEvent.descriptionFor(scmNavigator), is("Create event for tag mytag in repository giteaRepo"));
    }

    @Test
    public void descriptionForSCMSource_withBranchCreateEvent() {
        assertThat(branchCreateEvent.descriptionFor(scmSource), is("Create event for branch feature/branch"));
    }

    @Test
    public void descriptionForSCMSource_withTagCreateEvent() {
        assertThat(tagCreateEvent.descriptionFor(scmSource), is("Create event for tag mytag"));
    }

    @Test
    public void description_withBranchCreateEvent() {
        assertThat(branchCreateEvent.description(), is("Create event for branch feature/branch in repository ownerUser/giteaRepo"));
    }

    @Test
    public void description_withTagCreateEvent() {
        assertThat(tagCreateEvent.description(), is("Create event for tag mytag in repository ownerUser/giteaRepo"));
    }

    @Test
    public void headsFor_withBranchCreateEvent() {
        Map<SCMHead, SCMRevision> headsFor = branchCreateEvent.headsFor(giteaSCMSource);

        assertThat(headsFor.size(), is(1));
        SCMHead scmHead = headsFor.keySet().iterator().next();
        assertThat(scmHead.getName(), is("feature/branch"));
        SCMRevision scmRevision = headsFor.values().iterator().next();
        assertThat(((BranchSCMRevision) scmRevision).getHash(), is("12345"));

    }

    @Test
    public void headsFor_withTagCreateEvent() {
        Map<SCMHead, SCMRevision> headsFor = tagCreateEvent.headsFor(giteaSCMSource);

        assertThat(headsFor.size(), is(1));
        SCMHead scmHead = headsFor.keySet().iterator().next();
        assertThat(scmHead.getName(), is("mytag"));
        SCMRevision scmRevision = headsFor.values().iterator().next();
        assertThat(((TagSCMRevision) scmRevision).getHash(), is("12345"));
    }

    GiteaCreateEvent withGiteaCreateEvent(String ref, String refType) {
        GiteaCreateEvent event = new GiteaCreateEvent();
        event.setSha("12345");
        event.setRef(ref);
        event.setRefType(refType);
        GiteaRepository repository = new GiteaRepository();
        repository.setName("giteaRepo");
        GiteaOwner owner = new GiteaOwner();
        owner.setUsername("ownerUser");
        repository.setOwner(owner);
        event.setRepository(repository);
        return event;
    }
}