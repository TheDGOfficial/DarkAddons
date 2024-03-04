#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

grep -rnI '.' --exclude-dir={SkytilsMod,.idea,.git,.gradle,build,.vscode,run} --exclude=todo.sh --exclude=*.jar --exclude=*.csv -e 'TODO' --color=always
