git clone $VERSIONS_REPO_URL
cd versions
git fetch origin +refs/pull/*:refs/remotes/origin/pr/*
git checkout $sha1
cd ..
./scripts/validate_yamls.py -d versions
