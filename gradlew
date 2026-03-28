#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
APP_BASE_NAME=$(basename "$0")

if [ -n "$JAVA_HOME" ]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE="java"
fi

if ! command -v "$JAVA_EXE" >/dev/null 2>&1; then
  echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
  exit 1
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVA_EXE" $JAVA_OPTS $GRADLE_OPTS "-Dorg.gradle.appname=$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
