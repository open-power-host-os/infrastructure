NIGHTLY_DIR_NAME=$(cat NIGHTLY_DIR_NAME)
BUILD_TIMESTAMP=$(cat BUILD_TIMESTAMP)
BUILD_DIR_PATH="../builds/${BUILD_TIMESTAMP}"
NIGHLTY_DIR_RSYNC_URL="${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_NIGHTLY_DIR}"

rsync_upload() {
    rsync -e "ssh -i ${HOME}/.ssh/upload_server_id_rsa" \
          --verbose --compress --links --stats --times --chmod=a+rwx,g+wx,o- \
          $@ $NIGHLTY_DIR_RSYNC_URL
}

# update nightly build dir and latest nightly build
ln -s "$BUILD_DIR_PATH" "$NIGHTLY_DIR_NAME"
ln -s "$NIGHTLY_DIR_NAME" latest

rsync_upload "$NIGHTLY_DIR_NAME"
rsync_upload latest
