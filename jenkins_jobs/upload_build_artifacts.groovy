job('upload_build_artifacts') {
  label('builds_slave_label')
  logRotator {
    numToKeep(30)
  }
  concurrentBuild()
  parameters {
    stringParam('BUILD_JOB_NUMBER', '',
		'Number of the Host OS build job to upload.')
    stringParam('UPLOAD_SERVER_HOST_NAME', "${UPLOAD_SERVER_HOST_NAME}",
		'Host name of the target server to upload build results.')
    stringParam('UPLOAD_SERVER_USER_NAME',
		"${UPLOAD_SERVER_USER_NAME}",
		'User name of the target server to upload build results.')
    stringParam('UPLOAD_SERVER_BUILDS_DIR',
		"${UPLOAD_SERVER_BUILDS_DIR}",
		'Directory in the target server to upload build results.')
  }
  steps {
    copyArtifacts('build_host_os') {
      buildSelector {
	buildNumber('$BUILD_JOB_NUMBER')
      }

      includePatterns('BUILD_TIMESTAMP')
      includePatterns('SUCCESS')
      includePatterns('repository/')
      optional()
    }
    shell(readFileFromWorkspace('jenkins_jobs/upload_build_artifacts/script.sh'))
  }
  publishers {
    archiveArtifacts('hostos.repo')
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
