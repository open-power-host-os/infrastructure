BUILD_TIMESTAMP=$(cat BUILD_TIMESTAMP)
BUILDS_REPO_COMMIT=$(cat BUILDS_REPO_COMMIT)
VERSIONS_REPO_COMMIT=$(cat VERSIONS_REPO_COMMIT)
BUILD_DIR_PATH="${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_TIMESTAMP}"
BUILD_DIR_HTTP_URL="http://${UPLOAD_SERVER_HOST_NAME}${BUILD_DIR_PATH}"
BUILD_DIR_RSYNC_URL="${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${BUILD_DIR_PATH}"

if [ -f SUCCESS ]; then
    REPOSITORY_FILE_URL="${BUILD_DIR_HTTP_URL}/hostos.repo"
    BUILD_STATUS='PASS'
else
    REPOSITORY_FILE_URL='null'
    BUILD_STATUS='FAIL'
fi

cat <<EOF > STATUS
{
    "BUILDS_REPO_COMMIT": "$BUILDS_REPO_COMMIT",
    "BUILD_ID": "$BUILD_JOB_NUMBER",
    "BUILD_LOG": "$BUILD_URL",
    "BUILD_STATUS": "$BUILD_STATUS",
    "BUILD_TIMESTAMP": "$BUILD_TIMESTAMP",
    "REPO": "$REPOSITORY_FILE_URL",
    "VERSIONS_REPO_COMMIT": "$VERSIONS_REPO_COMMIT"
}
EOF

rsync_upload() {
    rsync -e "ssh -i ${HOME}/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
              --verbose --compress --stats --times --chmod=a+rwx,g+wx,o- \
              $@ $BUILD_DIR_RSYNC_URL
}

# Create remote build directory
mkdir $BUILD_TIMESTAMP
# This needs the "./" because the timestamp format confuses rsync
rsync_upload --recursive ./$BUILD_TIMESTAMP

rsync_upload STATUS
rsync_upload --recursive repository

# Create hostos.repo
echo -e """[hostos]
name=hostos
baseurl=${BUILD_DIR_HTTP_URL}/repository
enabled=1
priority=1
gpgcheck=0""" > hostos.repo

rsync_upload hostos.repo
