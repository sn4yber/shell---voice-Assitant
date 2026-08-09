#!/usr/bin/env bash
set -e

echo "Comprobando Java..."
java -version || echo "Java no encontrado"

echo "Comprobando Gradle wrapper..."
if [ -f ./gradlew ]; then
  ./gradlew -v
else
  echo "gradlew no encontrado. Abrir el proyecto en Android Studio y generar Gradle Wrapper."
fi

echo "Comprobando sdkmanager... (si usas Android SDK)"
if command -v sdkmanager >/dev/null 2>&1; then
  sdkmanager --list
else
  echo "sdkmanager no encontrado en PATH"
fi

exit 0
