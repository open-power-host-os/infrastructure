# ISO 8601 date with nanoseconds precision
TIMESTAMP=$(date --utc +'%Y-%m-%dT%H:%M:%S.%N')
VERSIONS_REPO_DIR=$(basename $VERSIONS_REPO_URL .git)
MOCK_CONFIG_FILE="extras/centOS/7/mock/epel-7-ppc64le.cfg"
MAIN_CENTOS_REPO_RELEASE_URL="http://mirror.centos.org/altarch/7"


# Fetch pull requests in case this job was triggered by one
git clone $VERSIONS_REPO_URL $VERSIONS_REPO_DIR --no-checkout
pushd $VERSIONS_REPO_DIR
git fetch origin +refs/pull/*:refs/remotes/origin/pr/*
popd

# Tell mock to use a different mirror/repo. This could be used to:
# - speedup the chroot installation
# - use a different version of CentOS
# - workaround any issue with CentOS official mirrors
if [ -n "$CENTOS_ALTERNATE_MIRROR_RELEASE_URL" ]; then
    sed -i \
        "s|${MAIN_CENTOS_REPO_RELEASE_URL}|${CENTOS_ALTERNATE_MIRROR_RELEASE_URL}|" \
        $MOCK_CONFIG_FILE
fi

# running
python host_os.py \
    --verbose \
    build-package \
        --keep-builddir \
        --result-dir ./repository \
        --build-versions-repository-url $VERSIONS_REPO_URL \
        --build-version $VERSIONS_REPO_COMMIT \
        --packages $PACKAGES \
        --mock-args "$MOCK_ARGS"

# creating the yum repository locally
createrepo ./repository

# Create BUILD_TIMESTAMP file with timestamp information
echo "${TIMESTAMP}" > ./BUILD_TIMESTAMP

# inform status to upload job
touch SUCCESS
