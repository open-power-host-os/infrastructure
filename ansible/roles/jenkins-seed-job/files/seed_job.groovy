node ('master') {
      git credentialsId: 'github-user-pass-credentials',
          url: REPOSITORY_URL,
          branch: REPOSITORY_COMMIT
      jobDsl targets: JOB_DESCRIPTORS_FILES
}
