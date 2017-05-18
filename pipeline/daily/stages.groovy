#!groovy

import groovy.transform.Field

buildStages = load 'infrastructure/pipeline/build/stages.groovy'
pipelineParameters = load 'infrastructure/pipeline/daily/parameters.groovy'

@Field String PERIODIC_BUILDS_DIR_NAME
@Field String RELEASE_DATE

@Field String MAIN_REPO_URL_PREFIX
@Field String PUSH_REPO_URL_PREFIX
@Field String VERSIONS_REPO_NAME
@Field String VERSIONS_MAIN_REPO_URL
@Field String VERSIONS_PUSH_REPO_URL
@Field String BUILDS_REPO_NAME
@Field String COMMIT_BRANCH

def initialize(List pipelineParameters = pipelineParameters,
               String triggerExpression = '0 22 * * *') {
  properties([parameters(pipelineParameters),
              pipelineTriggers([cron(triggerExpression)]),
              [$class: 'jenkins.model.BuildDiscarderProperty', strategy:
               [$class: 'LogRotator', numToKeepStr: '10']]])

  MAIN_REPO_URL_PREFIX = "ssh://git@github.com/$params.GITHUB_ORGANIZATION_NAME"
  PUSH_REPO_URL_PREFIX = "ssh://git@github.com/$params.GITHUB_BOT_USER_NAME"

  String REPOSITORIES_PATH = "$params.BUILDS_WORKSPACE_DIR/repositories"

  VERSIONS_REPO_NAME = 'versions'
  VERSIONS_MAIN_REPO_URL = "$MAIN_REPO_URL_PREFIX/${VERSIONS_REPO_NAME}.git"
  VERSIONS_PUSH_REPO_URL = "$PUSH_REPO_URL_PREFIX/${VERSIONS_REPO_NAME}.git"

  BUILDS_REPO_NAME = 'builds'

  buildStages.gitRepos = utils.getGitRepos(null)
  buildStages.buildInfo = [:]

  PERIODIC_BUILDS_DIR_NAME = (
    params.UPLOAD_SERVER_PERIODIC_BUILDS_DIR_PATH.tokenize('/').last())
  RELEASE_DATE = new Date().format('yyyy-MM-dd', TimeZone.getTimeZone('UTC'))
  COMMIT_BRANCH = "$PERIODIC_BUILDS_DIR_NAME-$RELEASE_DATE"
}

def updateVersions() {
  deleteDir()
  dir('builds') {
    git(url: "https://github.com/$params.GITHUB_ORGANIZATION_NAME/builds.git",
        branch: params.BUILDS_REPO_REFERENCE)
    sh """\
python host_os.py    \
       --verbose \
       --work-dir $params.BUILDS_WORKSPACE_DIR \
       update-versions \
           --packages-metadata-repo-url $VERSIONS_MAIN_REPO_URL \
           --packages-metadata-repo-branch $params.VERSIONS_REPO_REFERENCE \
           --updater-name '$params.GITHUB_BOT_NAME' \
           --updater-email $params.GITHUB_BOT_EMAIL \
           --push-repo-url $VERSIONS_PUSH_REPO_URL \
           --push-repo-branch $COMMIT_BRANCH \
"""
  }
}

def buildPackages() {
  buildStages.buildPackages()
}

def buildIso() {
  buildStages.buildIso()
}

def uploadBuildArtifacts() {
  buildStages.uploadArtifacts()
}

def createSymlinks() {
  String RSYNC_URL_PREFIX =
    "$params.UPLOAD_SERVER_USER_NAME@$params.UPLOAD_SERVER_HOST_NAME:"
  String PERIODIC_BUILDS_DIR_RSYNC_URL =
    "${RSYNC_URL_PREFIX}$params.UPLOAD_SERVER_PERIODIC_BUILDS_DIR_PATH"

  File periodicBuildsDir = new File(params.UPLOAD_SERVER_PERIODIC_BUILDS_DIR_PATH)
  File buildDir = new File(params.UPLOAD_SERVER_BUILDS_DIR_PATH,
                           buildStages.buildInfo.BUILD_TIMESTAMP)
  String RELATIVE_BUILD_DIR_PATH =
    periodicBuildsDir.toPath().relativize(buildDir.toPath()).toString()

  echo "Creating $PERIODIC_BUILDS_DIR_NAME builds symlinks"
  sh "ln -s $RELATIVE_BUILD_DIR_PATH $RELEASE_DATE"
  sh "ln -s $RELEASE_DATE latest"

  echo "Creating remote $PERIODIC_BUILDS_DIR_NAME directory hierarchy"
  sh "mkdir $PERIODIC_BUILDS_DIR_NAME"
  utils.rsyncUpload("--recursive $PERIODIC_BUILDS_DIR_NAME/",
                    PERIODIC_BUILDS_DIR_RSYNC_URL)

  echo "Uploading $PERIODIC_BUILDS_DIR_NAME builds symlinks"
  utils.rsyncUpload("--links $RELEASE_DATE", PERIODIC_BUILDS_DIR_RSYNC_URL)
  utils.rsyncUpload('--links latest', PERIODIC_BUILDS_DIR_RSYNC_URL)
}

return this
