def createPipeline(String repositoryName) {
  multibranchPipelineJob(repositoryName) {
    branchSources {
      github {
        scanCredentialsId('github-user-pass-credentials')
        repoOwner(GITHUB_ORGANIZATION_NAME)
        repository(repositoryName)
        buildForkPRHead(true)
        buildForkPRMerge(false)
        buildOriginBranch(true)
        buildOriginBranchWithPR(true)
        buildOriginPRHead(false)
        buildOriginPRMerge(false)
      }
    }
    triggers {
      periodic(1)
    }
    orphanedItemStrategy {
      discardOldItems {
        numToKeep(30)
      }
    }
  }
}

createPipeline('infrastructure')
createPipeline('builds')
createPipeline('versions')
