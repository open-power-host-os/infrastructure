pipelineParameters += [
  CENTOS_ALTERNATE_MIRROR_RELEASE_URL: [
    defaultValue: 'http://mirror.centos.org/altarch/7.5.1804',
    description:
      'URL up to the release component of a CentOS YUM repository ' +
      'alternate mirror. Empty to use CentOS latest release ' +
      'official repository.'],
  UPLOAD_SERVER_PERIODIC_BUILDS_DIR_PATH: [
    defaultValue: constants.UPLOAD_SERVER_RELEASE_DIR_PATH,
    description:
      'Directory in the target server to upload periodic builds results.' ],
  VERSIONS_REPO_REFERENCE: [
    defaultValue: 'hostos-release',
    description: 'Git reference to checkout from the versions repository.'],
]

return pipelineParameters
