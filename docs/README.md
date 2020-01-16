# Gitea Plugin documentation

<!-- bare-bones, but better than nothing! -->

## Initial Setup

1. Go to **Manage Jenkins -> Configure System** and scroll down to **Gitea Servers**
2. Add a new server by name and URL, your URL field should be an accessible location of your Gitea instance via HTTP(s)
3. Optionally enable the "manage hooks" checkbox, this will allow Jenkins to configure your webhooks using an account of your choosing.
    - It is recommended to use a personal access token, you can do this by selecting "Add" next to the credentials dropdown and changing it's "Kind" to **Gitea Personal Access Token** and "Scope" to **System**.

Hint: you can ignore a "HTTP 403/Forbidden" error here in case your gitea instance is private.

## Setup gitea user

1. login to your gitea instance with an administrator account.
2. create a new user, e.g. "jenkins". Set password to something secure - you will not need it for login.
3. add the jenkins user to the organization you want to build projects for in jenkins (either by adding him to an existing team or adding a new "ci"-team). Make sure that team is associated to the repositories you want to build.
3. log out of gitea.
4. log back in as the new "jenkins" user.
5. in user profile settings, go to "application" and add a new access token. Make sure to note the token shown.

## Add gitea organization item

1. In main menu, click "New Item". Note that gitea plugin depends on the multibranch pipeline plugin, so make sure to have that installed.
2. Select "Gitea organization" as the item type
3. In the "Gitea organzations" section, add a new credential of type "Gitea personal access token".
4. Add the access token created before for the jenkins user in gitea. Ignore the error about the token not having the correct length.
5. In the "Owner" field, add the name of the organization in gitea you want to build projects for (**not** the full name).
6. Fill the rest of the form as required. Click "Save". The following scan should list the repositories that the jenkins user can see in the organization selected.
