#!/usr/bin/env bash
set -euo pipefail

# Simple safety check
if [ ! -f "build.gradle" ] && [ ! -f "build.gradle.kts" ]; then
  echo "Please run this from the project root (where build.gradle is)."
  exit 1
fi

echo "=== Service template initializer ==="

read -rp "New service name (e.g. whatever-service): " SERVICE_NAME
read -rp "Main class name (e.g. WhateverApplication): " MAIN_CLASS
read -rp "Package suffix after com.jay. (e.g. whatever): " PKG_SUFFIX

if [ -z "$SERVICE_NAME" ] || [ -z "$MAIN_CLASS" ] || [ -z "$PKG_SUFFIX" ]; then
  echo "All values are required."
  exit 1
fi

OLD_APP_NAME="service-template"
OLD_PACKAGE="com.jay.template"
NEW_PACKAGE="com.jay.${PKG_SUFFIX}"

echo
echo "Using:"
echo "  Service name : $SERVICE_NAME"
echo "  Main class   : $MAIN_CLASS"
echo "  Package      : $NEW_PACKAGE"
echo

read -rp "Proceed with these changes? [y/N] " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
  echo "Aborted."
  exit 0
fi

# Helper: safe replace across common project files
replace_all() {
  local search="$1"
  local replace="$2"

  # Add or remove paths here as your template grows
  FILES=$(grep -rl --exclude-dir=.git "$search" \
    src \
    build.gradle* \
    settings.gradle* \
    Dockerfile \
    buildspec.yml \
    .github 2>/dev/null || true)

  if [ -n "$FILES" ]; then
    echo "Replacing '$search' -> '$replace' in:"
    echo "$FILES"
    # Linux GNU sed; on macOS you may need: sed -i '' ...
    echo "$FILES" | xargs sed -i "s/$search/$replace/g"
  fi
}

echo "Replacing application name..."
replace_all "$OLD_APP_NAME" "$SERVICE_NAME"

echo "Replacing package name..."
replace_all "$OLD_PACKAGE" "$NEW_PACKAGE"

echo "Replacing main application class name..."
replace_all "ServiceTemplateApplication" "$MAIN_CLASS"

# Move package directories
OLD_MAIN_DIR="src/main/java/com/jay/template"
NEW_MAIN_DIR="src/main/java/com/jay/${PKG_SUFFIX}"

OLD_TEST_DIR="src/test/java/com/jay/template"
NEW_TEST_DIR="src/test/java/com/jay/${PKG_SUFFIX}"

if [ -d "$OLD_MAIN_DIR" ]; then
  echo "Moving $OLD_MAIN_DIR -> $NEW_MAIN_DIR"
  mkdir -p "$(dirname "$NEW_MAIN_DIR")"
  mv "$OLD_MAIN_DIR" "$NEW_MAIN_DIR"
fi

if [ -d "$OLD_TEST_DIR" ]; then
  echo "Moving $OLD_TEST_DIR -> $NEW_TEST_DIR"
  mkdir -p "$(dirname "$NEW_TEST_DIR")"
  mv "$OLD_TEST_DIR" "$NEW_TEST_DIR"
fi

# Rename main class file if it exists
APP_FILE=$(find src -name "ServiceTemplateApplication.java" 2>/dev/null || true)
if [ -n "$APP_FILE" ]; then
  APP_DIR=$(dirname "$APP_FILE")
  NEW_FILE="${APP_DIR}/${MAIN_CLASS}.java"
  echo "Renaming $APP_FILE -> $NEW_FILE"
  mv "$APP_FILE" "$NEW_FILE"
fi

echo
echo "Initialization complete. Review changes, then:"
echo "  git add ."
echo "  git commit -m \"Initialize $(echo "$SERVICE_NAME")\""