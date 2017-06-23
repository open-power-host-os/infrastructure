def createPeriodicPipeline(String name, String cronExpression) {
  pipelineJob(name) {
    definition {
      cps {
        script("""\
node('master') {
  dir('infrastructure') {
    git(url: 'https://github.com/$GITHUB_ORGANIZATION_NAME/infrastructure.git',
        branch: 'master')
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

createPeriodicPipeline('daily', "$NIGHTLY_BUILDS_CRON_EXPRESSION")
createPeriodicPipeline('weekly', "$WEEKLY_BUILDS_CRON_EXPRESSION")
