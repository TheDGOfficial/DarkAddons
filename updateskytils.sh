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

cd ws-shared || { echo "cd failed"; exit 1; } 

git stash &> /dev/null

cd .. || { echo "cd failed"; exit 1; } 

git submodule init
git submodule update

git fetch --all --tags --force &> /dev/null

git checkout dev &> /dev/null
git pull -X theirs &> /dev/null
release_commit=$(git log --grep "^version:\ $SKYTILS_VERSION\$" -1 --pretty=format:"%h")
git checkout "$release_commit" &> /dev/null

git stash pop &> /dev/null || true 
git stash drop &> /dev/null || true 

git diff > ../SkytilsMod.patch

cd hypixel-api || { echo "cd failed"; exit 1; } 

git stash pop &> /dev/null || true 
git stash drop &> /dev/null || true 

git diff > ../../hypixel-api.patch

cd .. || { echo "cd failed"; exit 1; } 

cd ws-shared || { echo "cd failed"; exit 1; } 

git stash pop &> /dev/null || true 
git stash drop &> /dev/null || true 

git diff > ../../ws-shared.patch

cd .. || { echo "cd failed"; exit 1; } 
cd .. || { echo "cd failed"; exit 1; } 

git add SkytilsMod
