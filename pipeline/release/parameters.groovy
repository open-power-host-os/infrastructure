pipelineParameters += [
  UPLOAD_SERVER_PERIODIC_BUILDS_DIR_PATH: [
    defaultValue: constants.UPLOAD_SERVER_RELEASE_DIR_PATH,
    description:
      'Directory in the target server to upload periodic builds results.' ],
  VERSIONS_REPO_REFERENCE: [
    defaultValue: 'hostos-release',
    description: 'Git reference to checkout from the versions repository.'],
  BUILD_PACKAGES_EXTRA_PARAMETERS: [
    defaultValue: '--mock-args "--define \'extraver .rel\'"',
    description: 'Arbitrary extra parameters to pass to the build-packages ' +
      'command. Arguments containing spaces have to be enclosed in double ' +
      'quotes, e.g. --mock-args "--with tests"'],
]

return pipelineParameters
