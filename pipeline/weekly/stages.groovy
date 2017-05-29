#!groovy

import groovy.transform.Field

dailyStages = load 'infrastructure/pipeline/daily/stages.groovy'
pipelineParameters = load 'infrastructure/pipeline/weekly/parameters.groovy'

@Field String GITHUB_IO_REPO_NAME
@Field String GITHUB_IO_MAIN_REPO_URL
@Field String GITHUB_IO_PUSH_REPO_URL

def initialize() {
  String triggerExpression = '0 11 * * 3'
  dailyStages.initialize(pipelineParameters, triggerExpression)

  GITHUB_IO_REPO_NAME = "${params.GITHUB_ORGANIZATION_NAME}.github.io"
  GITHUB_IO_MAIN_REPO_URL =
    "$dailyStages.MAIN_REPO_URL_PREFIX/${GITHUB_IO_REPO_NAME}.git"
  GITHUB_IO_PUSH_REPO_URL =
    "$dailyStages.PUSH_REPO_URL_PREFIX/${GITHUB_IO_REPO_NAME}.git"
}

def updateVersions() {
  dailyStages.updateVersions()
}

def buildPackages() {
  dailyStages.buildPackages()
}

def buildIso() {
  dailyStages.buildIso()
}

def uploadBuildArtifacts() {
  dailyStages.uploadBuildArtifacts()
}

def createReleaseNotes() {
  deleteDir()
  dir('builds') {
    git(url: "https://github.com/$params.GITHUB_ORGANIZATION_NAME/builds.git",
        branch: params.BUILDS_REPO_REFERENCE)
    sh """\
python host_os.py \
       --verbose \
       --work-dir $params.BUILDS_WORKSPACE_DIR \
       build-release-notes \
           --packages-metadata-repo-url $dailyStages.VERSIONS_PUSH_REPO_URL \
           --packages-metadata-repo-branch $dailyStages.COMMIT_BRANCH \
           --release-notes-repo-url $GITHUB_IO_MAIN_REPO_URL \
           --updater-name '$params.GITHUB_BOT_NAME' \
           --updater-email $params.GITHUB_BOT_EMAIL \
           --push-repo-url $GITHUB_IO_PUSH_REPO_URL \
           --push-repo-branch $dailyStages.COMMIT_BRANCH
"""
  }
}

def createSymlinks() {
  dailyStages.createSymlinks()
}

def tagGitRepos() {
  String REPOSITORIES_PATH = "$params.BUILDS_WORKSPACE_DIR/repositories"
  String UPDATED_VERSIONS_REPO_PATH =
    "$REPOSITORIES_PATH/${VERSIONS_REPO_NAME}_update-versions"
  String GITHUB_IO_REPO_PATH = "$REPOSITORIES_PATH/$GITHUB_IO_REPO_NAME"
  String BUILDS_REPO_PATH = '.'

  List REPOS_PATHS = [
    UPDATED_VERSIONS_REPO_PATH, GITHUB_IO_REPO_PATH, BUILDS_REPO_PATH]
  String VERSION_FILE_PATH = "$UPDATED_VERSIONS_REPO_PATH/VERSION"
  String VERSION = readFile(VERSION_FILE_PATH).readLines().last()
  String TAG_NAME = "$VERSION-$dailyStages.RELEASE_DATE"

  for (String repo_path : REPOS_PATHS) {
    dir(repo_path) {
      sh "git tag $TAG_NAME"
      sh "git push origin $TAG_NAME"
    }
  }
}

return this
