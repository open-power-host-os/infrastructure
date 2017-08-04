job('trigger_host_os_admin_commands_from_builds_repo') {
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
  }
  properties {
    githubProjectUrl("https://github.com/${GITHUB_ORGANIZATION_NAME}/builds/")
  }
  triggers {
    githubPullRequest {
      userWhitelist("${GHPRB_ADMIN_USER}")
      orgWhitelist("${GHPRB_ADMIN_ORGANIZATION}")
      allowMembersOfWhitelistedOrgsAsAdmin()
      cron('H/5 * * * *')
      triggerPhrase('.*start\\W+(tests|admin\\W+commands).*')
      extensions {
	commitStatus {
	  context('Administrative commands')
	}
      }
    }
  }
  steps {
    downstreamParameterized {
      trigger('run_host_os_admin_commands') {
	block {
	  buildStepFailure('FAILURE')
	  failure('FAILURE')
	  unstable('UNSTABLE')
	}
	parameters {
	  predefinedProps([BUILDS_REPO_COMMIT: '$sha1',
			   VERSIONS_REPO_COMMIT: 'master'])
	}
      }
    }
  }
  wrappers {
    timestamps()
  }
}
