job('trigger_nightly_host_os_build') {
  label('!master')
  logRotator {
    numToKeep(30)
  }
  parameters {
    stringParam('GITHUB_ORGANIZATION_NAME',
		"${GITHUB_ORGANIZATION_NAME}",
		'GitHub organization from where the Host OS repositories will be checked out.')
    stringParam('UPLOAD_SERVER_HOST_NAME', "${UPLOAD_SERVER_HOST_NAME}",
		'Host name of the target server to upload build results.')
    stringParam('UPLOAD_SERVER_USER_NAME',
		"${UPLOAD_SERVER_USER_NAME}",
		'User name of the target server to upload build results.')
    stringParam('UPLOAD_SERVER_NIGHTLY_DIR',
		"${UPLOAD_SERVER_NIGHTLY_DIR}",
		'Directory in the target server to upload nightly build results.')
  }
  scm {
    git {
      remote {
	url('https://github.com/${GITHUB_ORGANIZATION_NAME}/builds/')
      }
      branch('master')
    }
  }
  triggers {
    cron('0 22 * * *')
  }
  steps {
    shell(readFileFromWorkspace(
	    'jenkins_jobs/trigger_nightly_host_os_build/pre_build_script.sh'))
    downstreamParameterized {
      trigger('build_host_os') {
	block {
	  buildStepFailure('FAILURE')
	  failure('FAILURE')
	  unstable('UNSTABLE')
	}
	parameters {
	  predefinedProps(
	    [BUILDS_REPO_URL:
	     'https://github.com/${GITHUB_ORGANIZATION_NAME}/builds.git',
	     BUILDS_REPO_COMMIT: 'master',
	     VERSIONS_REPO_URL:
	     'https://github.com/${GITHUB_ORGANIZATION_NAME}/versions.git',
             VERSIONS_REPO_COMMIT: 'master',
             EXTRA_PARAMETERS: '--mock-args "--enable-plugin=tmpfs --plugin-option=tmpfs:keep_mounted=True --plugin-option=tmpfs:max_fs_size=32g --plugin-option=tmpfs:required_ram_mb=39800 --with tests"'])
	}
      }
    }
    copyArtifacts('build_host_os') {
      buildSelector {
        buildNumber('$TRIGGERED_BUILD_NUMBER_build_host_os')
      }
      includePatterns('BUILD_TIMESTAMP')
    }
    shell(readFileFromWorkspace(
	    'jenkins_jobs/trigger_nightly_host_os_build/post_build_script.sh'))
    downstreamParameterized {
      trigger('build_host_os_iso') {
        block {
          buildStepFailure('FAILURE')
          failure('FAILURE')
          unstable('UNSTABLE')
        }
        parameters {
          predefinedProps(
            [BUILDS_REPO_URL:
             'https://github.com/${GITHUB_ORGANIZATION_NAME}/builds.git',
             BUILDS_REPO_COMMIT: 'master',
             BUILD_JOB_NUMBER:
             '${TRIGGERED_BUILD_NUMBER_build_host_os}'])
        }
      }
    }
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
