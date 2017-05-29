job('build_old_host_os_versions') {
  label('!master')
  parameters {
    stringParam('GITHUB_ORGANIZATION_NAME',
		"${GITHUB_ORGANIZATION_NAME}",
		'GitHub organization from where the Host OS repositories will be checked out.')
    stringParam('BUILDS_REPO_URL',
		"https://github.com/${GITHUB_ORGANIZATION_NAME}/builds.git",
		'URL of the builds repository.')
    stringParam('BUILDS_REPO_REFERENCE', 'origin/master',
		'Git reference to checkout from the builds repository.')
  }
  scm {
    git {
      remote {
	    url('$BUILDS_REPO_URL')
      }
      branch('$BUILDS_REPO_REFERENCE')
    }
  }
  steps {
    shell(
      readFileFromWorkspace(
	    'jenkins_jobs/build_old_host_os_versions/script.sh'))
  }
  wrappers {
    timestamps()
  }
}
