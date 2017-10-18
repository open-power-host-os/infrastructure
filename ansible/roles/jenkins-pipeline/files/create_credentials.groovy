import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;

node ('master') {
  Domain global_domain = Domain.global()
  SystemCredentialsProvider$StoreImpl credentials_store = Jenkins.instance.getExtensionList(
    'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
  )[0].getStore()


  String githubCredentialsId = 'github-user-pass-credentials'
  String githubUserName = params.GITHUB_BOT_USER_NAME
  String githubPassword = params.GITHUB_BOT_ACCESS_TOKEN

  Credentials githubOldCredentials = credentials_store.getCredentials(global_domain).findResult {
    it.id == githubCredentialsId ? it : null
  }
  if (githubOldCredentials != null) {
    println("Removing old credentials with ID '$githubCredentialsId'")
    credentials_store.removeCredentials(global_domain, githubOldCredentials)
  }

  Credentials githubNewCredentials = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    githubCredentialsId,
    "",
    githubUserName,
    githubPassword)
  println("Adding new credentials with ID '$githubCredentialsId' and user name '$githubUserName'")
  credentials_store.addCredentials(global_domain, githubNewCredentials)


  String jenkinsSlaveCredentialsId = 'jenkins-user-ssh-credentials'
  String jenkinsSlaveUserName = params.JENKINS_SLAVE_USER_NAME
  String jenkinsSlavePrivateSSHKeyPath = params.JENKINS_SLAVE_PRIVATE_SSH_KEY_PATH

  Credentials jenkinsSlaveOldCredentials = credentials_store.getCredentials(global_domain).findResult {
    it.id == jenkinsSlaveCredentialsId ? it : null
  }
  if (jenkinsSlaveOldCredentials != null) {
    println("Removing old credentials with ID '$jenkinsSlaveCredentialsId'")
    credentials_store.removeCredentials(global_domain, jenkinsSlaveOldCredentials)
  }

  Credentials jenkinsSlaveNewCredentials = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    jenkinsSlaveCredentialsId,
    jenkinsSlaveUserName,
    new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(
      jenkinsSlavePrivateSSHKeyPath), "", ""
  )
  println("Adding new credentials with ID '$jenkinsSlaveCredentialsId' and user name '$jenkinsSlaveUserName'")
  credentials_store.addCredentials(global_domain, jenkinsSlaveNewCredentials)
}
