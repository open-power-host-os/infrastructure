MOCK_CONFIG_FILE="config/mock/CentOS/7/CentOS-7-ppc64le.cfg"
LEGACY_MOCK_DIR="config/mock/centOS/7.2"
LEGACY_MOCK_CONFIG_FILE="${LEGACY_MOCK_DIR}/centOS-7.2-ppc64le.cfg"
BUILD_CONFIG_FILE="config/host_os.yaml"
CENTOS_7_YUM_REPO_URL="http://mirror.centos.org/altarch/7"
CENTOS_7_2_YUM_REPO_URL="http://vault.centos.org/altarch/7.2.1511"
VERSIONS_REPO_URL="ssh://git@github/${GITHUB_ORGANIZATION_NAME}/versions.git"
TAG_1_0="hostos-1.0"
TAG_1_5="hostos-1.5"

# create legacy directories
mkdir -p $LEGACY_MOCK_DIR

# copy mock config to the legacy location
cp $MOCK_CONFIG_FILE $LEGACY_MOCK_CONFIG_FILE

# use CentOS 7.2 instead of latest CentOS 7 yum repository
sed -i \
    "s|${CENTOS_7_YUM_REPO_URL}|${CENTOS_7_2_YUM_REPO_URL}|" \
    $LEGACY_MOCK_CONFIG_FILE

# Disable caches and bind mounts so there are no conflicts with concurrent
# builds and to make sure the packages are installed from the correct
# repositories
sed -i \
    "/config_opts\['plugin_conf'\]\['root_cache_enable'\]/d" \
    $LEGACY_MOCK_CONFIG_FILE
sed -i \
    "/config_opts\['plugin_conf'\]\['yum_cache_enable'\]/d" \
    $LEGACY_MOCK_CONFIG_FILE
sed -i \
    "/config_opts\['plugin_conf'\]\['bind_mount_enable'\]/d" \
    $LEGACY_MOCK_CONFIG_FILE
echo "\
config_opts['plugin_conf']['root_cache_enable'] = False
config_opts['plugin_conf']['yum_cache_enable'] = False
config_opts['plugin_conf']['bind_mount_enable'] = False
" >> $LEGACY_MOCK_CONFIG_FILE

# 1.5
python host_os.py \
       --verbose \
       --distro-name centOS \
       --distro-version 7.2 \
       build-packages \
       --packages-metadata-repo-branch $TAG_1_5

# 1.0
python host_os.py \
       --verbose \
       --distro-name centOS \
       --distro-version 7.2 \
       build-packages \
       --packages-metadata-repo-branch $TAG_1_0
