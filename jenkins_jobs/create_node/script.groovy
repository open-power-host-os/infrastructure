import jenkins.model.*
import hudson.model.*
import hudson.plugins.sshslaves.*
import hudson.slaves.*

Jenkins.instance.addNode(
  new DumbSlave(
    build.buildVariableResolver.resolve("NODE_NAME"),
    build.buildVariableResolver.resolve("NODE_DESCRIPTION"),
    build.buildVariableResolver.resolve("NODE_USER_HOME"),
    build.buildVariableResolver.resolve("NUMBER_OF_EXECUTORS"),
    Node.Mode.NORMAL,
    build.buildVariableResolver.resolve("NODE_LABEL"),
    new SSHLauncher(build.buildVariableResolver.resolve("IP_ADDRESS"), 22,
		    build.buildVariableResolver.resolve("CREDENTIALS_ID"),
		    null, null, null, null),
    new RetentionStrategy.Always(),
    new LinkedList()))
