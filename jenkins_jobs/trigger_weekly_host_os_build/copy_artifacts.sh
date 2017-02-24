JENKINS_MASTER=$(basename $JENKINS_URL)
JENKINS_BUILD_ARCHIVE_DIR_NAME="archive"
ARTIFACTS_SRC_BUILD_NUMBER=$TRIGGERED_JOB_NUMBER_build_host_os
ARTIFACTS_SRC_JOB_NAME="build_host_os"
JENKINS_BUILD_ARCHIVE_URL=${JENKINS_MASTER}:${JENKINS_HOME}/jobs/${ARTIFACTS_SRC_JOB_NAME}/builds/${ARTIFACTS_SRC_BUILD_NUMBER}/${JENKINS_BUILD_ARCHIVE_DIR_NAME}

rsync_download() {
    rsync -e "ssh -i ${HOME}/.ssh/jenkins_id_rsa" \
              --verbose --compress --stats --times --perms \
              $JENKINS_BUILD_ARCHIVE_URL/$1 .
}

rsync_download BUILD_TIMESTAMP
