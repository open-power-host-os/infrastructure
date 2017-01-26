RELEASE_DATE=$(date +%Y-%m-%d)

# Upgrade versions
# sudo yum install rpmdevtools
python host_os.py \
   --verbose \
   upgrade-versions \
       --no-push-updates \
       --updater-name "$GITHUB_BOT_NAME" \
       --updater-email "$GITHUB_BOT_EMAIL"

echo "VERSIONS_REPO_COMMIT=HEAD" > BUILD_PARAMETERS
echo "$RELEASE_DATE" > NIGHTLY_DIR_NAME
