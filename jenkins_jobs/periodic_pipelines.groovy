def createPeriodicPipeline(String name) {
  pipelineJob(name) {
    definition {
      cps {
        script("""\
node {
  Map constants = readProperties file: '/etc/jenkins/pipeline_constants.groovy'

  dir('infrastructure') {
    git(url: 'https://github.com/$GITHUB_ORGANIZATION_NAME/infrastructure.git',
        branch: 'pipeline')
  }
  pipeline = load 'infrastructure/pipeline/${name}.groovy'
}

pipeline.execute()
""")
        sandbox()
      }
    }
  }
}

createPeriodicPipeline('daily')
createPeriodicPipeline('weekly')
