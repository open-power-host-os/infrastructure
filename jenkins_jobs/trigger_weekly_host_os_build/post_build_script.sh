WEEKLY_DIR_NAME=$(cat WEEKLY_DIR_NAME)
BUILD_DIR_NAME=$(rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" --list-only "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_BUILDS_DIR}/" | tr -s ' ' | cut -d ' ' -f 5 | sort -g | tail -n1)
BUILD_DIR_PATH="../to_build/${BUILD_DIR_NAME}"

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
