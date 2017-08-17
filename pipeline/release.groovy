#!groovy

constants = readProperties file: '/etc/jenkins/pipeline_constants.groovy'
develPipeline = load 'infrastructure/pipeline/devel.groovy'
pipelineParameters = load 'infrastructure/pipeline/release/parameters.groovy'

def execute() {
  Boolean skipIfNoUpdates = true
  String releaseCategory = 'release'
  develPipeline.execute(skipIfNoUpdates, releaseCategory)
}

return this
