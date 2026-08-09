# 🐚 SHELL — Motorcycle Voice Assistant

> **Asistente de voz para motociclistas basado en Android, diseñado para controlar el teléfono mediante voz mientras se conduce, utilizando el intercomunicador Bluetooth como interfaz de audio.**

**Nombre:** Shell
**Plataforma:** Android
**Lenguaje:** Kotlin
**UI:** Jetpack Compose
**Arquitectura:** MVVM + Clean Architecture ligera
**Persistencia:** DataStore → Room cuando sea necesario
**Backend:** No para el MVP
**IA:** Se incorpora progresivamente
**Dispositivo de prueba:** teléfono Android físico
**Interfaz de audio:** intercomunicador Bluetooth

---

# 1. 🎯 Objetivo del proyecto

Shell busca convertirse en un **copiloto de voz para motocicleta**.

El usuario debe poder realizar acciones habituales sin retirar las manos del manillar ni mirar/tocar constantemente el teléfono.

Ejemplos:

```text
"Shell, llama a mamá."

"Shell, pon música."

"Shell, reproduce mi playlist de carretera."

"Shell, llévame a Cartagena."

"Shell, ¿cuánto falta?"

"Shell, lee mi última notificación."

"Shell, dile a Chelsea que ya voy para allá."
```

Posteriormente:

```text
"Shell, tengo una nueva carrera."

"Shell, ¿dónde es la recogida?"

"Shell, acepta."

"Shell, rechaza."
```

Y eventualmente:

```text
"Shell, estoy aburrido."

"¿Quieres que te ponga música?"

"Sí."

"¿Qué género?"

"Rock."

```

El objetivo final no es solamente ejecutar comandos, sino construir un **asistente conversacional orientado a conducción**.

---

# 2. 🧠 Principio arquitectónico principal

Shell será principalmente **local-first**.

Las funciones básicas no dependerán de un backend propio.

```text
                    SHELL
                      │
          ┌───────────┴───────────┐
          │                       │
        LOCAL                   ONLINE
          │                       │
    Android APIs               IA / APIs
    Bluetooth                 Música
    llamadas                  Servicios externos
    contactos                 Información
    navegación                Conversación
    notificaciones
```

### ¿Por qué?

Porque mientras conduces puedes:

* perder cobertura;
* tener poca batería;
* tener mala conexión;
* estar viajando por carretera.

Funciones básicas como llamadas, volumen o control multimedia no deberían depender de nuestro servidor.

---

# 3. 🏗️ Arquitectura

Se utilizará una **MVVM + Clean Architecture ligera**.

No se busca implementar una arquitectura excesivamente compleja.

```text
app/
│
├── presentation/
│   ├── screens/
│   ├── components/
│   ├── navigation/
│   └── viewmodels/
│
├── domain/
│   ├── models/
│   ├── intents/
│   ├── commands/
│   ├── skills/
│   └── usecases/
│
├── data/
│   ├── preferences/
│   ├── contacts/
│   ├── history/
│   └── repositories/
│
├── services/
│   ├── voice/
│   ├── bluetooth/
│   ├── notification/
│   └── accessibility/
│
└── core/
    ├── permissions/
    ├── audio/
    ├── logging/
    └── utils/
```

---

# 4. 🖥️ Presentation Layer

Tecnologías:

* Kotlin
* Jetpack Compose
* ViewModel
* Navigation Compose

La interfaz NO será el centro de Shell.

Será principalmente un:

> **Panel de control y configuración.**

## Dashboard

Debe mostrar:

```text
SHELL

🟢 Modo conductor activo

🎧 Intercomunicador
Conectado

🎙️ Asistente
Escuchando

🔋 Teléfono
82%

Últimas acciones:

📞 Llamada a mamá
🎵 Música reproducida
🗺️ Navegación iniciada
```

---

# 5. 🧠 Domain Layer

Esta será la parte más importante del proyecto.

