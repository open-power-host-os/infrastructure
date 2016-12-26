VERSIONS_REPO_DIR="components"
REPO_FILE="extras/centOS/7.2/mock/epel-7-ppc64le.cfg"
MAIN_CENTOS_REPO_BASE_URL="http://mirror.centos.org/altarch"

function on_exit(){
    rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
          --verbose --compress --stats --times --chmod=a+rwx,g+rwx,o- \
          "STATUS" \
          "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_NUMBER}/"
}

function on_error(){
    echo  "{'REPO': null, 'BUILD_LOG': '$BUILD_URL', 'BUILD_ID': $BUILD_NUMBER, 'BUILD_STATUS': 'FAIL'}" > STATUS
    exit 1
}

trap on_exit EXIT
trap on_error ERR

set +e

# Fetch pull requests in case this job was triggered by one
git clone $VERSIONS_REPO_URL $VERSIONS_REPO_DIR --no-checkout
pushd $VERSIONS_REPO_DIR
git fetch origin +refs/pull/*:refs/remotes/origin/pr/*
popd

# Use an internal mirror to speedup the chroot install
# This is also a workaround to an issue  with CentOS mirrors where yum can`t download the packages
if [ -n "$CENTOS_INTERNAL_MIRROR_BASE_URL" ]; then
    sed -i \
        "s|${MAIN_CENTOS_REPO_BASE_URL}|${CENTOS_INTERNAL_MIRROR_BASE_URL}|" \
        $REPO_FILE
fi

# running
python host_os.py \
    --verbose \
    build-package \
        --keep-builddir \
        --result-dir ./repository \
        --build-versions-repository-url $VERSIONS_REPO_URL \
        --build-version $VERSIONS_REPO_COMMIT \
        --packages $PACKAGES

# creating the yum repository locally
createrepo ./repository

# create hostos.repo
echo -e """[hostos]
name=hostos
baseurl=http://${UPLOAD_SERVER_HOST_NAME}${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_NUMBER}/repository
enabled=1
gpgcheck=0""" > hostos.repo
# status json
echo  "{'REPO': 'http://${UPLOAD_SERVER_HOST_NAME}${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_NUMBER}/hostos.repo', 'BUILD_LOG': '$BUILD_URL', 'BUILD_ID': $BUILD_NUMBER, 'BUILD_STATUS': 'PASS'}" > STATUS

# publish
rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
      --verbose --compress --recursive --stats --times --chmod=a+rwx,g+rwx,o- \
      "repository" \
      "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_NUMBER}"
rsync -e "ssh -i $HOME/.ssh/${UPLOAD_SERVER_USER_NAME}_id_rsa" \
      --verbose --compress --stats --times --chmod=a+rwx,g+rwx,o- \
      "hostos.repo" \
      "${UPLOAD_SERVER_USER_NAME}@${UPLOAD_SERVER_HOST_NAME}:${UPLOAD_SERVER_BUILDS_DIR}/${BUILD_NUMBER}"
