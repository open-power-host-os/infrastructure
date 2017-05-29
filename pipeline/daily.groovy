#!groovy

pipelineStages = load 'infrastructure/pipeline/daily/stages.groovy'

def execute() {
  timestamps{
    stage('Initialize') {
      pipelineStages.initialize()
    }

    stage('Update packages versions') {
      node('builds_slave_label') {
        // pipelineStages.updateVersions()
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

    stage('Upload build artifacts') {
      node('builds_slave_label') {
        pipelineStages.uploadBuildArtifacts()
      }
    }

    stage('Create symlinks') {
      node('builds_slave_label') {
        pipelineStages.createSymlinks()
      }
    }
  }
}

return this
