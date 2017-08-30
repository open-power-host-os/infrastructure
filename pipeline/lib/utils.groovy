#!groovy

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import hudson.util.Secret
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl


def addSecretString(Secret secret, String description) {

  credentialsId = java.util.UUID.randomUUID().toString()

  credentials = new StringCredentialsImpl(
    CredentialsScope.GLOBAL, credentialsId, description, secret)

  getCredentialsStore().addCredentials(Domain.global(), credentials)

  return credentialsId
}

def removeCredentials(String credentialsId) {
  domain = Domain.global()

  credentialsMatched = CredentialsMatchers.filter(
    getCredentialsStore().getCredentials(domain),
    CredentialsMatchers.withId(credentialsId))

  // A credentialsId should only match a single set of credentials, so
  // simply take the first match here
  credentials = credentialsMatched.get(0)

  getCredentialsStore().removeCredentials(domain, credentials)
}

def getCredentialsStore() {
  return SystemCredentialsProvider.getInstance().getStore()
}

def getGitRepos(String triggeredRepoName) {
  String githubOrgPath = "ssh://git@github/$params.GITHUB_ORGANIZATION_NAME"
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
               sha: env.CHANGE_REFSPEC,
               status: status)
}

def checkoutRepo(String repoName, Map gitRepos) {
  dir(repoName) {
    checkout(gitRepos[repoName])
  }
}

def replaceInFile(String fileName, String token, String value) {
  String content = readFile fileName
  content.replaceAll(token, value)
  writeFile file: fileName, text: content
}

def archiveAndPrint(String pattern, boolean allowEmpty = false) {
  try {
    // We need to check if the files exist first because archiveArtifacts
    // fails silently
    // https://issues.jenkins-ci.org/browse/JENKINS-38005
    String files = sh(script:"ls $pattern", returnStdout: true).trim()
    echo('Archiving:\n' + files)
    archiveArtifacts(pattern)
  } catch (hudson.AbortException exception) {
    echo('No files to archive')
    if (!allowEmpty) {
      throw(exception)
    }
  }
}

def rsyncUpload(String args, String buildDirRsyncURL) {
  sh """\
rsync -e 'ssh -i $env.HOME/.ssh/upload_server_id_rsa' \\
      --verbose --compress --stats --times --chmod=a+rwx,g+rwx,o- \\
      $args $buildDirRsyncURL\
"""
}

def convertToJenkinsParameters(Map parameters) {
  List jenkinsParams = []

  for (param in parameters) {
    jenkinsParam = null

    if (!param.value.type) {
      jenkinsParam = string(name: param.key,
                            defaultValue: param.value.defaultValue,
	                    description: param.value.description)
    } else if (param.value.type == "password") {
      jenkinsParam = password(name: param.key,
                              defaultValue: param.value.defaultValue,
                              description: param.value.description)
    }
    jenkinsParams.add(jenkinsParam)
  }
  return jenkinsParams
}


return this
