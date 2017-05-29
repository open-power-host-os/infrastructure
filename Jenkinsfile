node ('master') {
  dir('infrastructure') {
    checkout scm
  }
  pipeline = load 'infrastructure/pipeline/build.groovy'
}

pipeline.execute()
