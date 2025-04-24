#!/bin/bash
set -eEuo pipefail
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR
IFS=$'\n\t'

java -cp ~/.m2/repository/org/ow2/asm/asm/9.8/asm-9.8.jar:. JarDependencyChecker.java ~/DarkAddons/build/libs/DarkAddons-v0.2.0-opt.jar gg.skytils