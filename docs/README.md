# Gitea Plugin documentation

<!-- bare-bones, but better than nothing! -->

## Initial Setup

1. Go to **Manage Jenkins -> Configure System** and scroll down to **Gitea Servers**
2. Add a new server by name and URL, your URL field should be an accessible location of your Gitea instance via HTTP(s)
3. Optionally enable the "manage hooks" checkbox, this will allow Jenkins to configure your webhooks using an account of your choosing.
    - It is recommended to use a personal access token, you can do this by selecting "Add" next to the credentials dropdown and changing it's "Kind" to **Gitea Personal Access Token** and "Scope" to **System**.