Shell no debería estar construido como:

```kotlin
if (command.contains("mamá")) {
    ...
}
```

En lugar de eso se utilizará un sistema de:

## Intent → Skill → Action

Ejemplo:

```text
"Shell, llama a mamá."
        │
        ▼
Speech Recognition
        │
        ▼
Intent Engine
        │
        ▼
CALL_CONTACT
        │
        ▼
PhoneSkill
        │
        ▼
call("Mamá")
```

---

# 6. 🧩 Sistema de Skills

Cada grupo de capacidades tendrá su propio módulo.

```text
skills/

├── PhoneSkill
├── MusicSkill
├── NavigationSkill
├── NotificationSkill
├── MessagingSkill
├── SystemSkill
├── AccessibilitySkill
└── DriverSkill
```

## PhoneSkill

Responsable de:

* llamadas;
* contactos;
* contestar;
* finalizar llamadas.

Ejemplos:

```text
"Llama a mamá."
"Llama a Chelsea."
"Llama a papá."
```

---

# 7. 🎵 MusicSkill

Responsable de:

* play;
* pause;
* siguiente;
* anterior;
* volumen;
* reproducción de contenido compatible.

Ejemplos:

```text
"Shell, reproduce."

"Shell, pausa."

"Shell, siguiente."

"Shell, pon música."

"Shell, reproduce mi playlist de carretera."
```

Inicialmente se utilizarán los controles multimedia de Android.

Posteriormente podrán agregarse integraciones específicas con servicios como Spotify o YouTube Music, según las APIs y permisos disponibles.

---

# 8. 🗺️ NavigationSkill

Shell tendrá integración con aplicaciones de navegación.

Principalmente:

> **Waze**

También se podrá contemplar Google Maps posteriormente.

Ejemplos:

```text
"Shell, llévame a Cartagena."

"Shell, navega a mi casa."

"Shell, abre Waze."

"Shell, ¿cuánto falta?"

"Shell, cancela la navegación."
```

La primera implementación debería utilizar las capacidades oficiales de Android/intents/deep links disponibles para iniciar navegación.

No se debe intentar controlar visualmente Waze mediante AccessibilityService si existe una API o mecanismo oficial equivalente.

---

# 9. 🔔 NotificationSkill

Shell podrá recibir y procesar determinadas notificaciones mediante:

```text
NotificationListenerService
```

Flujo:

```text
Aplicación externa
       │
       ▼
Notificación
       │
       ▼
Shell
       │
       ▼
Interpretación
       │
       ▼
Text-to-Speech
       │
       ▼
🎧 Intercomunicador
```

Ejemplo:

```text
"Shell, tengo una nueva notificación."

"Mensaje de Chelsea: ya llegué."
```

---

# 10. 💬 MessagingSkill

Objetivo:

Permitir comandos como:

```text
"Shell, dile a Chelsea que ya voy para allá."

"Shell, dile a mamá que llegué."
```

La arquitectura deberá diferenciar entre:

```text
GENERATE_MESSAGE
```

y

```text
SEND_MESSAGE
```

Shell podrá generar el contenido mediante IA posteriormente.

El envío dependerá de las capacidades oficiales de Android y de cada plataforma de mensajería.

---

# 11. ♿ AccessibilitySkill

Se utilizará:

```text
AccessibilityService
```

únicamente cuando sea necesario.

Objetivos potenciales:

* interactuar con aplicaciones que no expongan una API suficiente;
* leer determinados elementos accesibles;
* realizar acciones sobre elementos compatibles;
* automatizaciones orientadas a accesibilidad.

No será el mecanismo principal de Shell.

Orden de preferencia:

```text
1. Android API oficial
2. Intent / Deep Link
3. API oficial de aplicación externa
4. AccessibilityService
```

Esto mantiene el proyecto más estable y compatible.

---

# 12. 🚗 DriverSkill

Este será uno de los módulos más importantes a largo plazo.

