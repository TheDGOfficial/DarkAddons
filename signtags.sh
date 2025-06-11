#!/bin/bash
set -eEuo pipefail
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR
IFS=$'\n\t'

git for-each-ref refs/tags --format='%(refname:short)' \
| xargs -I % sh -c 'git tag -f -s % -m "Signed tag %" %^{}'
git push --tags --force
