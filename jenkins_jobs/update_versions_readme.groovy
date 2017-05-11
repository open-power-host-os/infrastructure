job('update_versions_readme') {
  label('!master')

  parameters {
    stringParam('BUILDS_REPO_URL',
		"https://github.com/${GITHUB_ORGANIZATION_NAME}/builds.git",
		'URL of the builds repository.')

    stringParam('BUILDS_REPO_COMMIT', 'master',
		'Commit ID to checkout from the builds repository.')

    stringParam('VERSIONS_REPO_URL',
		"https://github.com/${GITHUB_ORGANIZATION_NAME}/versions.git",
		'URL of the versions repository.')

    stringParam('VERSIONS_REPO_COMMIT', 'master',
		'Commit ID to checkout from the versions repository.')

    stringParam('GITHUB_BOT_NAME', "${GITHUB_BOT_NAME}",
		'Name of the GitHub user to create commits automatically.')

    stringParam('GITHUB_BOT_USER_NAME', "${GITHUB_BOT_USER_NAME}",
		'User name of the GitHub user to create commits automatically.')

    stringParam('GITHUB_BOT_EMAIL', "${GITHUB_BOT_EMAIL}",
		'Email of the GitHub user to create commits automatically')
  }

  scm {
    git {
      remote {
        url('$BUILDS_REPO_URL')
        refspec('+refs/heads/*:refs/remotes/origin/* ' +
                '+refs/pull/*:refs/remotes/origin/pr/*')
      }

      branch('$BUILDS_REPO_COMMIT')
  }

  steps {
    shell(readFileFromWorkspace('jenkins_jobs/update_versions_readme/script.sh'))
  }

  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
