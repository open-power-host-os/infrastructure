job('trigger_weekly_host_os_build') {
  label('!master')
  logRotator {
    numToKeep(30)
  }
  parameters {
    stringParam('GITHUB_ORGANIZATION_NAME',
		"${GITHUB_ORGANIZATION_NAME}",
		'GitHub organization from where the Host OS repositories will be checked out.')
    stringParam('BUILDS_REPOSITORY_BRANCH',
		"master",
		'Branch of the builds repository to clone and pass to the build jobs.')
    stringParam('VERSIONS_REPOSITORY_BRANCH',
		"master",
		"Branch of the versions repository on which to base the update of packages' versions.")
    stringParam('GITHUB_BOT_NAME', "${GITHUB_BOT_NAME}",
		'Name of the GitHub user to create commits automatically.')
    stringParam('GITHUB_BOT_USER_NAME', "${GITHUB_BOT_USER_NAME}",
		'User name of the GitHub user to create commits automatically.')
    stringParam('GITHUB_BOT_EMAIL', "${GITHUB_BOT_EMAIL}",
		'Email of the GitHub user to create commits automatically')
    stringParam('UPLOAD_SERVER_HOST_NAME', "${UPLOAD_SERVER_HOST_NAME}",
		'Host name of the target server to upload build results.')
    stringParam('UPLOAD_SERVER_USER_NAME',
		"${UPLOAD_SERVER_USER_NAME}",
		'User name of the target server to upload build results.')
    stringParam('UPLOAD_SERVER_WEEKLY_DIR',
		"${UPLOAD_SERVER_WEEKLY_DIR}",
		'Directory in the target server to upload weekly build results.')
    stringParam('BUILD_ISO_TRIGGER_PHRASE', 'start iso',
		'Phrase that is recognized as a trigger by the job that starts an ISO build.')
  }
  scm {
    git {
      remote {
	url('https://github.com/${GITHUB_ORGANIZATION_NAME}/builds/')
      }
      branch('$BUILDS_REPOSITORY_BRANCH')
    }
  }
  triggers {
    cron('0 11 * * 3')
  }
  steps {
    shell(readFileFromWorkspace(
	    'jenkins_jobs/trigger_weekly_host_os_build/script.sh'))
  }
  wrappers {
    timestamps()
    preBuildCleanup()
    credentialsBinding {
      usernamePassword('GITHUB_USER_NAME', 'GITHUB_PASSWORD', 'github-user-pass-credentials')
    }
  }
}