Permitirá funciones específicas para conducción.

Ejemplo:

```text
Nueva carrera
      │
      ▼
Shell interpreta
      │
      ▼
"Recogida en Cartagena.
Destino Turbaco.
Valor: $18.500.
¿Deseas aceptar?"
      │
      ▼
"Sí."
      │
      ▼
DriverSkill
```

La implementación dependerá de las notificaciones, APIs y políticas de cada plataforma utilizada.

No se debe asumir desde el principio que una aplicación externa permitirá automatizar todas sus acciones.

---

# 13. 🎙️ Voice System

El sistema de voz tendrá varias etapas:

```text
Micrófono
   │
   ▼
Wake Word
   │
   ▼
Speech-to-Text
   │
   ▼
Intent Engine
   │
   ▼
Skill
   │
   ▼
Action
   │
   ▼
Text-to-Speech
   │
   ▼
Bluetooth
```

Ejemplo:

```text
"Shell, llama a mamá."
        ↓
"Shell" → Wake Word
        ↓
"llama a mamá" → Speech-to-Text
        ↓
CALL_CONTACT
        ↓
PhoneSkill
        ↓
Android
        ↓
📞 llamada
```

---

# 14. 🎧 Bluetooth

Shell no necesita implementar desde cero el protocolo del intercomunicador.

El flujo será:

```text
🎧 Intercomunicador
       │
       │ Bluetooth
       ▼
📱 Android
       │
       ▼
Shell
```

Android gestionará el dispositivo Bluetooth y el enrutamiento de audio.

Shell deberá:

* detectar conexión;
* detectar desconexión;
* conocer el estado del dispositivo;
* activar/desactivar el modo conductor;
* utilizar el micrófono y salida de audio adecuados.

---

# 15. 🏍️ Driving Mode

Cuando el usuario active el modo conducción:

```text
DRIVING MODE = ON
```

Shell cambiará su comportamiento.

Prioridad:

```text
Seguridad
   ↓
Comunicación
   ↓
Navegación
   ↓
Música
   ↓
Otras funciones
```

Ejemplos permitidos:

```text
📞 llamadas
🎵 música
🗺️ navegación
🔔 notificaciones
💬 mensajes
🔊 volumen
```

Funciones potencialmente bloqueadas o limitadas:

```text
📱 navegación visual compleja
📸 cámara
📱 redes sociales
⌨️ escritura manual
```

El objetivo es minimizar la interacción visual.

---

# 16. 💾 Persistencia

## MVP

No utilizar una base de datos tradicional.

Utilizar:

> **DataStore**

Para:

```text
nombre del asistente
wake word
preferencias
configuración de voz
modo conducción
proveedor musical
intercomunicador preferido
```

## Posteriormente

Utilizar:

> **Room**

Para datos estructurados como:

```text
CommandHistory

id
timestamp
command
intent
status
duration
```

Esto permitirá mostrar:

```text
Historial

08:13
📞 Llamada a mamá

08:20
🎵 Música reproducida

08:37
🗺️ Waze iniciado

09:02
🔊 Volumen aumentado
```

---

# 17. 🌐 Backend

## MVP

**NO.**

No utilizar:

* Node.js
* Spring Boot
* PostgreSQL
* Prisma
* Render
* Firebase

para el núcleo inicial.

Shell debe ser una aplicación Android autónoma.

## Futuro

Un backend podría agregarse para:

* sincronización;
* configuración entre dispositivos;
* estadísticas;
* perfiles;
* almacenamiento de conversaciones;
* servicios de IA;
* integración con APIs externas.

Pero será una decisión posterior.

---

# 18. 🤖 Inteligencia Artificial

La IA no será el núcleo inicial.

Primero:

```text
Comandos deterministas
```

Ejemplo:

```text
"llama a mamá"
```

Después:

```text
Intención + entidades
```

Finalmente:

```text
Conversación natural
```

