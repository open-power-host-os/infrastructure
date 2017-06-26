#!groovy

import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import hudson.util.Secret
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl


def addSecretString(Secret secret, String description) {

  credentialsId = java.util.UUID.randomUUID().toString()

  credentials = new StringCredentialsImpl(
    CredentialsScope.GLOBAL, credentialsId, description, secret)

  getCredentialsStore().addCredentials(Domain.global(), credentials)

  return credentialsId
}

def removeCredentials(String credentialsId) {
  domain = Domain.global()

  credentialsMatched = CredentialsMatchers.filter(
    getCredentialsStore().getCredentials(domain),
    CredentialsMatchers.withId(credentialsId))

  // A credentialsId should only match a single set of credentials, so
  // simply take the first match here
  credentials = credentialsMatched.get(0)

  getCredentialsStore().removeCredentials(domain, credentials)
}


def getCredentialsStore() {
  return SystemCredentialsProvider.getInstance().getStore()
}

return this
