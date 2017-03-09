# ISO 8601 date with nanoseconds precision
TIMESTAMP=$(date --utc +'%Y-%m-%dT%H:%M:%S.%N')
BUILDS_WORKSPACE_DIR="/var/lib/host-os"
VERSIONS_REPO_DIR=$(basename $VERSIONS_REPO_URL .git)
VERSIONS_REPO_PATH="$BUILDS_WORKSPACE_DIR/repositories/$VERSIONS_REPO_DIR"
MOCK_CONFIG_FILE="mock_configs/CentOS/7/CentOS-7-ppc64le.cfg"
MAIN_CENTOS_REPO_RELEASE_URL="http://mirror.centos.org/altarch/7"


# Fetch pull requests in case this job was triggered by one
test -d $VERSIONS_REPO_PATH/.git \
    || git clone $VERSIONS_REPO_URL $VERSIONS_REPO_PATH --no-checkout
pushd $VERSIONS_REPO_PATH
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
if [ -n "$PACKAGES" ]; then
    PACKAGES_PARAMETER="--packages $PACKAGES"
fi
eval python host_os.py \
     --verbose \
     --work-dir $BUILDS_WORKSPACE_DIR \
     build-packages \
         --force-rebuild \
         --keep-build-dir \
         --packages-metadata-repo-url $VERSIONS_REPO_URL \
         --packages-metadata-repo-branch $VERSIONS_REPO_COMMIT \
         $PACKAGES_PARAMETER \
         $EXTRA_PARAMETERS

# Create BUILD_TIMESTAMP file with timestamp information
echo "${TIMESTAMP}" > ./BUILD_TIMESTAMP

# inform status to upload job
touch SUCCESS
