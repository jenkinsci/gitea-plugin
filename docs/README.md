# Gitea Plugin documentation

> ℹ️ Gitea plugin depends on the `multibranch pipeline` plugin.

<!-- bare-bones, but better than nothing! -->

## Initial Setup

1. Go to **Manage Jenkins -> Configure System** and scroll down to **Gitea Servers**
2. Add a new server by name and URL, your URL field should be an accessible location of your Gitea instance via HTTP(s)
3. Optionally enable the "manage hooks" checkbox, this will allow Jenkins to configure your webhooks using an account of your choosing.
    - It is recommended to use a personal access token, you can do this by selecting "Add" next to the credentials dropdown and changing it's "Kind" to **Gitea Personal Access Token** and "Scope" to **System**.

> ℹ️ Hint: you can ignore a "HTTP 403/Forbidden" error here in case your Gitea instance is private.

## Setup Gitea user

1. login to your Gitea instance with an administrator account (optional, you can also register a new user).
2. create a new user, e.g. "jenkins". Set password to something secure - you will not need it for login.
3. add the jenkins user to the organization you want to build projects for in jenkins (either by adding him to an existing team or adding a new "ci"-team). Make sure that team is associated to the repositories you want to build.
3. log out of Gitea.
4. log back in as the new "jenkins" user.
5. in user profile settings, go to "application" and add a new access token. Make sure to note the token shown.

> ℹ️  Hint: As of [Gitea version 1.16.9](https://github.com/go-gitea/gitea/releases/tag/v1.16.9) the user requires **Write permissions** to repository code. Otherwise, status checks cannot be submitted anymore causing "HTTP 403/Forbidden" errors during builds.

## Map your Gitea organization/user

### Create an item inside Jenkins

1. In main menu, click "New Item"

**Using `branch-api` plugin version <2.7.0**

2. Select "Gitea organization" as the item type

**Using `branch-api` plugin version >=2.7.0**

2. Select "Organization Folder" as the item type

### Configure the item

1. When configuring the new item, select "Repository Sources"

ℹ️ **This is only necessary when using `branch-api` plugin version >=2.7.0**

2. In the "Gitea organzations" section, add a new credential of type "Gitea personal access token".
3. Add the access token created before for the jenkins user in Gitea. Ignore the error about the token not having the correct length.
4. In the "Owner" field, add the name of the organization in Gitea you want to build projects for (**not** the full name).
5. Fill the rest of the form as required. Click "Save". The following scan should list the repositories that the jenkins user can see in the organization selected.

## Using a Gitea personal access token in a Jenkins Pipeline

["Using credentials"](https://www.jenkins.io/doc/book/using/using-credentials/) includes a Jenkins Pipeline credentials tutorial.
Additional credentials examples are available in the ["Injecting secrets into builds"](https://docs.cloudbees.com/docs/cloudbees-ci/latest/secure/injecting-secrets) page from CloudBees.

The Gitea plugin includes support for Gitea personal access tokens as Jenkins [credentials](https://www.jenkins.io/doc/book/using/using-credentials/).
Personal access tokens are used to authenticate Gitea repository and API access without using a Gitea username and password.
Gitea personal access tokens are defined in Gitea from the "Applications" page of each Gitea user's "Settings" (/user/settings/applications).
Personal access token permissions can be defined to grant access to a subset of the resources of the Gitea server.

Once a Gitea personal access token has been created in Gitea and added to Jenkins as a credential, it can be referenced from a Pipeline job using the `withCredentials` Pipeline step.
Use the [Pipeline syntax snippet generator](https://www.jenkins.io/pipeline/getting-started-pipelines/#using-snippet-generator) to create an example of the `withCredentials` step.
Choose "Secret text" as the credential type in the snippet generator.

A typical example of a Gitea personal access token in a Jenkins declarative Pipeline would look like:

```groovy
pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        withCredentials([string(credentialsId: 'my-gitea-token', variable: 'MY_TOKEN')]) {
          if (isUnix()) {
            sh 'git clone https://$MY_TOKEN@gitea.com/exampleUser/private-repo.git'
          } else {
            bat 'git clone https://%MY_TOKEN%@gitea.com/exampleUser/private-repo.git'
          }
        }
      }
    }
  }
}
```

A typical example of a Gitea personal access token in a Jenkins scripted Pipeline would look like:

```groovy
node {
  stage('Checkout') {
    withCredentials([string(credentialsId: 'my-gitea-token', variable: 'MY_TOKEN')]) {
      if (isUnix()) {
        sh 'git clone https://$MY_TOKEN@gitea.com/exampleUser/private-repo.git'
      } else {
        bat 'git clone https://%MY_TOKEN%@gitea.com/exampleUser/private-repo.git'
      }
    }
  }
}
```

### Note

You should use a single quote (`'`) instead of a double quote (`"`) whenever you can.
This is particularly important in Pipelines where a statement may be interpreted by both the Pipeline engine and an external interpreter, such as a Unix shell (`sh`) or Windows Command (`bat`) or Powershell (`ps`).
This reduces complications with password masking and command processing.
The `sh` step in the above examples properly demonstrates this.
It references an environment variable, so the single-quoted string passes its value unprocessed to the `sh` step, and the shell interprets `$MY_TOKEN`.
