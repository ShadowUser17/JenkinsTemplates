#### URLs:
- [pipeline-credentials](https://www.jenkins.io/doc/pipeline/steps/credentials-binding/)
- [credentials-plugin-doc](https://github.com/jenkinsci/credentials-plugin/tree/master/docs)
- [credentials-plugin-lib](https://javadoc.jenkins.io/plugin/credentials/)

#### From CLI:
```groovy
```

#### From Pipeline:
```groovy
def cred_list = ["testing-env"];
def text = "";

for(def name in cred_list) {
    withCredentials([file(credentialsId: "${name}", variable: "ENV_FILE")]) {
        text = readFile("${ENV_FILE}");
    }
    println("${name}:\n${text}\n");
}
```
