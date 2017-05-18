#!groovy

pipelineStages = load 'infrastructure/pipeline/weekly/stages.groovy'

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

    stage('Create release notes') {
      node('builds_slave_label') {
        // pipelineStages.createReleaseNotes()
      }
    }

    stage('Create symlinks') {
      node('builds_slave_label') {
        pipelineStages.createSymlinks()
      }
    }

    stage('Tag Git repositories') {
      node('builds_slave_label') {
        pipelineStages.tagGitRepos()
      }
    }
  }
}

return this
