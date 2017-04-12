# ISO 8601 date with nanoseconds precision
TIMESTAMP=$(date --utc +'%Y-%m-%dT%H:%M:%S.%N')
BUILDS_WORKSPACE_DIR="/var/lib/host-os"
VERSIONS_REPO_DIR="$(basename $VERSIONS_REPO_URL .git)_build-packages"
VERSIONS_REPO_PATH="$BUILDS_WORKSPACE_DIR/repositories/$VERSIONS_REPO_DIR"
MOCK_CONFIG_FILE="mock_configs/CentOS/7/CentOS-7-ppc64le.cfg"
MAIN_CENTOS_REPO_RELEASE_URL="http://mirror.centos.org/altarch/7"

# There is a symlink from the workdir to this directory. This makes
# Jenkins handle its cleanup, since it's in the job workspace.
mkdir mock_build

# Resolve the reference name to a commit id. If the value is not a
# reference, assume it is a commit id.
builds_repo_commit=$(git show-ref --hash "$BUILDS_REPO_REFERENCE" \
                         || echo "$BUILDS_REPO_REFERENCE")

# Set origin remote URL
# This is the remote name assumed by the GHPRB plugin
if [ -d $VERSIONS_REPO_PATH/.git ]; then
    pushd $VERSIONS_REPO_PATH
    git remote remove origin || true
    git remote add origin $VERSIONS_REPO_URL
else
    git clone $VERSIONS_REPO_URL $VERSIONS_REPO_PATH --no-checkout
    pushd $VERSIONS_REPO_PATH
fi

# Fetch pull requests in case this job was triggered by one
git fetch origin +refs/pull/*:refs/remotes/origin/pr/*

versions_repo_commit=$(git show-ref --hash "$VERSIONS_REPO_REFERENCE" \
                           || echo "$VERSIONS_REPO_REFERENCE")
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
         --packages-metadata-repo-branch $VERSIONS_REPO_REFERENCE \
         $PACKAGES_PARAMETER \
         $EXTRA_PARAMETERS

# Create BUILD_TIMESTAMP file with timestamp information
echo "${TIMESTAMP}" > ./BUILD_TIMESTAMP
echo "$builds_repo_commit" > ./BUILDS_REPO_COMMIT
echo "$versions_repo_commit" > ./VERSIONS_REPO_COMMIT

# inform status to upload job
touch SUCCESS
