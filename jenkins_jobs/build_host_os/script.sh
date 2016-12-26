VERSIONS_REPO_DIR="components"
REPO_FILE="extras/centOS/7.2/mock/epel-7-ppc64le.cfg"
MAIN_CENTOS_REPO_BASE_URL="http://mirror.centos.org/altarch"

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

# inform status to upload job
touch SUCCESS