Ejemplo:

```text
"Shell, estoy aburrido."

Shell:
"¿Quieres que te ponga música?"

"Sí, algo que me mantenga despierto."

Shell:
"Entendido. Voy a buscar algo con más energía."
```

La IA será un componente del:

> **Conversation Engine**

y no estará mezclada directamente con cada Skill.

---

# 19. 🧠 Conversation Engine

Arquitectura futura:

```text
                 Usuario
                    │
                    ▼
              Speech-to-Text
                    │
                    ▼
            Conversation Engine
                    │
          ┌─────────┴─────────┐
          │                   │
      Simple Intent       AI Reasoning
          │                   │
          └─────────┬─────────┘
                    ▼
                Skill
                    │
                    ▼
                 Acción
```

Esto permitirá que Shell determine cuándo necesita IA y cuándo no.

---

# 20. 🔐 Seguridad y permisos

Shell tendrá que solicitar permisos progresivamente.

No pedir todos al instalar.

Ejemplo:

```text
Primer inicio
     ↓
Micrófono

Al activar llamadas
     ↓
Contactos + llamadas

Al activar notificaciones
     ↓
Notification Access

Al activar automatizaciones
     ↓
AccessibilityService

Al utilizar ubicación
     ↓
Location
```

El usuario siempre debe conocer qué permiso está concediendo y para qué.

---

# 21. 🧪 Entorno de desarrollo

Hardware:

```text
💻 Laptop
   │
   ├── Android Studio
   ├── Kotlin
   ├── Git
   └── JDK
          │
          ▼
     📱 Android físico
          │
          ↕ Bluetooth
     🎧 Intercomunicador
```

El teléfono secundario será el:

> **Shell Development Device**

No dependeremos del emulador como dispositivo principal de pruebas.

---

# 22. 🛠️ Metodología de desarrollo

Se trabajará por incrementos pequeños.

No construir:

```text
SHELL 1.0
```

de una vez.

Construiremos:

```text
SHELL 0.1
     ↓
SHELL 0.2
     ↓
SHELL 0.3
     ↓
SHELL 0.4
     ↓
...
```

Cada versión debe tener una funcionalidad comprobable.

---

# 23. 🚀 Roadmap

## Fase 0 — Preparación

* instalar Android Studio;
* configurar Kotlin;
* configurar JDK;
* configurar teléfono físico;
* configurar Git;
* crear repositorio;
* crear proyecto Android.

---

## Fase 1 — Shell 0.1

### Objetivo

Conseguir:

> "Shell, llama a mamá."

Implementar:

* Jetpack Compose;
* micrófono;
* Speech-to-Text;
* Text-to-Speech;
* contactos;
* llamadas;
* permisos;
* primera arquitectura de Skills.

---

## Fase 2 — Shell 0.2

### Multimedia

Implementar:

* play;
* pause;
* siguiente;
* anterior;
* volumen;
* reproducción multimedia.

---

## Fase 3 — Shell 0.3

### Bluetooth + modo conductor

Implementar:

* detección de intercomunicador;
* conexión/desconexión;
* audio;
* micrófono;
* servicio foreground;
* Driving Mode.

---

## Fase 4 — Shell 0.4

### Navegación

Implementar:

* Waze;
* comandos de navegación;
* destino;
* apertura de rutas;
* cancelación;
* información de navegación cuando sea posible.

---

## Fase 5 — Shell 0.5

### Notificaciones

Implementar:

* NotificationListenerService;
* lectura por voz;
* filtrado de aplicaciones;
* prioridad de notificaciones.

---

## Fase 6 — Shell 0.6

### Accessibility

Investigar e implementar:

* AccessibilityService;
* lectura de elementos;
* acciones compatibles;
* automatizaciones específicas.

---

## Fase 7 — Shell 0.7

### Conversación

Agregar:

* contexto;
* memoria temporal;
* preguntas de seguimiento;
* IA;
* conversación natural.

