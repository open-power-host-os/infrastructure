pipelineParameters += [
  string(name: 'UPLOAD_SERVER_PERIODIC_BUILDS_DIR_PATH',
         defaultValue: constants.UPLOAD_SERVER_WEEKLY_DIR_PATH,
         description: 'Directory in the target server to upload ' +
         'periodic builds results.'),
]

return pipelineParameters
