import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*;

global_domain = Domain.global()
credentials_store = Jenkins.instance.getExtensionList(
    'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
    )[0].getStore()
credentials = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    build.buildVariableResolver.resolve("CREDENTIALS_ID"),
    "",
    build.buildVariableResolver.resolve("USER_NAME"),
    build.buildVariableResolver.resolve("PASSWORD"))
credentials_store.addCredentials(global_domain, credentials)
