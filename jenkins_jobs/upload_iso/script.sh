BUILD_TIMESTAMP=$(cat BUILD_TIMESTAMP)
BUILD_DIR_PATH="${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_TIMESTAMP}"
BUILD_DIR_RSYNC_URL="${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${BUILD_DIR_PATH}"
ISO_DIR_RSYNC_URL="${BUILD_DIR_RSYNC_URL}/iso"

rsync_upload() {
    rsync -e "ssh -i ${HOME}/.ssh/upload_server_id_rsa" \
              --verbose --compress --stats --times --chmod=a+rwx,g+wx,o- \
              --ignore-existing --itemize-changes \
              $@ $ISO_DIR_RSYNC_URL
}

# Create remote iso directory
mkdir iso
rsync_upload --recursive iso

rsync_upload *.iso
rsync_upload *-CHECKSUM
