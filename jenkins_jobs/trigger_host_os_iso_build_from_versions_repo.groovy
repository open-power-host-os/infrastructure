job('trigger_host_os_iso_build_from_versions_repo') {
  label('!master')
  logRotator {
    numToKeep(30)
  }
  concurrentBuild()
  throttleConcurrentBuilds {
    maxPerNode(1)
  }
  parameters {
    stringParam('sha1', '', 'SHA-1 of the commit to build.')
    stringParam('GITHUB_ORGANIZATION_NAME',
		"${GITHUB_ORGANIZATION_NAME}",
		'GitHub organization from where the Host OS repositories will be checked out.')
  }
  properties {
    githubProjectUrl("https://github.com/${GITHUB_ORGANIZATION_NAME}/versions/")
  }
  triggers {
    githubPullRequest {
      userWhitelist("${GHPRB_ADMIN_USER}")
      orgWhitelist("${GHPRB_ADMIN_ORGANIZATION}")
      allowMembersOfWhitelistedOrgsAsAdmin()
      cron('H/5 * * * *')
      triggerPhrase('.*start\\W+iso.*')
      onlyTriggerPhrase()
      extensions {
	commitStatus {
	  context('Build Host OS ISO')
	  statusUrl('${JENKINS_URL}/job/build_host_os_iso/${TRIGGERED_BUILD_NUMBER_build_host_os_iso}')
	}
      }
    }
  }
  steps {
    shell(readFileFromWorkspace(
	    'jenkins_jobs/trigger_host_os_iso_build_from_versions_repo/script.sh'))
    downstreamParameterized {
      trigger('build_host_os_iso') {
	block {
	  buildStepFailure('FAILURE')
	  failure('FAILURE')
	  unstable('UNSTABLE')
	}
	parameters {
	  propertiesFile("BUILD_PARAMETERS", true)
	}
      }
    }
  }
  wrappers {
    timestamps()
    credentialsBinding {
      usernamePassword('GITHUB_USER_NAME', 'GITHUB_PASSWORD', 'github-user-pass-credentials')
    }
  }
}
