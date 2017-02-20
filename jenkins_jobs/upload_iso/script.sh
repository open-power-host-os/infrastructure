BUILD_TIMESTAMP=$(cat BUILD_TIMESTAMP)
BUILD_DIR_PATH="${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_TIMESTAMP}"
BUILD_DIR_RSYNC_URL="${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${BUILD_DIR_PATH}"
ISO_DIR_RSYNC_URL="${BUILD_DIR_RSYNC_URL}/iso"

alias rsync_upload="rsync -e \
    'ssh -i ${HOME}/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa' \
    --verbose --compress --stats --times --chmod=a+rwx,g+wx,o-"

# Create remote iso directory
mkdir iso
rsync_upload --recursive iso $BUILD_DIR_RSYNC_URL

rsync_upload *.iso $ISO_DIR_RSYNC_URL
rsync_upload *-CHECKSUM $ISO_DIR_RSYNC_URL
