job('run_host_os_admin_commands') {
  label('builds_slave_label')
  logRotator {
    numToKeep(30)
  }
  concurrentBuild()
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
  }
  steps {
    shell(readFileFromWorkspace('jenkins_jobs/run_host_os_admin_commands/script.sh'))
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
