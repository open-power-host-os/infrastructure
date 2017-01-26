job('trigger_nightly_host_os_build') {
  label('!master')
  parameters {
    stringParam('GITHUB_ORGANIZATION_NAME',
		"${GITHUB_ORGANIZATION_NAME}",
		'GitHub organization from where the Host OS repositories will be checked out.')
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
	     'https://github.com/${GITHUB_BOT_USER_NAME}/versions.git',
             EXTRA_PARAMETERS: '--mock-args "--enable-plugin=tmpfs --plugin-option=tmpfs:keep_mounted=True --plugin-option=tmpfs:max_fs_size=32g --plugin-option=tmpfs:required_ram_mb=39800 --with tests"'])
          propertiesFile("BUILD_PARAMETERS", true)
	}
      }
    }
    shell(readFileFromWorkspace(
	    'jenkins_jobs/trigger_nightly_host_os_build/post_build_script.sh'))
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
