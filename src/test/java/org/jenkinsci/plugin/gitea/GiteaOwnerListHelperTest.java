package org.jenkinsci.plugin.gitea;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jenkinsci.plugin.gitea.client.api.GiteaOwner;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GiteaOwnerListHelperTest {

    @Test
    void getOwners_withValidRepositories_returnsUniqueOwnersIncludingCurrentUser() {
        List<GiteaRepository> repositories = Arrays.asList(
                createMockRepository("alice"),
                createMockRepository("bob"),
                createMockRepository("alice") // duplicate
        );

        Set<String> owners = GiteaOwnerListHelper.getOwners("charlie", repositories);

        assertThat(owners, containsInAnyOrder("alice", "bob", "charlie"));
    }

    @Test
    void getOwners_withEmptyRepositories_returnsOnlyCurrentUser() {
        List<GiteaRepository> repositories = Collections.emptyList();

        Set<String> owners = GiteaOwnerListHelper.getOwners("charlie", repositories);

        assertThat(owners.contains("charlie"), is(true));
    }

    @Test
    void populateOwnerListBoxModel_withCurrentOwnerSelected_placesCurrentOwnerFirst() {
        List<GiteaRepository> repositories = Arrays.asList(
                createMockRepository("alice"),
                createMockRepository("bob"));

        var result = GiteaOwnerListHelper.populateOwnerListBoxModel("charlie", "bob", repositories);

        assertThat(result.size(), is(3));
        assertThat(result.get(0).name, is("bob"));
        assertThat(result.get(1).name, anyOf(is("alice"), is("charlie")));
        assertThat(result.get(2).name, anyOf(is("alice"), is("charlie")));
    }

    @Test
    void populateOwnerListBoxModel_withCurrentOwnerNotInList_includesCurrentOwner() {
        List<GiteaRepository> repositories = Arrays.asList(
                createMockRepository("alice"));

        var result = GiteaOwnerListHelper.populateOwnerListBoxModel("bob", "charlie", repositories);

        assertThat(result.size(), is(2));
        assertThat(result.stream().anyMatch(opt -> "charlie".equals(opt.name)), is(false));
        assertThat(result.stream().anyMatch(opt -> "alice".equals(opt.name)), is(true));
        assertThat(result.stream().anyMatch(opt -> "bob".equals(opt.name)), is(true));
    }

    @Test
    void populateOwnerListBoxModel_selectedOwnerIsFirst_restAreUnordered() {
        List<GiteaRepository> repositories = Arrays.asList(
                createMockRepository("alice"),
                createMockRepository("bob"),
                createMockRepository("charlie")
        );

        var result = GiteaOwnerListHelper.populateOwnerListBoxModel("dave", "bob", repositories);

        assertThat(result.size(), is(4));
        assertThat(result.get(0).name, is("bob")); // selected owner first

        // The rest should be alice, charlie, dave in any order
        List<String> rest = Arrays.asList(result.get(1).name, result.get(2).name, result.get(3).name);
        assertThat(rest, containsInAnyOrder("alice", "charlie", "dave"));
    }

    @Test
    void populateOwnerListBoxModel_selectedOwnerNotPresent() {
        List<GiteaRepository> repositories = Arrays.asList(
                createMockRepository("alice"),
                createMockRepository("charlie")
        );

        var result = GiteaOwnerListHelper.populateOwnerListBoxModel("dave", "bob", repositories);

        assertThat(result.size(), is(3));
        assertThat(result.get(0).name, not(is("bob"))); // selected owner first

        // The rest should be alice, charlie, dave in any order
        List<String> rest = Arrays.asList(result.get(0).name, result.get(1).name, result.get(2).name);
        assertThat(rest, containsInAnyOrder("alice", "charlie", "dave"));
    }

    private GiteaRepository createMockRepository(String ownerName) {
        GiteaRepository repo = mock(GiteaRepository.class);
        GiteaOwner owner = mock(GiteaOwner.class);
        when(owner.getUsername()).thenReturn(ownerName);
        when(repo.getOwner()).thenReturn(owner);
        return repo;
    }
}
