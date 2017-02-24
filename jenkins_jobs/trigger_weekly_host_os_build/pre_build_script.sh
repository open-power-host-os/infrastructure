set -e

VERSIONS_REPOSITORY_URL="https://github.com/${GITHUB_ORGANIZATION_NAME}/versions.git"
RELEASE_DATE=$(date +%Y-%m-%d)
COMMIT_BRANCH="weekly-${RELEASE_DATE}"

# the GITHUB_USER_NAME and GITHUB_PASSWORD variables below refer to
# the credentials owner, which is not necessarily the same as the
# source repo owner - GITHUB_BOT_USER_NAME in this case
alias github="github_api $GITHUB_USER_NAME $GITHUB_PASSWORD"

create_pull_request() {
    local dest_repo=$1

    # the 'eval' sets the variable 'pr_number' to the number of the
    # new pull-request
    eval $(github open_pr "Weekly build" "${GITHUB_BOT_USER_NAME}:${COMMIT_BRANCH}" \
          "${GITHUB_ORGANIZATION_NAME}/${dest_repo}" "master" || echo "exit 1")
}

write_comment() {
    local comment_text="$1"

    github write_comment "${GITHUB_ORGANIZATION_NAME}/versions" \
       "$pr_number" "$comment_text"
}

get_build_state(){
    local target_context="Build Host OS"

    # this will evaluate the queried keys as variables: state=<state>,
    # target_url=<url>
    eval $(github_api "$GITHUB_USER_NAME" "$GITHUB_PASSWORD" \
                      query_status "${GITHUB_ORGANIZATION_NAME}/versions" \
                      "$VERSIONS_PR_NUMBER" "$target_context" --state \
                      --target-url || echo "exit 1")
}

get_pr_state(){
    local pr_number=$1
    local repo=$2

    # this will evaluate the queried keys as variables: state=<state>,
    # title=<title>
    eval $(github_api "$GITHUB_USER_NAME" "$GITHUB_PASSWORD" \
                      query_pr "${GITHUB_ORGANIZATION_NAME}/$repo" \
                      "$pr_number" --state --title --merged || echo "exit 1")
}

upgrade_versions() {
    python host_os.py \
           --verbose \
           upgrade-versions \
               --build-versions-repository-url "$VERSIONS_REPOSITORY_URL" \
               --build-version "$VERSIONS_REPOSITORY_BRANCH" \
               --updater-name "$GITHUB_BOT_NAME" \
               --updater-email "$GITHUB_BOT_EMAIL" \
               --push-repo-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/versions.git" \
               --push-repo-branch "$COMMIT_BRANCH"
}

create_release_notes() {
    python host_os.py \
           --verbose \
           release-notes \
               --build-versions-repository-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/versions.git" \
               --build-version "$COMMIT_BRANCH" \
               --updater-name "$GITHUB_BOT_NAME" \
               --updater-email "$GITHUB_BOT_EMAIL" \
               --push-repo-url "ssh://git@github.com/${GITHUB_BOT_USER_NAME}/${GITHUB_ORGANIZATION_NAME}.github.io.git" \
               --push-repo-branch "$COMMIT_BRANCH"
}

wait_pull_request_merge() {
    local pr_number=$1
    local repo=$2

    get_pr_state $pr_number $repo
    while [ $state == "open" ]; do
        echo "Waiting for pull-request ${pr_number}: $title to be merged..."
        sleep 1m
        get_pr_state $pr_number $repo
    done

    if [ $merged == "False" ]; then
        echo "Pull-request $pr_number is closed but wasn't merged, aborting..."
        exit 1
    fi
}

fetch_build_timestamp() {
    get_build_state
    local artifacts_src_build_number=$(basename $target_url)
    local artifacts_url=$(basename $JENKINS_URL):${JENKINS_HOME}/jobs/build_host_os/builds/${artifacts_src_build_number}/archive

    rsync -e "ssh -i ${HOME}/.ssh/jenkins_id_rsa" \
              --verbose --compress --stats --times --perms \
              $artifacts_url/BUILD_TIMESTAMP .
}

create_symlinks() {
    local build_dir_path="../to_build/$(cat BUILD_TIMESTAMP)"

    ln -s "$build_dir_path" "$RELEASE_DATE"
    ln -s "$RELEASE_DATE" latest

    rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
          --verbose --compress --links --times --chmod=a+rwx,g+rwx,o- \
          "$RELEASE_DATE" "latest" \
          "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_WEEKLY_DIR}/"
}

upgrade_versions
create_pull_request "versions"
VERSIONS_PR_NUMBER=$pr_number

write_comment "$BUILD_ISO_TRIGGER_PHRASE"

create_release_notes
create_pull_request "${GITHUB_ORGANIZATION_NAME}.github.io"
GITHUB_IO_PR_NUMBER=$pr_number

wait_pull_request_merge $VERSIONS_PR_NUMBER "versions"
wait_pull_request_merge $GITHUB_IO_PR_NUMBER "${GITHUB_ORGANIZATION_NAME}.github.io"

fetch_build_timestamp
create_symlinks
