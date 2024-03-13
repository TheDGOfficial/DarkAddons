#!/bin/bash
set -eEuo pipefail
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR
IFS=$'\n\t'

declare -A versionProperties
while IFS='=' read -r key value; do
  if [ -n "$key" ]; then
    versionProperties["$key"]="$value"
  fi
done < versions.properties

SKYTILS_VERSION=${versionProperties["skytils.version"]}

git submodule init

cd SkytilsMod || { echo "cd failed"; exit 1; }

git stash &> /dev/null

cd .. || { echo "cd failed"; exit 1; } 

git submodule update

cd SkytilsMod || { echo "cd failed"; exit 1; }
cd hypixel-api || { echo "cd failed"; exit 1; } 

git stash &> /dev/null

cd .. || { echo "cd failed"; exit 1; } 

git submodule init
git submodule update

git fetch --all --tags &> /dev/null

git checkout dev &> /dev/null
git pull &> /dev/null
release_commit=$(git log --grep "version: $SKYTILS_VERSION" -1 --pretty=format:"%h")
git checkout "$release_commit" &> /dev/null

git stash pop &> /dev/null || true 
git stash drop &> /dev/null || true 

git diff --unified=0 --ignore-space-at-eol > ../SkytilsMod.patch

cd hypixel-api || { echo "cd failed"; exit 1; } 

git stash pop &> /dev/null || true 
git stash drop &> /dev/null || true 

git diff --unified=0 --ignore-space-at-eol > ../../hypixel-api.patch

cd .. || { echo "cd failed"; exit 1; } 
cd .. || { echo "cd failed"; exit 1; } 

