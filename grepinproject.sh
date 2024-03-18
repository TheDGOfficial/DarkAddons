#!/bin/bash
set -eEuo pipefail
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR
IFS=$'\n\t'

grep -rnI '.' --exclude-dir={SkytilsMod,.idea,.git,.gradle,build,.vscode,run} --exclude=todo.sh --exclude=grepinproject.sh --exclude=*.jar --exclude=*.csv --exclude=*.patch -e "$1" --color=always

