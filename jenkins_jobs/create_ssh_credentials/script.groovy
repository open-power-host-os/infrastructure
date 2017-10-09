import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;

String credentialsId = build.buildVariableResolver.resolve("CREDENTIALS_ID")
String userName = build.buildVariableResolver.resolve("USER_NAME")
String sshPrivateKeyPass = build.buildVariableResolver.resolve("SSH_PRIVATE_KEY_PATH")

global_domain = Domain.global()
credentials_store = Jenkins.instance.getExtensionList(
    'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
    )[0].getStore()

oldCredentials = credentials_store.getCredentials(global_domain).findResult {
  it.id == credentialsId ? it : null
}
if (oldCredentials != null) {
  println("Removing old credentials with ID '$credentialsId'")
  credentials_store.removeCredentials(global_domain, oldCredentials)
}

newCredentials = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    credentialsId,
    userName,
    new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(
        sshPrivateKeyPass), "", ""
    )
println("Adding new credentials with ID '$credentialsId' and user name '$userName'")
credentials_store.addCredentials(global_domain, newCredentials)
