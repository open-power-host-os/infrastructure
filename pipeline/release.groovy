#!groovy

constants = readProperties file: '/etc/jenkins/pipeline_constants.groovy'
develPipeline = load 'infrastructure/pipeline/devel.groovy'
pipelineParameters = load 'infrastructure/pipeline/release/parameters.groovy'

def execute() {
  Boolean skipIfNoUpdates = true
  develPipeline.execute(skipIfNoUpdates)
}

return this
