job('upload_iso') {
  label('builds_slave_label')
  logRotator {
    numToKeep(30)
  }
  concurrentBuild()
  parameters {
    stringParam('BUILD_JOB_NUMBER', '',
                'Number of the job that built the packages used to create the ISO.')
    stringParam('BUILD_ISO_JOB_NUMBER', '',
                'Number of the job containing the build artifacts to upload.')
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
    }
    copyArtifacts('build_host_os_iso') {
      buildSelector {
        buildNumber('$BUILD_ISO_JOB_NUMBER')
      }
      includePatterns('*.iso')
      includePatterns('*.iso.sha256')
    }
    shell(readFileFromWorkspace('jenkins_jobs/upload_iso/script.sh'))
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