---

## Fase 8 — Shell 1.0

### Motorcycle Copilot

```text
        🐚 SHELL
           │
     ┌─────┼─────┐
     │     │     │
   📱    🎧    🏍️
 Android Audio Moto
     │     │     │
     └─────┼─────┘
           │
          🤖
```

Funciones:

* asistente de voz;
* llamadas;
* música;
* navegación;
* Waze;
* notificaciones;
* mensajes;
* automatizaciones;
* conversación;
* IA;
* modo conductor.

---

# 24. 🔮 Fase futura — IoT

Posteriormente se podrá integrar un:

> **ESP32**

en la motocicleta.

Arquitectura:

```text
🏍️ Sensores
     │
     ▼
   ESP32
     │
 Bluetooth
     │
     ▼
📱 Shell
     │
     ▼
🤖 Conversation Engine
```

Posibles datos:

* voltaje de batería;
* temperatura;
* estado;
* sensores adicionales;
* GPS dedicado;
* otros datos de la motocicleta.

Entonces:

> "Shell, ¿cómo está la moto?"

podría convertirse en una consulta real a hardware.

---

# 25. 🧱 Primera versión del proyecto

La primera meta NO será:

> "Crear un Jarvis completo."

La primera meta será:

> **Conseguir que un teléfono Android físico conectado al intercomunicador permita decir "Shell, llama a mamá" y realizar la llamada sin tocar el teléfono.**

Si eso funciona:

**Shell 0.1 existe.**

A partir de ahí, cada nueva funcionalidad será un módulo adicional.

---

# 26. 📌 Reglas del proyecto

### Regla 1

No añadir tecnología porque sí.

### Regla 2

No crear backend hasta que exista una necesidad real.

### Regla 3

No crear base de datos hasta que exista información que realmente necesite persistencia estructurada.

### Regla 4

Preferir APIs oficiales de Android.

### Regla 5

AccessibilityService será un mecanismo secundario, no el corazón del sistema.

### Regla 6

Toda funcionalidad deberá poder probarse en un dispositivo físico.

### Regla 7

El modo conducción prioriza seguridad sobre funcionalidades.

### Regla 8

La IA se incorporará después de tener un sistema determinista funcional.

### Regla 9

Cada versión debe terminar en un estado funcional.

### Regla 10

Git desde el primer commit.

---

# 27. 🏁 Primer Sprint

## Objetivo

Crear la base de Shell.

### Tareas

* [ ] Instalar Android Studio.
* [ ] Configurar JDK.
* [ ] Configurar Kotlin.
* [ ] Conectar teléfono de desarrollo.
* [ ] Activar debugging.
* [ ] Crear proyecto.
* [ ] Configurar Git.
* [ ] Crear primera pantalla Compose.
* [ ] Crear `ShellViewModel`.
* [ ] Crear estructura `presentation/domain/data/services`.
* [ ] Crear sistema inicial de comandos.
* [ ] Implementar prueba de voz.
* [ ] Implementar Text-to-Speech.
* [ ] Probar comando simple.

### Primer comando

```text
"Shell, ¿qué hora es?"
```

Respuesta:

```text
"Son las..."
```

Después:

```text
"Shell, llama a mamá."
```

Ese será el primer verdadero milestone.

---

# 🐚 Definición final del proyecto

**Shell no será una simple aplicación de comandos de voz.**

Será una plataforma Android modular que funciona como:

> **copiloto de voz para motocicleta.**

Su núcleo será local, modular y extensible.

Su interfaz será un panel de configuración y monitorización.

Su principal interfaz durante la conducción será:

```text
🎙️ voz
+
🎧 intercomunicador
```

Y su evolución será:

```text
Comandos
   ↓
Automatizaciones
   ↓
Integraciones
   ↓
Conversación
   ↓
IA
   ↓
IoT
   ↓
🏍️ Motorcycle Copilot
```
