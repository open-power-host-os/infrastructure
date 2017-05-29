#!groovy

pipelineStages = load 'infrastructure/pipeline/build/stages.groovy'

def execute() {
  timestamps{
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

    stage('Build packages') {
      node('builds_slave_label') {
        pipelineStages.buildPackages()
      }
    }

    if (currentBuild.result != 'FAILURE') {
      stage('Build ISO') {
        node('builds_slave_label') {
          pipelineStages.buildIso()
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
