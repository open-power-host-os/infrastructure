import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;

global_domain = Domain.global()
credentials_store = Jenkins.instance.getExtensionList(
    'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
    )[0].getStore()
credentials = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    build.buildVariableResolver.resolve("CREDENTIALS_ID"),
    build.buildVariableResolver.resolve("USER_NAME"),
    new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(
        build.buildVariableResolver.resolve("SSH_PRIVATE_KEY_PATH")), "", ""
    )
credentials_store.addCredentials(global_domain, credentials)
