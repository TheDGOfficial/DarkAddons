#!/bin/bash
set -eEuo pipefail
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR
IFS=$'\n\t'

if [[ "$(basename "$PWD")" != "SkytilsMod" ]]; then
 echo "Error: Script must be run from inside the SkytilsMod directory (current: $(basename "$PWD"))"
 (return 0 2>/dev/null) && sourced=1 || sourced=0

 if (( sourced )); then
  return 1
 else
  exit 1
 fi
fi

declare -A versionProperties
while IFS='=' read -r key value; do
  if [ -n "$key" ]; then
    versionProperties["$key"]="$value"
  fi
done < ../versions.properties

SKYTILS_VERSION=${versionProperties["skytils.version"]}

log_commit=$(git log --grep "^version:\ $SKYTILS_VERSION\$" -1 --pretty=format:"%h")
if git rev-parse -q --verify "refs/tags/v$SKYTILS_VERSION" > /dev/null; then
    tag_commit=$(git rev-list -n 1 "v$SKYTILS_VERSION")
else
    tag_commit=""
fi

release_commit=""
using_tag=false

if [[ -z "$log_commit" && -z "$tag_commit" ]]; then
 echo "Neither log commit nor tag commit found for version $SKYTILS_VERSION"
 exit 1
elif [[ -z "$log_commit" ]]; then
 release_commit="$tag_commit"
elif [[ -z "$tag_commit" ]]; then
 release_commit="$log_commit"
else
 # Determine which commit is newer
 if git merge-base --is-ancestor "$log_commit" "$tag_commit"; then
  release_commit="$tag_commit"
  using_tag=true
 else
  release_commit="$log_commit"
  using_tag=false
 fi
fi

if [[ "$release_commit" == "" ]]; then
 echo "Can't determine release commit for version $SKYTILS_VERSION"
 exit 1
fi

commit_name=$(git log -1 --format="%s" "$release_commit")

if [[ "$using_tag" == "false" ]]; then
 echo "Using release commit: $commit_name ($release_commit)"
else
 echo "Using release tagged commit: $commit_name ($release_commit)"
fi

export release_commit="$release_commit"
