VERSIONS_REPO_DIR=$(basename $VERSIONS_REPO_URL .git)
BUILDS_CONFIG_FILE="config/host_os.yaml"
BUILD_TIMESTAMP=$(cat BUILD_TIMESTAMP)
ISO_VERSION=$BUILD_TIMESTAMP
MOCK_CONFIG_FILE="config/mock/CentOS/7/build-iso-CentOS-7-ppc64le.cfg"
MAIN_CENTOS_REPO_RELEASE_URL="http://mirror.centos.org/altarch/7"
MAIN_EPEL_REPO_RELEASE_URL="http://download.fedoraproject.org/pub/epel/7"

# Format iso version.  Example of expected string:
# 2017-05-10T12:50:29.163138539
ISO_VERSION=${ISO_VERSION//-/}
ISO_VERSION=${ISO_VERSION//:/}
ISO_VERSION=${ISO_VERSION//.*/}

# Tell mock and pungi to use different CentOS and EPEL mirrors/repos.
# This could be used to:
# - speedup the chroot installation
# - use a different version of CentOS
# - workaround any issue with CentOS official mirrors
if [ -n "$CENTOS_ALTERNATE_MIRROR_RELEASE_URL" ]; then
    sed -i \
        "s|${MAIN_CENTOS_REPO_RELEASE_URL}|${CENTOS_ALTERNATE_MIRROR_RELEASE_URL}|" \
        $MOCK_CONFIG_FILE
    sed -i \
        "s|${MAIN_CENTOS_REPO_RELEASE_URL}|${CENTOS_ALTERNATE_MIRROR_RELEASE_URL}|" \
        $BUILDS_CONFIG_FILE
fi
if [ -n "$EPEL_ALTERNATE_MIRROR_RELEASE_URL" ]; then
    # The mock configuration file currently has a mirror list URL for
    # EPEL, which can't be replaced in the same manner as the others.
    sed -i \
        "s|${MAIN_EPEL_REPO_RELEASE_URL}|${EPEL_ALTERNATE_MIRROR_RELEASE_URL}|" \
        $BUILDS_CONFIG_FILE
fi


# running
eval python host_os.py \
     --verbose \
     build-iso \
         --packages-dir repository \
         --iso-version $ISO_VERSION \
         $EXTRA_PARAMETERS

# inform status to upload job
touch SUCCESS
