List pipelineParameters = [
  string(name: 'GITHUB_ORGANIZATION_NAME',
         defaultValue: constants.GITHUB_ORGANIZATION_NAME,
         description: 'GitHub organization where the repositories are ' +
         'located.'),
  string(name: 'GITHUB_AUTHORIZED_USERS',
         defaultValue: constants.GITHUB_AUTHORIZED_USERS,
         description: 'GitHub users authorized to start builds.'),

  string(name: 'BUILDS_WORKSPACE_DIR',
         defaultValue: constants.BUILDS_WORKSPACE_DIR,
         description: 'Workspace directory where builds will happen.'),
  string(name: 'MOCK_CONFIG_FILE',
         defaultValue: constants.MOCK_CONFIG_FILE,
         description: 'Mock configuration file path.'),
  string(name: 'BUILDS_CONFIG_FILE',
         defaultValue: constants.BUILDS_CONFIG_FILE,
         description: 'Builds configuration file path.'),

  string(name: 'MAIN_CENTOS_REPO_RELEASE_URL',
         defaultValue: constants.MAIN_CENTOS_REPO_RELEASE_URL,
         description:
         'URL up to the release component of the main CentOS YUM ' +
         'repository, which will be replaced by an alternate mirror.'),
  string(name: 'MAIN_EPEL_REPO_RELEASE_URL',
         defaultValue: constants.MAIN_EPEL_REPO_RELEASE_URL,
         description:
         'URL up to the release component of the main EPEL YUM ' +
         'repository, which will be replaced by an alternate mirror.'),
  string(name: 'CENTOS_ALTERNATE_MIRROR_RELEASE_URL',
         defaultValue: constants.CENTOS_ALTERNATE_MIRROR_RELEASE_URL,
         description:
         'URL up to the release component of a CentOS YUM repository ' +
         'alternate mirror. Empty to use CentOS latest release ' +
         'official repository.'),
  string(name: 'EPEL_ALTERNATE_MIRROR_RELEASE_URL',
         defaultValue: constants.EPEL_ALTERNATE_MIRROR_RELEASE_URL,
         description:
         'URL up to the release component of an EPEL YUM repository ' +
         'alternate mirror. Empty to use EPEL latest release ' +
         'official repository.'),

  string(name: 'UPLOAD_SERVER_HOST_NAME',
         defaultValue: constants.UPLOAD_SERVER_HOST_NAME,
         description:
         'Host name of the target server to upload build results.'),
  string(name: 'UPLOAD_SERVER_USER_NAME',
         defaultValue: constants.UPLOAD_SERVER_USER_NAME,
         description:
         'User name of the target server to upload build results.'),
  string(name: 'UPLOAD_SERVER_BUILDS_DIR_PATH',
         defaultValue: constants.UPLOAD_SERVER_BUILDS_DIR_PATH,
         description:
         'Directory in the target server to upload build results.'),

  string(name: 'BUILDS_REPO_REFERENCE',
         defaultValue: 'master',
         description:
         'Git reference to checkout from the builds repository.'),
  string(name: 'VERSIONS_REPO_REFERENCE',
         defaultValue: 'master',
         description:
         'Git reference to checkout from the versions repository.'),

  string(name: 'PACKAGES',
         defaultValue: '',
         description: 'Packages to build. Leave empty to build all.'),
  string(name: 'BUILD_PACKAGES_EXTRA_PARAMETERS',
         defaultValue: '',
         description: 'Arbitrary extra parameters to pass to the ' +
         'build-packages command. Arguments containing spaces have to ' +
         'be enclosed in double quotes, e.g. --mock-args "--with tests"'),
  string(name: 'BUILD_ISO_EXTRA_PARAMETERS',
         defaultValue: '',
         description: 'Arbitrary extra parameters to pass to the ' +
         'build-iso command. Arguments containing spaces have to ' +
         'be enclosed in double quotes, e.g. --mock-args "--with tests"'),
]

return pipelineParameters
