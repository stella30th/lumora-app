#!/bin/sh
# Lumora Maven Wrapper (ban rut gon): tu tai Maven ve local neu chua co, roi chay build bang chinh no.
# Dung khi may chua cai Maven global. Muon dung ban wrapper chinh thuc cua Apache, chay:
#   mvn -N wrapper:wrapper -Dmaven=3.9.9
set -e

WRAPPER_DIR="$(cd "$(dirname "$0")" && pwd)"
PROPS_FILE="$WRAPPER_DIR/.mvn/wrapper/maven-wrapper.properties"
DIST_URL=$(grep '^distributionUrl=' "$PROPS_FILE" | cut -d'=' -f2-)
DISTS_DIR="$WRAPPER_DIR/.mvn/wrapper/dists"
ARCHIVE_NAME=$(basename "$DIST_URL")
DIR_NAME="${ARCHIVE_NAME%-bin.zip}"
MVN_BIN="$DISTS_DIR/$DIR_NAME/bin/mvn"

if [ ! -x "$MVN_BIN" ]; then
  echo "Dang tai Maven ve: $DIST_URL"
  mkdir -p "$DISTS_DIR"
  curl -fsSL -o "$DISTS_DIR/$ARCHIVE_NAME" "$DIST_URL"
  unzip -q -o "$DISTS_DIR/$ARCHIVE_NAME" -d "$DISTS_DIR"
  rm "$DISTS_DIR/$ARCHIVE_NAME"
  chmod +x "$MVN_BIN"
fi

exec "$MVN_BIN" "$@"
