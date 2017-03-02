job('trigger_unit_tests_from_builds_repo') {
  label('validation_slave_label')
  logRotator {
    numToKeep(30)
  }
  concurrentBuild()
  parameters {
    stringParam('sha1', '', 'SHA-1 of the commit to validate.')
  }
  properties {
    githubProjectUrl("https://github.com/${GITHUB_ORGANIZATION_NAME}/builds/")
  }
  scm {
    git {
      remote {
	url("https://github.com/${GITHUB_ORGANIZATION_NAME}/builds.git")
	refspec('+refs/pull/*:refs/remotes/origin/pr/*')
      }
      branch('$sha1')
    }
  }
  triggers {
    githubPullRequest {
      userWhitelist("${GHPRB_ADMIN_USER}")
      orgWhitelist("${GHPRB_ADMIN_ORGANIZATION}")
      allowMembersOfWhitelistedOrgsAsAdmin()
      cron('H/5 * * * *')
      triggerPhrase('.*start\\W+(tests|unittests).*')
      extensions {
	commitStatus {
	  context('Unit tests')
	}
      }
    }
  }
  steps {
    shell(
      readFileFromWorkspace(
	'jenkins_jobs/trigger_unit_tests_from_builds_repo/script.sh'))
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
