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

MAVEN_DEPENDENCY_PLUGIN_VERSION=3.8.1

GUAVA_VERSION=${versionProperties["guava.version"]}

JETBRAINS_ANNOTATIONS_VERSION=${versionProperties["jetbrains.annotations.version"]}
COMMONS_LANG3_VERSION=${versionProperties["commons.lang3.version"]}

ASM_VERSION=${versionProperties["asm.version"]}

HYPIXEL_MODAPI_VERSION=1.0.1.2

VIGILANCE_VERSION=312
ELEMENTA_VERSION=710
UNIVERSALCRAFT_VERSION=427

AUTHLIB_VERSION=1.5.25

LAUNCHWRAPPER_VERSION=1.12

NETTY_VERSION=4.0.23.Final

LWJGL_VERSION=2.9.4-nightly-20150209

LOG4J_VERSION=2.0-beta9

GSON_VERSION=2.2.4

SOUNDSYSTEM_VERSION=201809301515

ERRORPRONE_VERSION=2.41.0

KOTLIN_VERSION=2.1.20
KOTLIN_COROUTINES_VERISON=1.10.2

DARKADDONS_VERSION=${versionProperties["darkaddons.version"]}
SKYTILS_VERSION=${versionProperties["skytils.version"]}

MIXIN_VERSION=${versionProperties["mixin.version"]}

export MAVEN_OPTS="--enable-native-access=ALL-UNNAMED"

ensure_m2_artifact_exists() {
 GROUP=$1
 ARTIFACT=$2
 VERSION=$3
 ARTIFACT_PATH=$4
 SOURCES_PATH=$5
 JAVADOC_PATH=$6
 PROCESSOR_PATH=$7
 FETCH_SOURCE=$8
 FETCH_JAVADOC=$9
 HAS_PROCESSOR=${10}

 if [ ! -f "$ARTIFACT_PATH" ]; then
  ./mvnw -Dmaven.mainClass=org.apache.maven.cling.MavenCling org.apache.maven.plugins:maven-dependency-plugin:$MAVEN_DEPENDENCY_PLUGIN_VERSION:get -DremoteRepositories=https://repo.essential.gg/public/,https://repo.spongepowered.org/maven/,https://repo.papermc.io/repository/maven-public/,https://repo.hypixel.net/repository/Hypixel/,https://nexus.velocitypowered.com/repository/maven-public/ -Dtransitive=false -Dartifact="$GROUP":"$ARTIFACT":"$VERSION"
 fi
 if [ "$FETCH_SOURCE" = "true" ]; then
  if [ ! -f "$SOURCES_PATH" ]; then
   ./mvnw -Dmaven.mainClass=org.apache.maven.cling.MavenCling org.apache.maven.plugins:maven-dependency-plugin:$MAVEN_DEPENDENCY_PLUGIN_VERSION:get -DremoteRepositories=https://repo.essential.gg/public/,https://repo.spongepowered.org/maven/,https://repo.papermc.io/repository/maven-public/,https://repo.hypixel.net/repository/Hypixel/,https://nexus.velocitypowered.com/repository/maven-public/ -Dtransitive=false -Dartifact="$GROUP":"$ARTIFACT":"$VERSION":jar:sources
  fi
 fi
 if [ "$FETCH_JAVADOC" = "true" ]; then
  if [ ! -f "$JAVADOC_PATH" ]; then
   ./mvnw -Dmaven.mainClass=org.apache.maven.cling.MavenCling org.apache.maven.plugins:maven-dependency-plugin:$MAVEN_DEPENDENCY_PLUGIN_VERSION:get -DremoteRepositories=https://repo.essential.gg/public/,https://repo.spongepowered.org/maven/,https://repo.papermc.io/repository/maven-public/,https://repo.hypixel.net/repository/Hypixel/,https://nexus.velocitypowered.com/repository/maven-public/ -Dtransitive=false -Dartifact="$GROUP":"$ARTIFACT":"$VERSION":jar:javadoc
  fi
 fi
 if [ "$HAS_PROCESSOR" = "true" ]; then
  if [ ! -f "$PROCESSOR_PATH" ]; then
   ./mvnw -Dmaven.mainClass=org.apache.maven.cling.MavenCling org.apache.maven.plugins:maven-dependency-plugin:$MAVEN_DEPENDENCY_PLUGIN_VERSION:get -DremoteRepositories=https://repo.essential.gg/public/,https://repo.spongepowered.org/maven/,https://repo.papermc.io/repository/maven-public/,https://repo.hypixel.net/repository/Hypixel/,https://nexus.velocitypowered.com/repository/maven-public/ -Dtransitive=false -Dartifact="$GROUP":"$ARTIFACT":"$VERSION":jar:processor
  fi
 fi
}

