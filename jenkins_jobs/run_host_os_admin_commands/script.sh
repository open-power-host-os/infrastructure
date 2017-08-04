VERSIONS_REPO_DIR=$(basename $VERSIONS_REPO_URL .git)


# Fetch pull requests in case this job was triggered by one
git clone $VERSIONS_REPO_URL $VERSIONS_REPO_DIR --no-checkout
pushd repositories/$VERSIONS_REPO_DIR
git fetch origin +refs/pull/*:refs/remotes/origin/pr/*
popd

eval python host_os.py \
     --verbose \
     build-release-notes \
         --packages-metadata-repo-url $VERSIONS_REPO_URL \
         --packages-metadata-repo-branch $VERSIONS_REPO_COMMIT \
         --no-push-updates

eval python host_os.py \
     --verbose \
     update-versions \
         --packages-metadata-repo-url $VERSIONS_REPO_URL \
         --packages-metadata-repo-branch $VERSIONS_REPO_COMMIT \
         --no-push-updates

eval python host_os.py \
     --verbose \
     update-versions-readme \
         --packages-metadata-repo-url $VERSIONS_REPO_URL \
         --packages-metadata-repo-branch $VERSIONS_REPO_COMMIT \
         --no-push-updates
