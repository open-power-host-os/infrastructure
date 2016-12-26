job('build_host_os') {
  label('builds_slave_label')
  concurrentBuild()
  throttleConcurrentBuilds {
    maxPerNode(1)
  }
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
    stringParam('PACKAGES', '', 'Packages to build. Leave empty to build all.')
    stringParam('CENTOS_INTERNAL_MIRROR_BASE_URL',
		"${CENTOS_INTERNAL_MIRROR_BASE_URL}",
		'Base URL to the CentOS YUM repository internal mirror. Empty to use main repository.')
    stringParam('UPLOAD_SERVER_HOST_NAME', "${UPLOAD_SERVER_HOST_NAME}",
		'Host name of the target server to upload build results.')
    stringParam('UPLOAD_SERVER_USER_NAME',
		"${UPLOAD_SERVER_USER_NAME}",
		'User name of the target server to upload build results.')
    stringParam('UPLOAD_SERVER_BUILDS_DIR',
		"${UPLOAD_SERVER_BUILDS_DIR}",
		'Directory in the target server to upload build results.')
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
    shell(readFileFromWorkspace('jenkins_jobs/build_host_os/script.sh'))
  }
  publishers {
    archiveArtifacts('repository/')
    archiveArtifacts('build/*/build.log')
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
