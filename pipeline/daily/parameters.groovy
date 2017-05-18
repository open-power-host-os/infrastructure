Map constants = readProperties file: '/etc/jenkins/pipeline_constants.groovy'

List pipelineParameters = load 'infrastructure/pipeline/build/parameters.groovy'
pipelineParameters += [
  string(name: 'GITHUB_BOT_NAME',
         defaultValue: constants.GITHUB_BOT_NAME,
         description:
         'Name of the GitHub user to create commits automatically.'),
  string(name: 'GITHUB_BOT_USER_NAME',
         defaultValue: constants.GITHUB_BOT_USER_NAME,
         description:
         'User name of the GitHub user to create commits automatically.'),
  string(name: 'GITHUB_BOT_EMAIL',
         defaultValue: constants.GITHUB_BOT_EMAIL,
         description:
         'Email of the GitHub user to create commits automatically.'),

  string(name: 'UPLOAD_SERVER_PERIODIC_BUILDS_DIR_PATH',
         defaultValue: constants.UPLOAD_SERVER_NIGHTLY_DIR_PATH,
         description:
         'Directory in the target server to upload periodic builds results.'),
]

return pipelineParameters
