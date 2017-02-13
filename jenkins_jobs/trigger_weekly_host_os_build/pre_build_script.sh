VERSIONS_REPO_URL="https://github.com/${GITHUB_ORGANIZATION_NAME}/versions.git"
RELEASE_DATE=$(date +%Y-%m-%d)
COMMIT_BRANCH="weekly-${RELEASE_DATE}"

# Upgrade versions
# sudo yum install rpmdevtools
python host_os.py \
   --verbose \
   upgrade-versions \
       --updater-name "$GITHUB_BOT_NAME" \
       --updater-email "$GITHUB_BOT_EMAIL" \
       --push-repo-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/versions.git" \
       --push-repo-branch "$COMMIT_BRANCH"

python host_os.py \
    --verbose \
    release-notes \
        --build-versions-repository-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/versions.git" \
	--build-version "$COMMIT_BRANCH" \
	--updater-name "$GITHUB_BOT_NAME" \
	--updater-email "$GITHUB_BOT_EMAIL" \
	--push-repo-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/${GITHUB_ORGANIZATION_NAME}.github.io.git" \
	--push-repo-branch "$COMMIT_BRANCH"

echo "VERSIONS_REPO_COMMIT=$COMMIT_BRANCH" > BUILD_PARAMETERS
echo "$RELEASE_DATE" > WEEKLY_DIR_NAME
