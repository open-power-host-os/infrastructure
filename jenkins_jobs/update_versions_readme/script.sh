python host-os.py \
  --verbose \
  update-versions-readme \
    --build-versions-repository-url ${VERSIONS_REPO_URL} \
    --build-version ${VERSIONS_REPO_COMMIT} \
    --updater-name "${GITHUB_BOT_NAME}" \
    --updater-email "${GITHUB_BOT_EMAIL}" \
    --push-repo-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/versions.git" \
    --push-repo-branch "${COMMIT_BRANCH}"
