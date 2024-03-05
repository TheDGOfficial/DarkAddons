#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

declare -A versionProperties
while IFS='=' read -r key value; do
  if [ -n "$key" ]; then
    versionProperties["$key"]="$value"
  fi
done < versions.properties

#KOTLIN_VERSION=2.0.0-dev-17175

#GSON_VERSION=${versionProperties["gson.version"]}
GUAVA_VERSION=${versionProperties["guava.version"]}

JETBRAINS_ANNOTATIONS_VERSION=${versionProperties["jetbrains.annotations.version"]}
COMMONS_LANG3_VERSION=${versionProperties["commons.lang3.version"]}

VIGILANCE_VERSION=295
ELEMENTA_VERSION=636
UNIVERSALCRAFT_VERSION=323

LAUNCHWRAPPER_VERSION=1.12

NETTY_VERSION=4.0.23.Final

LWJGL_VERSION=2.9.4-nightly-20150209
#VECMATH_VERSION=1.5.2

LOG4J_VERSION=2.0-beta9

ASM_VERSION=9.6

ERRORPRONE_VERSION=2.25.0

DARKADDONS_VERSION=${versionProperties["darkaddons.version"]}
SKYTILS_VERSION=${versionProperties["skytils.version"]}

MIXIN_VERSION=${versionProperties["mixin.version"]}

REPO=$HOME/.m2/repository

#GRADLE_REPO=$HOME/.gradle/caches
GRADLE_PROJECT_REPO=.gradle

#CLASSPATH=$REPO/com/google/code/gson/gson/$GSON_VERSION/gson-$GSON_VERSION.jar
CLASSPATH=$REPO/com/google/guava/guava/$GUAVA_VERSION/guava-$GUAVA_VERSION.jar

CLASSPATH=$CLASSPATH:$REPO/org/ow2/asm/asm/$ASM_VERSION/asm-$ASM_VERSION.jar
CLASSPATH=$CLASSPATH:$REPO/org/ow2/asm/asm-tree/$ASM_VERSION/asm-tree-$ASM_VERSION.jar
CLASSPATH=$CLASSPATH:$REPO/org/ow2/asm/asm-analysis/$ASM_VERSION/asm-analysis-$ASM_VERSION.jar
CLASSPATH=$CLASSPATH:$REPO/org/ow2/asm/asm-util/$ASM_VERSION/asm-util-$ASM_VERSION.jar

CLASSPATH=$CLASSPATH:$REPO/org/apache/commons/commons-lang3/$COMMONS_LANG3_VERSION/commons-lang3-$COMMONS_LANG3_VERSION.jar
CLASSPATH=$CLASSPATH:$REPO/org/jetbrains/annotations/$JETBRAINS_ANNOTATIONS_VERSION/annotations-$JETBRAINS_ANNOTATIONS_VERSION.jar
CLASSPATH=$CLASSPATH:$REPO/gg/essential/vigilance-1.8.9-forge/$VIGILANCE_VERSION/vigilance-1.8.9-forge-$VIGILANCE_VERSION.jar
CLASSPATH=$CLASSPATH:$REPO/gg/essential/elementa-1.8.9-forge/$ELEMENTA_VERSION/elementa-1.8.9-forge-$ELEMENTA_VERSION.jar
CLASSPATH=$CLASSPATH:$REPO/gg/essential/universalcraft-1.8.9-forge/$UNIVERSALCRAFT_VERSION/universalcraft-1.8.9-forge-$UNIVERSALCRAFT_VERSION.jar

CLASSPATH=$CLASSPATH:$REPO/net/minecraft/launchwrapper/$LAUNCHWRAPPER_VERSION/launchwrapper-$LAUNCHWRAPPER_VERSION.jar

CLASSPATH=$CLASSPATH:$REPO/io/netty/netty-all/$NETTY_VERSION/netty-all-$NETTY_VERSION.jar

CLASSPATH=$CLASSPATH:$REPO/gg/skytils/skytilsmod/$SKYTILS_VERSION/skytilsmod-$SKYTILS_VERSION.jar

mkdir -p build/bin

