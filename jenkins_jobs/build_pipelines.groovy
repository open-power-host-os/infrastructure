def createPipeline(String repositoryName) {
  multibranchPipelineJob(repositoryName) {
    branchSources {
      github {
        apiUri("https://$GITHUB_DOMAIN/api/v3")
        scanCredentialsId('github-user-pass-credentials')
        repoOwner(GITHUB_ORGANIZATION_NAME)
        repository(repositoryName)
        buildForkPRHead(true)
        buildForkPRMerge(false)
        buildOriginBranch(false)
        buildOriginBranchWithPR(false)
        buildOriginPRHead(true)
        buildOriginPRMerge(false)
      }
    }
    triggers {
      periodic(1)
    }
    orphanedItemStrategy {
      discardOldItems()
    }
  }
}

createPipeline('infrastructure')
createPipeline('builds')
createPipeline('versions')
