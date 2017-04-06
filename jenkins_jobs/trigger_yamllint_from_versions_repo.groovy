job('trigger_yamllint_from_versions_repo') {
  label('validation_slave_label')
  concurrentBuild()
  parameters {
    stringParam('BUILDS_REPO_URL',
		"https://github.com/${GITHUB_ORGANIZATION_NAME}/builds.git",
		'URL of the builds repository.')
    stringParam('BUILDS_REPO_REFERENCE', 'origin/master',
		'Git reference to checkout from the builds repository.')
    stringParam('VERSIONS_REPO_URL',
		"https://github.com/${GITHUB_ORGANIZATION_NAME}/versions.git",
		'URL of the versions repository.')
    stringParam('sha1', 'origin/master',
		'Commit ID to checkout from the versions repository.')
  }
  properties {
    githubProjectUrl("https://github.com/${GITHUB_ORGANIZATION_NAME}/versions/")
  }
  scm {
    git {
      remote {
	    url('$BUILDS_REPO_URL')
      }
      branch('$BUILDS_REPO_REFERENCE')
    }
  }
  triggers {
    githubPullRequest {
      userWhitelist("${GHPRB_ADMIN_USER}")
      orgWhitelist("${GHPRB_ADMIN_ORGANIZATION}")
      allowMembersOfWhitelistedOrgsAsAdmin()
      cron('H/5 * * * *')
      triggerPhrase('.*start\\W+(tests|yamllint).*')
      extensions {
	commitStatus {
	  context('YAMLlint')
	}
      }
    }
  }
  steps {
    shell(
      readFileFromWorkspace(
	    'jenkins_jobs/trigger_yamllint_from_versions_repo/script.sh'))
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
