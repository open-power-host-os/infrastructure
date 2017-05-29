#!groovy

def getGitRepos(String triggeredRepoName) {
  String githubOrgPath = "https://github.com/$params.GITHUB_ORGANIZATION_NAME"
  gitRepos = [:]
  for (String repoName : ['builds', 'versions']) {
    if (repoName == triggeredRepoName) {
      gitRepos[repoName] = scm
    } else {
      String repoReferenceVarName = "${repoName}_REPO_REFERENCE".toUpperCase()
      gitRepos[repoName] =
        [$class: 'GitSCM',
         branches: [[name: params."$repoReferenceVarName"]],
         doGenerateSubmoduleConfigurations: false,
         extensions: [], submoduleCfg: [],
         userRemoteConfigs: [[url: "$githubOrgPath/$repoName"]]]
    }
  }
  return gitRepos
}

def setGithubStatus(String repositoryName, String description, String status) {
  githubNotify(account: params.GITHUB_ORGANIZATION_NAME,
               context: 'continuous-integration/jenkins/pr-head',
               credentialsId: 'github-user-pass-credentials',
               description: description,
               targetUrl: "$env.JOB_URL/workflow-stage/",
               repo: repositoryName,
               sha: env.CHANGE_BRANCH,
               status: status)
}

def checkoutRepo(String repoName, Map gitRepos) {
  dir(repoName) {
    checkout(gitRepos[repoName])
  }
}

def rsyncUpload(String args, String buildDirRsyncURL) {
  sh """\
rsync -e 'ssh -i $env.HOME/.ssh/upload_server_id_rsa' \\
      --verbose --compress --stats --times --chmod=a+rwx,g+rwx,o- \\
      $args $buildDirRsyncURL\
"""
}

def replaceInFile(String fileName, String token, String value) {
  String content = readFile fileName
  content.replaceAll(token, value)
  writeFile file: fileName, text: content
}

return this
