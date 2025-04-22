package org.jenkinsci.plugin.gitea.credentials.pipeline

node {
    withCredentials([[
        $class: 'org.jenkinsci.plugin.gitea.credentials.PersonalAccessTokenBinding',
        credentialsId: "personalAccessTokenId",
        variable: "API_TOKEN1"
    ]]) {
        println "Token1 is ${API_TOKEN1.substring(1)}"
    }
    withCredentials([giteaPersonalAccessToken(
        credentialsId: "personalAccessTokenId",
        variable: "API_TOKEN2"
    )]) {
        println "Token2 is ${API_TOKEN2.substring(1)}"
    }
}