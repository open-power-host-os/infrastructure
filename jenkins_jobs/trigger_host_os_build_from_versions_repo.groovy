job('trigger_host_os_build_from_versions_repo') {
  label('!master')
  concurrentBuild()
  throttleConcurrentBuilds {
    maxPerNode(1)
  }
  parameters {
    stringParam('sha1', '', 'SHA-1 of the commit to build.')
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
      triggerPhrase('.*start\\W+(tests|build).*')
      extensions {
	commitStatus {
	  context('Build Host OS')
	}
      }
    }
  }
  steps {
    downstreamParameterized {
      trigger('build_host_os') {
	block {
	  buildStepFailure('FAILURE')
	  failure('FAILURE')
	  unstable('UNSTABLE')
	}
	parameters {
	  predefinedProps([BUILDS_REPO_COMMIT: 'master',
			   VERSIONS_REPO_COMMIT: '$sha1'])
	}
      }
    }
  }
  wrappers {
    timestamps()
  }
}
