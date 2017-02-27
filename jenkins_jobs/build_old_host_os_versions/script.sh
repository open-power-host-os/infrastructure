MOCK_CONFIG_FILE="mock_configs/CentOS/7/CentOS-7-ppc64le.cfg"
BUILD_CONFIG_FILE="config.yaml"
CENTOS_7_YUM_REPO_URL="http://mirror.centos.org/altarch/7"
CENTOS_7_2_YUM_REPO_URL="http://vault.centos.org/altarch/7.2.1511/"
VERSIONS_REPO_URL="https://github.com/${GITHUB_ORGANIZATION_NAME}/versions.git"
TAG_1_0="v1.0.0"
TAG_1_5="v1.5.0"

# use CentOS 7.2 instead of latest CentOS 7 yum repository
sed -i \
    "s|${CENTOS_7_YUM_REPO_URL}|${CENTOS_7_2_YUM_REPO_URL}|" \
    $MOCK_CONFIG_FILE

sed -i \
    "s|build_versions_repository_url:.*|build_versions_repository_url: \"$VERSIONS_REPO_URL\"|" \
    $BUILD_CONFIG_FILE
sed -i \
    "s|distro_version:.*|distro_version: \"7.2\"|" \
    $BUILD_CONFIG_FILE
# -E is to avoid escaping parenthesis
sed -i -E \
    "s|  '7': (\"\./mock_configs.*)|  '7.2': \1|" \
    $BUILD_CONFIG_FILE

# clean yum repos cache to avoid conflict with newer CentOS versions content
# which may be already in the cache
mock --scrub all

# 1.5
sed -i \
    "s|build_version:.*|build_version: \"$TAG_1_5\"|" \
    $BUILD_CONFIG_FILE
python host_os.py --verbose build-package

# 1.0
sed -i \
    "s|build_version:.*|build_version: \"$TAG_1_0\"|" \
    $BUILD_CONFIG_FILE
python host_os.py --verbose build-package

# clean yum repos to avoid that future builds which use newer CentOS versions have conflicts
mock --scrub all
