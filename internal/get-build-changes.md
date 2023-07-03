#### URLs:
- [jenkins-model-Jenkins](https://javadoc.jenkins-ci.org/jenkins/model/Jenkins.html)
- [workflow-plugin-WorkflowRun](https://javadoc.jenkins.io/plugin/workflow-job/org/jenkinsci/plugins/workflow/job/WorkflowRun.html)
- [git-plugin-GitChangeSet](https://javadoc.jenkins.io/plugin/git/hudson/plugins/git/GitChangeSet.html)

#### From CLI:
```groovy
def job = Jenkins.instance.getItemByFullName("Testing/test-pipeline");
def build = job.getBuildByNumber(1);

for(def changeSet in build.changeSets) {
  for(def item in changeSet.items) {
    println(item.getComment().trim());
  }
}
```

#### From Pipeline:
```groovy
for(def changeSet in currentBuild.changeSets) {
  for(def item in changeSet.items) {
    println(item.getComment().trim());
  }
}
```
