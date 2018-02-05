#!groovy

constants = readProperties file: '/etc/jenkins/pipeline_constants.groovy'
utils = load 'infrastructure/pipeline/lib/utils.groovy'

pipelineStages = load 'infrastructure/pipeline/daily/stages.groovy'
pipelineParameters = load 'infrastructure/pipeline/devel/parameters.groovy'


def execute(Boolean skipIfNoUpdates = false, releaseCategory = 'devel') {
  timestamps {
    try {
      stage('Initialize') {
        pipelineStages.initialize()
      }

      node('builds_slave_label') {
        lock(resource: "update-versions_workspace_$env.NODE_NAME") {
          stage('Update packages versions') {
            pipelineStages.updateVersions(releaseCategory)
          }

          if (skipIfNoUpdates && !pipelineStages.hasUpdates) {
            echo 'No updates, skipping build'
            return
          }

          stage('Build packages') {
            pipelineStages.buildPackages()
          }

          if (currentBuild.result != 'FAILURE') {
            stage('Build images') {
              pipelineStages.buildImages()
            }
          }

          stage('Upload build artifacts') {
            pipelineStages.uploadBuildArtifacts()
          }

          stage('Build release notes') {
            pipelineStages.createReleaseNotes(releaseCategory)
          }

          stage('Run BVT') {
            node('bvt_slave_label') {
              pipelineStages.runBVT()
            }
          }

          if (currentBuild.result == 'UNSTABLE') {
            pipelineStages.notifyUnstable()
          }

          stage('Commit to Git repository') {
            pipelineStages.commitToGitRepo()
          }

          stage('Create symlinks') {
            pipelineStages.createSymlinks()
          }
        }
      }
    } catch (Exception exception) {
      pipelineStages.notifyFailure()
      throw exception
    }
  }
}

return this
