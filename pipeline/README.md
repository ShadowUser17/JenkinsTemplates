### Create DSL seed job
#### Job Authorization:
- Authorize Strategy ```Run as Specific User```
- User ID ```<username>```

#### Generic Webhook Trigger:
- Variables
  - BRANCH ```$.ref```
  - DELETED ```$.deleted```
  - COMMIT_ADDED ```$.head_commit.added```
  - COMMIT_MODIFIED ```$.head_commit.modified```
  - COMMIT_REMOVED ```$.head_commit.removed```

- Options
  - Token ```ci-cd-testing```
  - Expression ```^(refs\/heads\/master)\s+(false).*?(\"jenkins\/jobs\/.*\")```
  - Text ```$BRANCH $DELETED $COMMIT_MODIFIED $COMMIT_ADDED $COMMIT_REMOVED```
