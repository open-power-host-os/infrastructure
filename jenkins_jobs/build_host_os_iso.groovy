job('build_host_os_iso') {
  label('builds_slave_label')
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
    stringParam('BUILD_JOB_NUMBER', '',
                'Number of the Host OS build job that built the packages.')
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
    copyArtifacts('build_host_os') {
      buildSelector {
        buildNumber('$BUILD_JOB_NUMBER')
      }
      includePatterns('repository/')
    }
    shell(readFileFromWorkspace('jenkins_jobs/build_host_os_iso/script.sh'))
  }
  publishers {
    archiveArtifacts('*.iso')
    archiveArtifacts('*-CHECKSUM')
  }
  wrappers {
    timestamps()
    preBuildCleanup()
  }
}