CLASSPATH=""
HASH=""

REPO=$HOME/.m2/repository

add_m2_artifact_to_classpath() {
 GROUP_WITH_DOTS=$1

 ARTIFACT=$2
 VERSION=$3

 GROUP=${GROUP_WITH_DOTS//./\/}

 ARTIFACT_PATH=$REPO/$GROUP/$ARTIFACT/$VERSION/$ARTIFACT-$VERSION.jar
 SOURCES_PATH=$REPO/$GROUP/$ARTIFACT/$VERSION/$ARTIFACT-$VERSION-sources.jar
 JAVADOC_PATH=$REPO/$GROUP/$ARTIFACT/$VERSION/$ARTIFACT-$VERSION-javadoc.jar
 PROCESSOR_PATH=$REPO/$GROUP/$ARTIFACT/$VERSION/$ARTIFACT-$VERSION-processor.jar
 HAS_PROCESSOR="${4:-false}"
 FETCH_SOURCE="${5:-false}"
 FETCH_JAVADOC="${6:-false}"
 ensure_m2_artifact_exists "$GROUP_WITH_DOTS" "$ARTIFACT" "$VERSION" "$ARTIFACT_PATH" "$SOURCES_PATH" "$JAVADOC_PATH" "$PROCESSOR_PATH" "$FETCH_SOURCE" "$FETCH_JAVADOC" "$HAS_PROCESSOR"

 CLASSPATH=$CLASSPATH:$ARTIFACT_PATH
 HASH=$HASH$VERSION
}

GRADLE_PROJECT_REPO=.gradle

add_m2_artifact_to_classpath com.google.guava guava "$GUAVA_VERSION"
add_m2_artifact_to_classpath com.google.code.gson gson "$GSON_VERSION"

add_m2_artifact_to_classpath org.ow2.asm asm "$ASM_VERSION"
add_m2_artifact_to_classpath org.ow2.asm asm-tree "$ASM_VERSION"
add_m2_artifact_to_classpath org.ow2.asm asm-analysis "$ASM_VERSION"
add_m2_artifact_to_classpath org.ow2.asm asm-util "$ASM_VERSION"
add_m2_artifact_to_classpath org.ow2.asm asm-commons "$ASM_VERSION"

add_m2_artifact_to_classpath org.apache.commons commons-lang3 "$COMMONS_LANG3_VERSION"
add_m2_artifact_to_classpath org.jetbrains annotations "$JETBRAINS_ANNOTATIONS_VERSION"

add_m2_artifact_to_classpath gg.essential vigilance "$VIGILANCE_VERSION"
add_m2_artifact_to_classpath gg.essential elementa "$ELEMENTA_VERSION"
add_m2_artifact_to_classpath gg.essential universalcraft-1.8.9-forge "$UNIVERSALCRAFT_VERSION"

add_m2_artifact_to_classpath com.mojang authlib "$AUTHLIB_VERSION"

add_m2_artifact_to_classpath net.minecraft launchwrapper "$LAUNCHWRAPPER_VERSION"
add_m2_artifact_to_classpath io.netty netty-all "$NETTY_VERSION"
add_m2_artifact_to_classpath org.spongepowered mixin "$MIXIN_VERSION" true
add_m2_artifact_to_classpath org.lwjgl.lwjgl lwjgl "$LWJGL_VERSION"

add_m2_artifact_to_classpath org.apache.logging.log4j log4j-api "$LOG4J_VERSION"
add_m2_artifact_to_classpath com.google.errorprone error_prone_annotations "$ERRORPRONE_VERSION"

add_m2_artifact_to_classpath org.jetbrains.kotlin kotlin-stdlib "$KOTLIN_VERSION"
add_m2_artifact_to_classpath org.jetbrains.kotlinx kotlinx-coroutines-core "$KOTLIN_COROUTINES_VERISON"

add_m2_artifact_to_classpath net.hypixel mod-api-forge "$HYPIXEL_MODAPI_VERSION"

add_m2_artifact_to_classpath com.paulscode soundsystem "$SOUNDSYSTEM_VERSION"

CLASSPATH=$CLASSPATH:$REPO/gg/skytils/skytilsmod/$SKYTILS_VERSION/skytilsmod-$SKYTILS_VERSION.jar

HASH=$HASH$SKYTILS_VERSION

echo -n "$HASH" | sha256sum > .sha256sum

mkdir -p build/bin

DIR=$(find "$GRADLE_PROJECT_REPO/loom-cache/minecraftMaven/net/minecraft" -name "forge-1.8.9-11.15.1.2318-1.8.9-minecraft-merged-*" -not -path "$GRADLE_PROJECT_REPO/loom-cache/minecraftMaven/net/minecraft/forge-1.8.9-11.15.1.2318-1.8.9-minecraft-merged-project-root" | head -n 1)
ID="${DIR##*-}"

cp "$GRADLE_PROJECT_REPO/loom-cache/minecraftMaven/net/minecraft/forge-1.8.9-11.15.1.2318-1.8.9-minecraft-merged-$ID/1.8.9-de.oceanlabs.mcp.mcp_stable.1_8_9.22-1.8.9-forge-1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9-minecraft-merged-$ID-1.8.9-de.oceanlabs.mcp.mcp_stable.1_8_9.22-1.8.9-forge-1.8.9-11.15.1.2318-1.8.9.jar" "build/bin/mc.jar"

zip -d -q build/bin/mc.jar META-INF/MANIFEST.MF

CLASSPATH=$CLASSPATH:build/bin/mc.jar
CLASSPATH=$CLASSPATH:libs/javassist.jar

OUTPUT_JAR=build/libs/DarkAddons-v$DARKADDONS_VERSION-opt.jar

CLASSPATH_WITH_MOD=$CLASSPATH:build/libs/DarkAddons-v$DARKADDONS_VERSION-proguarded.jar
CLASSPATH_WITH_OPTIMIZED_MOD=$CLASSPATH:$OUTPUT_JAR

EXIT_CODE=1

set +eE
trap - ERR

cmp -s MarkCompilerGeneratedMethodsFinal.java build/bin/MarkCompilerGeneratedMethodsFinal.java
EXIT_CODE=$?

set -eE
trap 'CODE=$?; echo "$0: Error on line "$LINENO": $BASH_COMMAND"; exit $CODE' ERR

if [ "$EXIT_CODE" == "2" ]; then
 # Treat non-existent file as different.
 EXIT_CODE=1
fi

# Set exit code to 1 if class file doesn't exist, which will force compilation.
if [ ! -f build/optimizer/gg/darkaddons/MarkCompilerGeneratedMethodsFinal.class ]; then
 EXIT_CODE=1
fi

if [ "$EXIT_CODE" == "1" ]; then
 echo Compiling optimizer
 cp MarkCompilerGeneratedMethodsFinal.java build/bin/MarkCompilerGeneratedMethodsFinal.java
 javac -cp "$CLASSPATH_WITH_MOD" -proc:none -d build/optimizer -g -parameters -Xlint:all MarkCompilerGeneratedMethodsFinal.java
 jar --create --file=build/bin/optimizer.jar -C build/optimizer .
fi

java -cp "$CLASSPATH_WITH_MOD":build/bin/optimizer.jar gg.darkaddons.MarkCompilerGeneratedMethodsFinal

EXIT_CODE=$?

if [ "$EXIT_CODE" == "0" ]; then
  echo Verifying optimized artifact
  java -cp "$CLASSPATH_WITH_OPTIMIZED_MOD":build/bin/optimizer.jar gg.darkaddons.MarkCompilerGeneratedMethodsFinal postRun
fi

