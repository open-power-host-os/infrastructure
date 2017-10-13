import jenkins.model.*
import hudson.model.*
import hudson.plugins.sshslaves.*
import hudson.slaves.*

Jenkins.instance.addNode(
  new DumbSlave(
    params.NODE_NAME,
    params.NODE_DESCRIPTION,
    params.NODE_USER_HOME,
    params.NUMBER_OF_EXECUTORS,
    Node.Mode.NORMAL,
    params.NODE_LABEL,
    new SSHLauncher(params.IP_ADDRESS, 22,
		    params.CREDENTIALS_ID,
		    null, null, null, null),
    new RetentionStrategy.Always(),
    new LinkedList()))
