#!/bin/bash
set -eEuo pipefail
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR
IFS=$'\n\t'

shopt -s extglob

echo Starting
echo Setting up environment

# Those generally should not be changed.
declare -A versionProperties
while IFS='=' read -r key value; do
  if [ -n "$key" ]; then
    versionProperties["$key"]="$value"
  fi
done < versions.properties

DARKADDONS_VERSION=${versionProperties["darkaddons.version"]}

SKYTILS_VERSION=${versionProperties["skytils.version"]}

MOD_FILENAME=DarkAddons-v$DARKADDONS_VERSION-opt.jar

SCRIPT=$(realpath "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

#DESKTOPPATH=$(xdg-user-dir DESKTOP)

BUILD_OUTPUT_JAR_PATH=$SCRIPTPATH/build/libs/$MOD_FILENAME

# Make sure to place the JAR in the correct location.
TAILREC_OPTIMIZER_PATH=libs/sipka.jvm.tailrec.jar

MINECRAFT_FOLDER=$HOME/.minecraft
MOD_DIR=$MINECRAFT_FOLDER/mods

MOD_FILE_IN_MODS_FOLDER=$MOD_DIR/$MOD_FILENAME

CRASH_REPORT_FOLDER=$MINECRAFT_FOLDER/crash-reports
CRASH_REPORT_EXTENSION=txt

CRASH_REPORTS_PATTERN="$CRASH_REPORT_FOLDER/*.$CRASH_REPORT_EXTENSION"

ARTIFACT_PATTERN="build/libs/*.jar"
SIGNATURE_PATTERN="build/libs/*.asc"

MODS_ARTIFACT_PATTERN="$MOD_DIR/DarkAddons-*.jar"

LATEST_LOG_PATH=$MINECRAFT_FOLDER/logs/latest.log

EXIT_CODE=-1

echo Finished setting up environment

cd SkytilsMod || { echo "cd failed"; exit 1; }
git stash &> /dev/null
git stash drop &> /dev/null || true
cd .. || { echo "cd failed"; exit 1; } 

git submodule init

cd darkaddons-site || { echo "cd failed"; exit 1; }
git stash &> /dev/null
git stash drop &> /dev/null || true
git checkout main &> /dev/null
if [ "${1:-default}" != "--offline" ]; then
  git pull &> /dev/null
fi
cd .. || { echo "cd failed"; exit 1; }

git add darkaddons-site &> /dev/null

if [ "${1:-default}" != "--offline" ]; then
  git submodule update
fi

cd SkytilsMod || { echo "cd failed"; exit 1; }
git apply ../SkytilsMod.patch &> /dev/null
cd .. || { echo "cd failed"; exit 1; } 

cd darkaddons-site || { echo "cd failed"; exit 1; }

echo "$DARKADDONS_VERSION" > latest_mod_version.txt
if [[ $(git status --porcelain) ]]; then
 echo "RELEASE PENDING; do not forget to commit to darkaddons-site git repository at release time."
fi

cd .. || { echo "cd failed"; exit 1; }

chmod +x gradlew

if [ "${1:-default}" != "--skip-build" ]; then
  if [ ! -d "$HOME/.m2/repository/gg/skytils/skytilsmod/$SKYTILS_VERSION" ]; then
    rm -rf SkytilsMod/build/libs/*
    cd SkytilsMod || { echo "cd failed"; exit 1; }
    if [ "${1:-default}" != "--offline" ]; then
      git fetch --all --tags --force &> /dev/null
    fi
    git stash &> /dev/null
    git stash drop &> /dev/null || true 
    git checkout dev &> /dev/null
    if [ "${1:-default}" != "--offline" ]; then
      git pull -X theirs &> /dev/null
    fi
    . ../findstverscommit.sh
    git checkout "$release_commit" &> /dev/null
    cd hypixel-api || { echo "cd failed"; exit 1; } 
    git stash &> /dev/null
    git stash drop &> /dev/null || true
    cd .. || { echo "cd failed"; exit 1; }
    cd ws-shared || { echo "cd failed"; exit 1; } 
    git stash &> /dev/null
    git stash drop &> /dev/null || true
    cd .. || { echo "cd failed"; exit 1; }  
    git submodule init
    if [ "${1:-default}" != "--offline" ]; then
      git submodule update
    fi
    chmod +x gradlew
    git apply ../SkytilsMod.patch &> /dev/null
    cd hypixel-api || { echo "cd failed"; exit 1; }
    git apply ../../hypixel-api.patch &> /dev/null
    cd .. || { echo "cd failed"; exit 1; }
    cd ws-shared || { echo "cd failed"; exit 1; }
    git apply ../../ws-shared.patch &> /dev/null
    cd .. || { echo "cd failed"; exit 1; }
    if [ "${1:-default}" != "--offline" ]; then
      ./gradlew build remapJar publishToMavenLocal --no-daemon --refresh-dependencies
    else
      ./gradlew build remapJar publishToMavenLocal --no-daemon --refresh-dependencies --offline
    fi
    cd .. || { echo "cd failed"; exit 1; }
  else
    rm -rf "$HOME"/.m2/repository/gg/skytils/skytilsmod/!("$SKYTILS_VERSION"|maven-metadata-local.xml)/
  fi
fi

if [ "${1:-default}" == "--check-updates" ]; then
  echo Checking Gradle dependency updates
  ./gradlew dependencyUpdates --no-daemon -Drevision=integration # available values: milestone (default), release, integration
  exit
fi

if [ "${1:-default}" == "--regen-daemon-jvm-config" ]; then
  echo Regenerating Daemon JVM Config
  ./gradlew updateDaemonJvm
  exit
fi

if [ "${1:-default}" == "--build-health" ]; then
  echo Checking Gradle build health
  ./gradlew buildHealth
  exit
fi

if [ "${1:-default}" == "--refresh-dependencies" ]; then
  echo Force refreshing loom dependencies
  ./gradlew build test remapJar --refresh-dependencies
  exit
fi

if [ "${1:-default}" == "--clean-build" ]; then
  echo "Doing a clean build (will be a lot slower!)"
  ./gradlew build test remapJar --refresh-dependencies --rerun-tasks --no-build-cache --warning-mode all
  exit
fi

EXIT_CODE=1

if [ "${1:-default}" != "--skip-build" ]; then
  echo Building JAR
  # shellcheck disable=SC2206
  artifact_array=($ARTIFACT_PATTERN)
  if compgen -G "${artifact_array[@]}" >/dev/null; then
    rm "${artifact_array[@]}"
  fi
  # shellcheck disable=SC2206
  signature_array=($SIGNATURE_PATTERN)
  if compgen -G "${signature_array[@]}" >/dev/null; then
    rm "${signature_array[@]}"
  fi
  if [ "${1:-default}" != "--offline" ]; then
    if [ "${1:-default}" == "--info" ]; then
     ./gradlew build test remapJar --info
    else
     ./gradlew build test remapJar
    fi
  else
    if [ "${2:-default}" == "--info" ]; then
     ./gradlew build test remapJar --offline --info
    else
     ./gradlew build test remapJar --offline
    fi
  fi
  EXIT_CODE=$?
  if [ "$EXIT_CODE" == "0" ]; then
    echo "Stopping Gradle daemons..."
    ./gradlew --stop
    echo "Stopped Gradle daemons since Gradle build succeeded."
  else
    echo "Skipping stopping Gradle daemons since build failed."
  fi
fi

if [ "${1:-default}" != "--skip-build" ]; then
  for file in $ARTIFACT_PATTERN; do
    mv "$file" "${file/DarkAddons-/DarkAddons-v}" 2>/dev/null
  done

  if [ "$EXIT_CODE" == "0" ]; then
    echo Generating optimized JAR
    SECONDS=0
    ./optimize.sh
    echo Optimizing took "$SECONDS"s.
  fi
fi

if [ -f "$BUILD_OUTPUT_JAR_PATH" ] && [ "$EXIT_CODE" == "0" ]; then
  if [ -f "$TAILREC_OPTIMIZER_PATH" ]; then
    echo Optimizing JAR
    java -jar "$TAILREC_OPTIMIZER_PATH" -overwrite "$BUILD_OUTPUT_JAR_PATH"
  else
    echo Error: Can\'t find Optimizer JAR. Make sure file path is correct and the file exists!
  fi

  cp src/main/resources/pack.mcmeta pack.mcmeta
  jar uf "$BUILD_OUTPUT_JAR_PATH" pack.mcmeta
  rm pack.mcmeta

  zip -q -d "$BUILD_OUTPUT_JAR_PATH" "gg/darkaddons/ChromaScoreboard\$Dummy.class"

  if [ "${1:-default}" != "--skip-install" ]; then
    echo Placing JAR in mod folder
    # shellcheck disable=SC2206
    mods_artifact_array=($MODS_ARTIFACT_PATTERN)
    if compgen -G "${mods_artifact_array[@]}" >/dev/null; then
      rm "${mods_artifact_array[@]}"
    fi
    chmod +x "$BUILD_OUTPUT_JAR_PATH"
    if [ -d "$MOD_DIR" ]; then
     cp "$BUILD_OUTPUT_JAR_PATH" "$MOD_FILE_IN_MODS_FOLDER"
    fi
  fi
elif [ "${1:-default}" != "--skip-build" ]; then
  echo Skipping optimizing and putting JAR in the mod folder, gradle build likely failed. See errors above.
fi

echo Cleaning up old crash reports and logs

# shellcheck disable=SC2206
crash_reports_array=($CRASH_REPORTS_PATTERN)
if compgen -G "${crash_reports_array[@]}" >/dev/null; then
  cleaned_crash_reports=$(rm -v "${crash_reports_array[@]}" | wc -l)
  echo "Cleaned up $cleaned_crash_reports crash reports"
fi

if [ -d "$CRASH_REPORT_FOLDER" ]; then
  rmdir "$CRASH_REPORT_FOLDER"
fi

if [ "${1:-default}" != "--skip-install" ]; then
  if [ -f "$LATEST_LOG_PATH" ]; then
    rm "$LATEST_LOG_PATH"
    echo "Cleaned up 1 log file"
  fi
fi

echo Done

