package org.jenkinsci.plugin.gitea;

import hudson.util.ListBoxModel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jenkinsci.plugin.gitea.client.api.GiteaRepository;

/**
 * Utility class for returning unique Gitea repository owners.
 */
public class GiteaOwnerListHelper {
    /**
     * Retrieves unique repository owners from the given list of repositories.
     *
     * @param currentUser the Gitea user used for fetching the repositories
     * @param repositories the list of repositories to extract owners from
     * @return a Set of unique owner names
     */
    public static Set<String> getOwners(String currentUser,
                                        List<GiteaRepository> repositories) {
        Set<String> owners = new HashSet<>();

        for (GiteaRepository repo : repositories) {
            String owner = repo.getOwner().getUsername();
            owners.add(owner); // Set will handle duplicates
        }

        owners.add(currentUser); // Ensure current user is included

        return owners;
    }

    /**
     * Returns a ListBoxModel with repository owners.
     *
     * @param currentUser the Gitea user used for fetching the repositories
     * @param currentOwner the currently selected owner (will be preserved if valid)
     * @param repositories the list of repositories to extract owners from
     * @return the populated ListBoxModel
     */
    public static ListBoxModel populateOwnerListBoxModel(String currentUser,
                                        String currentOwner,
                                        List<GiteaRepository> repositories) {
        ListBoxModel result = new ListBoxModel();
        Set<String> owners = getOwners(currentUser, repositories);
        // Add owners to the result, with current selection first if it exists
        if (owners.remove(currentOwner)) {
            result.add(currentOwner);
        }

        for (String owner : owners) {
            result.add(owner);
        }

        return result;
    }
}
