package org.jenkinsci.plugin.gitea;

import java.util.ArrayList;
import java.util.Map;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.impl.mock.MockSCMNavigator;
import jenkins.scm.impl.mock.MockSCMSource;
import org.jenkinsci.plugin.gitea.client.api.GiteaDeleteEvent;
import org.jenkinsci.plugin.gitea.client.api.GiteaOwner;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class GiteaDeleteSCMEventTest {

    final GiteaSCMSource giteaSCMSource = new GiteaSCMSource("", "", "");
    final MockSCMSource scmSource = new MockSCMSource("controllerId", "giteaRepo", new ArrayList<>());
    final MockSCMNavigator scmNavigator = new MockSCMNavigator("controllerId", new ArrayList<>());
    final GiteaDeleteSCMEvent branchDeleteEvent = new GiteaDeleteSCMEvent(withGiteaDeleteEvent("feature/branch", "branch"), "?");
    final GiteaDeleteSCMEvent tagDeleteEvent = new GiteaDeleteSCMEvent(withGiteaDeleteEvent("mytag", "tag"), "?");

    @Test
    void descriptionForSCMNavigator_withBranchDeleteEvent() {
        assertThat(branchDeleteEvent.descriptionFor(scmNavigator), is("Delete event for branch feature/branch in repository giteaRepo"));
    }

    @Test
    void descriptionForSCMNavigator_withTagDeleteEvent() {
        assertThat(tagDeleteEvent.descriptionFor(scmNavigator), is("Delete event for tag mytag in repository giteaRepo"));
    }

    @Test
    void descriptionForSCMSource_withBranchDeleteEvent() {
        assertThat(branchDeleteEvent.descriptionFor(scmSource), is("Delete event for branch feature/branch"));
    }

    @Test
    void descriptionForSCMSource_withTagDeleteEvent() {
        assertThat(tagDeleteEvent.descriptionFor(scmSource), is("Delete event for tag mytag"));
    }

    @Test
    void description_withBranchDeleteEvent() {
        assertThat(branchDeleteEvent.description(), is("Delete event for branch feature/branch in repository ownerUser/giteaRepo"));
    }

    @Test
    void description_withTagDeleteEvent() {
        assertThat(tagDeleteEvent.description(), is("Delete event for tag mytag in repository ownerUser/giteaRepo"));
    }

    @Test
    void headsFor_withBranchDeleteEvent() {
        Map<SCMHead, SCMRevision> headsFor = branchDeleteEvent.headsFor(giteaSCMSource);

        assertThat(headsFor.size(), is(1));
        SCMHead scmHead = headsFor.keySet().iterator().next();
        assertThat(scmHead.getName(), is("feature/branch"));
        SCMRevision scmRevision = headsFor.values().iterator().next();
        assertThat(scmRevision, nullValue());

    }

    @Test
    void headsFor_withTagDeleteEvent() {
        Map<SCMHead, SCMRevision> headsFor = tagDeleteEvent.headsFor(giteaSCMSource);

        assertThat(headsFor.size(), is(1));
        SCMHead scmHead = headsFor.keySet().iterator().next();
        assertThat(scmHead.getName(), is("mytag"));
        SCMRevision scmRevision = headsFor.values().iterator().next();
        assertThat(scmRevision, nullValue());
    }

    GiteaDeleteEvent withGiteaDeleteEvent(String ref, String refType) {
        GiteaDeleteEvent event = new GiteaDeleteEvent();
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
