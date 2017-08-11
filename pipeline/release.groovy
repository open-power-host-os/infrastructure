#!groovy

constants = readProperties file: '/etc/jenkins/pipeline_constants.groovy'
develPipeline = load 'infrastructure/pipeline/devel.groovy'
pipelineParameters = load 'infrastructure/pipeline/release/parameters.groovy'

def execute() {
  develPipeline.execute()
}

return this
