# Shell — Motorcycle Voice Assistant

Este repositorio contiene la estructura inicial para Shell, un asistente de voz para motociclistas en Android (Kotlin + Jetpack Compose).

Requisitos:
- Android Studio (Arctic Fox o superior recomendada)
- JDK 11+
- Android SDK (API 34 recomendada)
- Dispositivo Android físico para pruebas (recomendado)

Pasos iniciales:
1. Abrir este proyecto en Android Studio.
2. Si falta, generar/actualizar Gradle Wrapper desde Android Studio.
3. Conectar dispositivo y habilitar USB debugging.
4. Ejecutar la app o usar "Run" desde Android Studio.

Configuración ya incluida:
- Gradle Wrapper y versiones compatibles.
- Base Compose con dashboard inicial.
- Permisos base para micrófono, contactos y llamadas.
- Estructura inicial para voz y permisos.

Siguientes pasos sugeridos:
- Implementar sistema de Skills y ShellViewModel
- Añadir permisos progresivos (micrófono, llamadas, notificaciones)
- Integrar Speech-to-Text y Text-to-Speech
