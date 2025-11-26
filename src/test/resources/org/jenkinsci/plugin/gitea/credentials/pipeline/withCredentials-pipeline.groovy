node {
    withCredentials([string(
        credentialsId: 'personalAccessTokenId',
        variable: 'API_TOKEN1'
    )]) {
        echo "Token1 is ${API_TOKEN1.substring(1)}"
        if (isUnix()) {
            sh 'echo API_TOKEN1 is $API_TOKEN1'
        } else {
            bat 'echo API_TOKEN1 is %API_TOKEN1%'
        }
    }
}
