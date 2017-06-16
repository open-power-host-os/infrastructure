#!groovy

pipelineStages = load 'infrastructure/pipeline/daily/stages.groovy'

def execute() {
  timestamps {
    stage('Initialize') {
      pipelineStages.initialize()
    }

    node('builds_slave_label') {
      lock(resource: "update-versions_workspace_$env.NODE_NAME") {
        stage('Update packages versions') {
          pipelineStages.updateVersions()
        }

        stage('Build packages') {
          pipelineStages.buildPackages()
        }

        if (currentBuild.result != 'FAILURE') {
          stage('Build ISO') {
            pipelineStages.buildIso()
          }
        }

        stage('Upload build artifacts') {
          pipelineStages.uploadBuildArtifacts()
        }

        stage('Create symlinks') {
          pipelineStages.createSymlinks()
        }
      }
    }
  }
}

return this
