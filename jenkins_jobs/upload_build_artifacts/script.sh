BUILD_TIMESTAMP=$(cat ./BUILD_TIMESTAMP)
BASE_URL="http://${UPLOAD_SERVER_HOST_NAME}${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_TIMESTAMP}"

if [ -f SUCCESS ]; then
    REPOSITORY_FILE_URL="${BASE_URL}/hostos.repo"
    BUILD_STATUS='PASS'
else
    REPOSITORY_FILE_URL='null'
    BUILD_STATUS='FAIL'
fi
echo  "{'REPO': '$REPOSITORY_FILE_URL', 'BUILD_TIMESTAMP': '${BUILD_TIMESTAMP}', 'BUILD_LOG': '$BUILD_URL', 'BUILD_ID': $BUILD_JOB_NUMBER, 'BUILD_STATUS': '$BUILD_STATUS'}" > STATUS

rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
      --verbose --compress --stats --times --chmod=a+rwx,g+rwx,o- \
      "STATUS" \
      "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_TIMESTAMP}/"

rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
      --verbose --compress --recursive --stats --times --chmod=a+rwx,g+rwx,o- \
      "repository" \
      "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_TIMESTAMP}/"

# Create hostos.repo
echo -e """[hostos]
name=hostos
baseurl=${BASE_URL}/repository
enabled=1
priority=1
gpgcheck=0""" > hostos.repo

rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
      --verbose --compress --stats --times --chmod=a+rwx,g+rwx,o- \
      "hostos.repo" \
      "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_TIMESTAMP}/"
