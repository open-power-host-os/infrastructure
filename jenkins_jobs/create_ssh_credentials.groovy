job('create_ssh_credentials') {
  label('master')
  parameters {
    stringParam('CREDENTIALS_ID', 'jenkins-user-ssh-credentials',
		'Credentials ID.')
    stringParam('USER_NAME', 'jenkins', 'User name.')
    stringParam('SSH_PRIVATE_KEY_PATH', '/var/lib/jenkins/.ssh/jenkins_id_rsa',
		'Path to SSH private key.')
  }
  steps {
    systemGroovyCommand(
      readFileFromWorkspace('jenkins_jobs/create_ssh_credentials/script.groovy'))
  }
}
