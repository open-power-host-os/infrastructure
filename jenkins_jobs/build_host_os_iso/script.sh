VERSIONS_REPO_DIR=$(basename $VERSIONS_REPO_URL .git)
MOCK_CONFIG_FILE="mock_configs/CentOS/7/spin-iso-CentOS-7-ppc64le.cfg"
MAIN_CENTOS_REPO_RELEASE_URL="http://mirror.centos.org/altarch/7"

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
eval python host_os.py \
     --verbose \
     build-iso \
         --packages-dir ./repository \
         $EXTRA_PARAMETERS

# inform status to upload job
touch SUCCESS
