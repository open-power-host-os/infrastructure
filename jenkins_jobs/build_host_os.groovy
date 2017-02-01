job('build_host_os') {
  label('builds_slave_label')
  logRotator {
    numToKeep(30)
  }
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
    stringParam('EXTRA_PARAMETERS', '', 'Arbitrary extra parameters to pass to the builds script. Arguments containing spaces have to be enclosed in double quotes, e.g. --mock-args "--with tests"')
    stringParam('CENTOS_ALTERNATE_MIRROR_RELEASE_URL',
		"${CENTOS_ALTERNATE_MIRROR_RELEASE_URL}",
		'URL up to the release component of a CentOS YUM repository alternate mirror. Empty to use CentOS latest release offical repository.')
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
    archiveArtifacts('SUCCESS')
    archiveArtifacts('BUILD_TIMESTAMP')
    archiveArtifacts('repository/')
    archiveArtifacts {
      pattern('build/*/*/*.log')
      allowEmpty()
    }
    downstreamParameterized {
      trigger('upload_build_artifacts') {
	condition('FAILED_OR_BETTER')
	parameters {
	  predefinedProp('BUILD_JOB_NUMBER', '$BUILD_NUMBER')
	}
      }
    }
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
