#!/bin/bash
declare -A versionProperties
while IFS='=' read -r key value; do
  if [ ! -z "$key" ]; then
    versionProperties["$key"]="$value"
  fi
done < version.properties

MIMALLOC_SO_VERSION=${versionProperties["mimalloc.so.version"]}

LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION grep -rnI '.' --exclude-dir={ASMHelper,SkytilsMod,.idea,.git,.gradle,build,.vscode,run,jdk-*} --exclude=todo.sh --exclude=*.jar --exclude=*.csv -e 'TODO' --color=always
