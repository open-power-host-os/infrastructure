def createPeriodicPipeline(String name, String cronExpression) {
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
    triggers {
      cron(cronExpression)
    }
  }
}

createPeriodicPipeline('devel', "$NIGHTLY_BUILDS_CRON_EXPRESSION")
createPeriodicPipeline('release', "$NIGHTLY_BUILDS_CRON_EXPRESSION")
