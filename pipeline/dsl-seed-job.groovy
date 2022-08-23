import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

@NonCPS
def scriptApprove() {
    def scriptApproval = ScriptApproval.get()
    scriptApproval.pendingScripts.each {
        scriptApproval.approveScript(it.hash)
    }
}

@NonCPS
def runMainDsl() {
    jobDsl(
        targets: 'Jenkins/Jobs/DSL/main.groovy',
        removedJobAction:  'IGNORE',
        removedViewAction: 'IGNORE',
        unstableOnDeprecation: true
    )
}

node() {
    stage("Clone repo...") {
        checkout([
            $class: 'GitSCM', branches: [[name: "${BRANCH}"]], doGenerateSubmoduleConfigurations: false,
            extensions: [[$class: 'CleanBeforeCheckout', deleteUntrackedNestedRepositories: true]],
            submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'Jenkins-CI-CD', url: "${REPO_URL}"]]
        ])
    }

    stage("Build DSL...") {
        try {
            runMainDsl()

        } catch (Exception ex) {
            scriptApprove()
            runMainDsl()
            currentBuild.result = "UNSTABLE"
        }
    }
}
