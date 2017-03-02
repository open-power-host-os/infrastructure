VERSIONS_REPOSITORY_URL="https://github.com/${GITHUB_ORGANIZATION_NAME}/versions.git"
RELEASE_DATE=$(date +%Y-%m-%d)
COMMIT_BRANCH="nightly-${RELEASE_DATE}"

# Upgrade versions
# sudo yum install rpmdevtools
python host_os.py \
       --verbose \
       upgrade-versions \
           --build-versions-repository-url "$VERSIONS_REPOSITORY_URL" \
           --build-version "$VERSIONS_REPOSITORY_BRANCH" \
           --updater-name "$GITHUB_BOT_NAME" \
           --updater-email "$GITHUB_BOT_EMAIL" \
           --push-repo-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/versions.git" \
           --push-repo-branch "$COMMIT_BRANCH"

echo "VERSIONS_REPO_COMMIT=$COMMIT_BRANCH" > BUILD_PARAMETERS
echo "$RELEASE_DATE" > NIGHTLY_DIR_NAME
