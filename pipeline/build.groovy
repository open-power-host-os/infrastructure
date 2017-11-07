#!groovy

constants = readProperties file: '/etc/jenkins/pipeline_constants.groovy'
utils = load 'infrastructure/pipeline/lib/utils.groovy'

pipelineStages = load 'infrastructure/pipeline/build/stages.groovy'


def execute() {
  timestamps {
    stage('Initialize') {
      pipelineStages.initialize()
    }

    stage('Authorize') {
      pipelineStages.authorize()
    }

    stage('Validate') {
      node('validation_slave_label') {
        pipelineStages.validate()
      }
    }

    catchError {
      stage('Build packages') {
        node('builds_slave_label') {
          pipelineStages.buildPackages()
        }
      }
    }

    catchError {
      stage('Build images') {
        node('builds_slave_label') {
          pipelineStages.buildImages()
        }
      }
    }

    stage('Upload') {
      node('builds_slave_label') {
        pipelineStages.uploadArtifacts()
      }
    }
  }
}

return this
