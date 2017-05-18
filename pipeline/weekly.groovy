#!groovy

pipelineStages = load 'infrastructure/pipeline/weekly/stages.groovy'

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
          // We need to preserve the builds repository to later tag the
          // correct commit
          dir ('build-packages') {
            pipelineStages.buildPackages()
          }
        }

        if (currentBuild.result != 'FAILURE') {
          stage('Build ISO') {
            dir ('build-iso') {
              pipelineStages.buildIso()
            }
          }
        }

        stage('Upload build artifacts') {
          dir ('upload') {
            pipelineStages.uploadBuildArtifacts()
          }
        }

        stage('Build release notes') {
          dir ('build-release-notes') {
            pipelineStages.createReleaseNotes()
          }
        }

        stage('Commit to Git repository') {
          pipelineStages.commitToGitRepo()
        }

        stage('Create symlinks') {
          pipelineStages.createSymlinks()
        }

        stage('Tag Git repositories') {
          pipelineStages.tagGitRepos()
        }
      }
    }
  }
}

return this
