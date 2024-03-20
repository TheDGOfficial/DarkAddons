#!/bin/bash
set -eEuo pipefail
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR
IFS=$'\n\t'

set +eE
trap - ERR

grep -rnI '.' --exclude-dir={SkytilsMod,.idea,.git,.gradle,build,.vscode,run} --exclude=todo.sh --exclude=grepinproject.sh --exclude=*.jar --exclude=*.csv --exclude=*.patch -e "$1" --color=always
EXIT_CODE=$?

if [ "$EXIT_CODE" == "2" ]; then
 echo "grep returned an error"
elif [ "$EXIT_CODE" == "1" ]; then
 echo "found no matches for pattern \"$1\""
fi

set -eE
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR

