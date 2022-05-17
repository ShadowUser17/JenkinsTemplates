node("master") {
    currentBuild.description = "Testing..."

    stage("Stage one...") {
        // cleanWs()

        /* git(
            credentialsId: "...",
            branch: '...',
            url: "..."
        ) */
    }

    stage("Stage two...") {
        /* try {
            timeout(time: 10, unit: 'MINUTES') {
                // ...
            }
        } catch(Exception exc) {
            throw exc
        } */
    }
}
