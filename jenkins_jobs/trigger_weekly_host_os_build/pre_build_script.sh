VERSIONS_REPOSITORY_URL="https://github.com/${GITHUB_ORGANIZATION_NAME}/versions.git"
RELEASE_DATE=$(date +%Y-%m-%d)
COMMIT_BRANCH="weekly-${RELEASE_DATE}"

create_pull_request() {
    local dest_repo=$1

    # the GITHUB_USER_NAME and GITHUB_PASSWORD variables below refer to
    # the credentials owner, which is not necessarily the same as the
    # source repo owner - GITHUB_BOT_USER_NAME in this case
    github_api "$GITHUB_USER_NAME" "$GITHUB_PASSWORD" \
        open_pr "Weekly build" "${GITHUB_BOT_USER_NAME}:${COMMIT_BRANCH}" \
	"${GITHUB_ORGANIZATION_NAME}/${dest_repo}" "master"
}

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

create_pull_request "versions"

python host_os.py \
       --verbose \
       release-notes \
           --build-versions-repository-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/versions.git" \
           --build-version "$COMMIT_BRANCH" \
           --updater-name "$GITHUB_BOT_NAME" \
           --updater-email "$GITHUB_BOT_EMAIL" \
           --push-repo-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/${GITHUB_ORGANIZATION_NAME}.github.io.git" \
           --push-repo-branch "$COMMIT_BRANCH"

create_pull_request "${GITHUB_ORGANIZATION_NAME}.github.io"

echo "VERSIONS_REPO_COMMIT=$COMMIT_BRANCH" > BUILD_PARAMETERS
echo "$RELEASE_DATE" > WEEKLY_DIR_NAME
