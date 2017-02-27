JENKINS_MASTER=$(basename $JENKINS_URL)
JENKINS_BUILD_ARCHIVE_DIR_NAME="archive"
JENKINS_BUILD_ARCHIVE_URL=${JENKINS_MASTER}:${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/${JENKINS_BUILD_ARCHIVE_DIR_NAME}

rsync_upload() {
    rsync -e 'ssh -i ${HOME}/.ssh/jenkins_id_rsa' \
              --verbose --compress --stats --times --perms \
              $@ $JENKINS_BUILD_ARCHIVE_URL
}

# Create remote archive directory
mkdir $JENKINS_BUILD_ARCHIVE_DIR_NAME
rsync_upload --recursive $JENKINS_BUILD_ARCHIVE_DIR_NAME

rsync_upload result/iso/latest/*
