job('create_user_pass_credentials') {
  label('master')
  parameters {
    stringParam('CREDENTIALS_ID', 'github-user-pass-credentials',
		'Credentials ID.')
    stringParam('USER_NAME', "${GITHUB_BOT_USER_NAME}", 'User name.')
    nonStoredPasswordParam('PASSWORD', 'User password.')
  }
  steps {
    systemGroovyCommand(
      readFileFromWorkspace(
	'jenkins_jobs/create_user_pass_credentials/script.groovy'))
  }
}
