WEEKLY_DIR_NAME=$(cat WEEKLY_DIR_NAME)
BUILD_TIMESTAMP=$(cat BUILD_TIMESTAMP)
BUILD_DIR_PATH="../to_build/${BUILD_TIMESTAMP}"

# update weekly build dir and latest weekly build
ln -s "$BUILD_DIR_PATH" "$WEEKLY_DIR_NAME"
ln -s "$WEEKLY_DIR_NAME" latest

rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
      --verbose --compress --links --times --chmod=a+rwx,g+rwx,o- \
      "$WEEKLY_DIR_NAME" \
      "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_WEEKLY_DIR}/"
rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
      --verbose --compress --links --times --chmod=a+rwx,g+rwx,o- \
      "latest" \
      "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_WEEKLY_DIR}/"
