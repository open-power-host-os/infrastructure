def createPeriodicPipeline(String name) {
  pipelineJob(name) {
    definition {
      cps {
        script("""\
node('master') {
  dir('infrastructure') {
    git(url: 'https://github.com/$GITHUB_ORGANIZATION_NAME/infrastructure.git',
        branch: '$REPOSITORY_COMMIT')
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