cp "$GRADLE_PROJECT_REPO/loom-cache/minecraftMaven/net/minecraft/forge-1.8.9-11.15.1.2318-1.8.9-minecraft-merged-project-root/1.8.9-de.oceanlabs.mcp.mcp_stable.1_8_9.22-1.8.9-forge-1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9-minecraft-merged-project-root-1.8.9-de.oceanlabs.mcp.mcp_stable.1_8_9.22-1.8.9-forge-1.8.9-11.15.1.2318-1.8.9.jar" "build/bin/mc.jar"

zip -d -q build/bin/mc.jar META-INF/MANIFEST.MF

CLASSPATH=$CLASSPATH:build/bin/mc.jar

CLASSPATH=$CLASSPATH:$REPO/org/spongepowered/mixin/$MIXIN_VERSION/mixin-$MIXIN_VERSION.jar

CLASSPATH=$CLASSPATH:$REPO/org/lwjgl/lwjgl/lwjgl/$LWJGL_VERSION/lwjgl-$LWJGL_VERSION.jar
#CLASSPATH=$CLASSPATH:$REPO/org/lwjgl/lwjgl/lwjgl_util/$LWJGL_VERSION/lwjgl_util-$LWJGL_VERSION.jar

#CLASSPATH=$CLASSPATH:$REPO/java3d/vecmath/$VECMATH_VERSION/vecmath-$VECMATH_VERSION.jar

#CLASSPATH=$CLASSPATH:$GRADLE_REPO/essential-loom/1.8.9/de.oceanlabs.mcp.mcp_stable.1_8_9.22-1.8.9-forge-1.8.9-11.15.1.2318-1.8.9/minecraft-intermediary.jar

CLASSPATH=$CLASSPATH:$REPO/org/apache/logging/log4j/log4j-api/$LOG4J_VERSION/log4j-api-$LOG4J_VERSION.jar

CLASSPATH=$CLASSPATH:$REPO/com/google/errorprone/error_prone_annotations/$ERRORPRONE_VERSION/error_prone_annotations-$ERRORPRONE_VERSION.jar

CLASSPATH=$CLASSPATH:libs/javassist.jar

OUTPUT_JAR=build/libs/DarkAddons-v$DARKADDONS_VERSION-opt.jar
#CLASSPATH_SEPERATED_BY_SEMICOLON=${CLASSPATH//://;}

CLASSPATH_WITH_MOD=$CLASSPATH:build/libs/DarkAddons-v$DARKADDONS_VERSION-proguarded.jar
CLASSPATH_WITH_OPTIMIZED_MOD=$CLASSPATH:$OUTPUT_JAR

EXIT_CODE=1

set +e

cmp -s MarkCompilerGeneratedMethodsFinal.java build/bin/MarkCompilerGeneratedMethodsFinal.java
EXIT_CODE=$?

set -e

if [ "$EXIT_CODE" == "2" ]; then
 # Treat non-existent file as different.
 EXIT_CODE=1
fi

# Set exit code to 1 if class file doesn't exist, which will force compilation.
if [ ! -f build/bin/gg/darkaddons/MarkCompilerGeneratedMethodsFinal.class ]; then
 EXIT_CODE=1
fi

if [ "$EXIT_CODE" == "1" ]; then
 cp MarkCompilerGeneratedMethodsFinal.java build/bin/MarkCompilerGeneratedMethodsFinal.java
 javac -cp "$CLASSPATH_WITH_MOD" -proc:none -d build/bin -g -parameters -Xlint:all MarkCompilerGeneratedMethodsFinal.java || echo Unable to compile MarkCompilerGeneratedMethodsFinal.java
fi

java -cp "$CLASSPATH_WITH_MOD":build/bin gg.darkaddons.MarkCompilerGeneratedMethodsFinal || echo "Unable to run MarkCompilerGeneratedMethodsFinal.class"

EXIT_CODE=$?

if [ "$EXIT_CODE" == "0" ]; then
  java -cp "$CLASSPATH_WITH_OPTIMIZED_MOD":build/bin gg.darkaddons.MarkCompilerGeneratedMethodsFinal postRun || echo "Unable to run MarkCompilerGeneratedMethodsFinal.class"
fi

#java -jar $HOME/jvm-constexpr/build/saker.jar.create/sipka.jvm.constexpr-fat.jar -classpath $CLASSPATH_SEPERATED_BY_SEMICOLON -input $OUTPUT_JAR -overwrite

