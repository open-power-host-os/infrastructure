job('create_node') {
  label('master')
  parameters {
    stringParam('NODE_NAME', 'builds_slave', 'Name of the slave node.')
    stringParam('NODE_DESCRIPTION', 'Slave used to build OpenPOWER Host OS',
		'Name of the slave node.')
    stringParam('NODE_USER_HOME', '/home/jenkins/',
		'Path to the user`s home directory in the slave node.')
    stringParam('NUMBER_OF_EXECUTORS', '5', 'Number of executors.')
    stringParam('NODE_LABEL', 'builds_slave_label validation_slave_label',
		'Label(s) of the slave node.')
    stringParam('IP_ADDRESS', '', 'IP address to the slave node.')
    stringParam('CREDENTIALS_ID', 'jenkins-user-ssh-credentials',
		'ID of the credentials used to access this node.')
  }
  steps {
    systemGroovyCommand(
      readFileFromWorkspace('jenkins_jobs/create_node/script.groovy'))
  }
}
