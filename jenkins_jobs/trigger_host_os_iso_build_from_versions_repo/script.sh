#!/bin/bash

set -e
set -x


LISTENED_CONTEXT="Build Host OS"


get_build_state(){
    # this will evaluate the queried keys as variables: state=<state>,
    # target_url=<url>
    eval $(github_api "$GITHUB_USER_NAME" "$GITHUB_PASSWORD" \
                      query_status "${GITHUB_ORGANIZATION_NAME}/versions" \
                      "$ghprbPullId" "$LISTENED_CONTEXT" --state \
                      --target-url || echo "exit 1")
}

get_pr_state(){
    # this will evaluate the queried keys as variables: state=<state>,
    # title=<title>
    eval $(github_api "$GITHUB_USER_NAME" "$GITHUB_PASSWORD" \
                      query_pr "${GITHUB_ORGANIZATION_NAME}/versions" \
                      "$ghprbPullId" --state --title || echo "exit 1")
}

state="pending"
get_build_state
while [ $state == "pending" ]; do
    echo "Waiting for $LISTENED_CONTEXT to finish..."
    echo "$target_url"
    sleep 1m
    get_build_state
done

if [ "$state" == "failure" -o "$state" == "error" ]; then
    echo "$LISTENED_CONTEXT failed, aborting."
    echo "$target_url."
    exit 1
fi

if [ "$state" == "success" ]; then
    echo "$LISTENED_CONTEXT succeeded."
    echo "$target_url"
    get_pr_state
    if [ "$state" == "closed" ]; then
        echo "Pull request ${ghprbPullId}: \"${title}\" was closed, aborting."
        echo "$ghprbPullLink"
        exit 1
    fi
fi

echo "BUILD_JOB_NUMBER=$(basename $target_url)" > BUILD_PARAMETERS
echo "Triggering ISO build from $target_url ..."

exit 0
