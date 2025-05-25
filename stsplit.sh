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

ASM_VERSION=${versionProperties["asm.version"]}

java -cp ~/.m2/repository/org/ow2/asm/asm/"$ASM_VERSION"/asm-"$ASM_VERSION".jar:. JarDependencyChecker.java ~/DarkAddons/build/libs/*-opt.jar gg.skytils
