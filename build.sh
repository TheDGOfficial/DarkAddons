#!/bin/bash
echo Starting
echo Setting up environment

export JAVA_HOME=/usr/lib/jvm/jdk-21-oracle-x64

# Those generally should not be changed.
declare -A versionProperties
while IFS='=' read -r key value; do
  if [ ! -z "$key" ]; then
    versionProperties["$key"]="$value"
  fi
done < version.properties

DARKADDONS_VERSION=${versionProperties["darkaddons.version"]}

cd darkaddons-site

echo "$DARKADDONS_VERSION" > latest_mod_version.txt
if [[ `git status --porcelain` ]]; then
 echo "RELEASE PENDING; do not forget to commit to darkaddons-site git repository at release time."
fi

cd ..

ASMHELPER_VERSION=${versionProperties["asmhelper.version"]}
SKYTILS_VERSION=${versionProperties["skytils.version"]}
MIMALLOC_SO_VERSION=${versionProperties["mimalloc.so.version"]}

MOD_FILENAME=DarkAddons-v$DARKADDONS_VERSION-opt.jar

SCRIPT=$(realpath "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

DESKTOPPATH=$(xdg-user-dir DESKTOP)

JAVA8_HOME=$DESKTOPPATH/apps/java8-jdk
BUILD_OUTPUT_JAR_PATH=$SCRIPTPATH/build/libs/$MOD_FILENAME

# Make sure to place the JAR in the correct location.
TAILREC_OPTIMIZER_PATH=$HOME/jvm-tail-recursion/build/saker.jar.create/sipka.jvm.tailrec.jar

MINECRAFT_FOLDER=$HOME/.minecraft
MOD_DIR=$MINECRAFT_FOLDER/mods

MOD_FILE_IN_MODS_FOLDER=$MOD_DIR/$MOD_FILENAME

CRASH_REPORT_FOLDER=$MINECRAFT_FOLDER/crash-reports
CRASH_REPORT_EXTENSION=txt

CRASH_REPORTS_PATTERN=$CRASH_REPORT_FOLDER/*.$CRASH_REPORT_EXTENSION

ARTIFACT_PATTERN=build/libs/*.jar
SIGNATURE_PATTERN=build/libs/*.asc

MODS_ARTIFACT_PATTERN=$MOD_DIR/DarkAddons-*.jar

LATEST_LOG_PATH=$MINECRAFT_FOLDER/logs/latest.log

EXIT_CODE=-1

#echo Purging garbage
#git gc

#cd ASMHelper
#git gc
#cd ..

#cd SkytilsMod
#git gc
#cd ..

#echo Purged garbage successfully.

echo Finished setting up environment

if [ "$1" != "--skip-build" ]; then
  if [ ! -d "$HOME/.m2/repository/dev/falsehonesty/asmhelper/AsmHelper/$ASMHELPER_VERSION" ]; then
    cd ASMHelper
    JAVA_HOME=$DESKTOPPATH/apps/java8-jdk LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION ./gradlew -Porg.gradle.java.installations.auto-download=false build publishToMavenLocal --no-daemon
    cd ..
  fi
fi

if [ "$1" != "--skip-build" ]; then
  if [ ! -d "$HOME/.m2/repository/gg/skytils/skytilsmod/$SKYTILS_VERSION" ]; then
    rm -rf SkytilsMod/build/libs/*
    cd SkytilsMod
    git submodule init
    git submodule update
    LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION ./gradlew -Porg.gradle.java.installations.auto-download=false build remapJar publishToMavenLocal --no-daemon
    cd ..
  fi
fi

if [ "$1" == "--check-updates" ]; then
  echo Checking Gradle dependency updates
  LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION ./gradlew -Porg.gradle.java.installations.auto-download=false dependencyUpdates --no-daemon -Drevision=integration # available values: milestone (default), release, integration
  exit
fi

if [ "$1" == "--refresh-dependencies" ]; then
  echo Force refreshing loom dependencies
  LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION ./gradlew -Porg.gradle.java.installations.auto-download=false build test remapJar --refresh-dependencies
  exit
fi

EXIT_CODE=1

if [ "$1" != "--skip-build" ]; then
  echo Building JAR
  if compgen -G "$ARTIFACT_PATTERN" >/dev/null; then
    rm $ARTIFACT_PATTERN
  fi
  if compgen -G "$SIGNATURE_PATTERN" >/dev/null; then
    rm $SIGNATURE_PATTERN
  fi
  DEBUG_PARAMS=""
  if [ "$1" == "--info" ]; then
    DEBUG_PARAMS=" --info"
  fi
  if [ "$1" != "--offline" ]; then
    JAVA_HOME=jdk-21.0.2 LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION ./gradlew -Porg.gradle.java.installations.auto-download=false build test remapJar$DEBUG_PARAMS
  else
    LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION ./gradlew -Porg.gradle.java.installations.auto-download=false build test remapJar --offline$DEBUG_PARAMS
  fi
  EXIT_CODE=$?
  if [ "$EXIT_CODE" == "0" ]; then
    echo "Stopping Gradle daemons..."
    JAVA_HOME=jdk-21.0.2 LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION ./gradlew -Porg.gradle.java.installations.auto-download=false --stop
    echo "Stopped Gradle daemons since Gradle build succeeded."
  else
    echo "Skipping stopping Gradle daemons since build failed."
  fi
fi

if [ "$1" != "--skip-build" ]; then
  for file in $ARTIFACT_PATTERN; do
    mv $file ${file/DarkAddons-/DarkAddons-v} 2>/dev/null
  done

  if [ "$EXIT_CODE" == "0" ]; then
    echo Generating optimized JAR
    ./optimize.sh
  fi
fi

if [ -f "$BUILD_OUTPUT_JAR_PATH" ] && [ "$EXIT_CODE" == "0" ]; then
  if [ -f "$TAILREC_OPTIMIZER_PATH" ]; then
    echo Optimizing JAR
    LD_PRELOAD=/usr/local/lib/libmimalloc.so.$MIMALLOC_SO_VERSION jdk-21.0.2/bin/java --enable-preview -jar $TAILREC_OPTIMIZER_PATH -overwrite $BUILD_OUTPUT_JAR_PATH
  else
    echo Error: Can\'t find Optimizer JAR. Make sure file path is correct and the file exists!
  fi

  # TODO Very hacky workaround
  #jdk-21.0.2/bin/jar xf $BUILD_OUTPUT_JAR_PATH gg/darkaddons/mixins/MixinSkytilsIODispatcher.class
  #sed -i 's/skytils\/ktx\/coroutines/skytils\/ktx-coroutines/g' gg/darkaddons/mixins/MixinSkytilsIODispatcher.class
  #jdk-21.0.2/bin/jar uf $BUILD_OUTPUT_JAR_PATH gg/darkaddons/mixins/MixinSkytilsIODispatcher.class
  #rm -rf gg

  cp src/main/resources/pack.mcmeta pack.mcmeta
  jdk-21.0.2/bin/jar uf $BUILD_OUTPUT_JAR_PATH pack.mcmeta
  rm pack.mcmeta

  if [ "$1" != "--skip-install" ]; then
    echo Placing JAR in mod folder
    if compgen -G "$MODS_ARTIFACT_PATTERN" >/dev/null; then
      rm $MODS_ARTIFACT_PATTERN
    fi
    chmod +x $BUILD_OUTPUT_JAR_PATH
    cp $BUILD_OUTPUT_JAR_PATH $MOD_FILE_IN_MODS_FOLDER
  fi
elif [ "$1" != "--skip-build" ]; then
  echo Skipping optimizing and putting JAR in the mod folder, gradle build likely failed. See errors above.
fi

echo Cleaning up old crash reports and logs

if compgen -G "$CRASH_REPORTS_PATTERN" >/dev/null; then
  cleaned_crash_reports=$(rm -v $CRASH_REPORTS_PATTERN | wc -l)
  echo "Cleaned up $cleaned_crash_reports crash reports"
fi

if [ -d "$CRASH_REPORT_FOLDER" ]; then
  rmdir $CRASH_REPORT_FOLDER
fi

if [ "$1" != "--skip-install" ]; then
  if [ -f "$LATEST_LOG_PATH" ]; then
    rm $LATEST_LOG_PATH
    echo "Cleaned up 1 log file"
  fi
fi

echo Done
