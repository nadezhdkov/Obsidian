# 🔮 Obsidian

Ein modernes und minimalistisches Java-Framework mit leistungsstarken Utility-Bibliotheken zur Vereinfachung häufiger Entwicklungsaufgaben.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../../../LICENSE.md)
[![Java Version](https://img.shields.io/badge/java-11%2B-orange.svg)](https://www.java.com/)
[![Maven Central](https://img.shields.io/badge/maven--central-1.0.0-brightgreen.svg)](https://central.sonatype.com/artifact/io.github.nadezhdkov/obsidian/overview)

**📖 Verfügbare Sprachen:** [English](../../../README.md) | [Português Brasileiro](../pt-BR/README.md) | [Deutsch](./README.md) | [Español](../es-ES/README.md)

## 📚 Über

Obsidian ist eine Sammlung unabhängiger und hochspezialisierter Module, die Java-Entwicklung vereinfachen. Jedes Modul ist so konzipiert, dass es spezifische Probleme mit einer klaren, flüssigen und benutzerfreundlichen API löst.

### Verfügbare Module

#### 🔧 [Obsidian Configuration](../../../docs/subproject/dotenv/dotenv_readme.md)

Minimalistisches **Annotations**-System zum Zuordnen von Umgebungsvariablen (`.env`) zu Java-Feldern.

**Funktionen:**
- ✅ Automatische Zuordnung von Umgebungsvariablen
- ✅ Unterstützung für Standardwerte
- ✅ Validierung erforderlicher Variablen
- ✅ Automatische Präfixe
- ✅ Automatische Typkonvertierung
- ✅ Spezifische Felder ignorieren

**Schnelles Beispiel:**
```java
@Env("DATABASE_URL")
String dbUrl;

@Env("PORT")
@Default("8080")
int port;

@RequiredEnv
@Env("API_KEY")
String apiKey;
```

#### 🔬 [Obsidian Reflection](../../../docs/subproject/reflect/reflect_readme.md)

Eine flüssige und leistungsstarke API für die Arbeit mit Reflection in Java auf einfache und intuitive Weise.

**Funktionen:**
- ✅ Flüssige API mit Methodenverkettung
- ✅ Leistungsstarke Filter für Felder und Methoden
- ✅ Type-sicher, soweit möglich
- ✅ Integriertes Builder-Pattern
- ✅ Einfache Annotation-Manipulation
- ✅ Kein Boilerplate-Code

**Schnelles Beispiel:**
```java
// Auf Felder zugreifen
Reflect.on(User.class)
    .fields()
    .named("name")
    .set(user, "John");

// Methoden aufrufen
Reflect.on(user)
    .methods()
    .getters()
    .each(System.out::println);

// Instanzen erstellen
User user = Reflect.on(User.class).newInstance("John", 30);
```

## 🚀 Quick Start

### Installation

#### Maven
```xml
<dependency>
    <groupId>obsidian.lib</groupId>
    <artifactId>obsidian-configuration</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>obsidian.lib</groupId>
    <artifactId>obsidian-reflection</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle
```gradle
implementation 'obsidian.lib:obsidian-configuration:1.0.0'
implementation 'obsidian.lib:obsidian-reflection:1.0.0'
```

### Vollständiges Beispiel

```java
import io.dotenv.annotations.Env;
import io.dotenv.annotations.Default;
import io.dotenv.core.Dotenv;
import lang.reflect.Reflect;

public class Application {
    
    @Env("APP_NAME")
    @Default("MyApp")
    String appName;
    
    @Env("PORT")
    @Default("8080")
    int port;
    
    public static void main(String[] args) {
        // Konfiguration aus .env laden
        Application app = new Application();
        Dotenv.load(app);
        
        // Reflection zur Inspektion der Anwendung verwenden
        Reflect.on(app)
            .fields()
            .each(f -> System.out.println(f.getName() + " = " + f.get(app)));
    }
}
```

## 📖 Dokumentation

- [Obsidian Configuration - Vollständige Dokumentation](../../../docs/subproject/dotenv/dotenv_readme.md)
- [Obsidian Reflection - Vollständige Dokumentation](../../../docs/subproject/reflect/reflect_readme.md)
- [JSON-Architektur](../../subproject/json/json_architecture_and_design.md)

## 🛠️ Entwicklung

### Anforderungen

- **Java 11** oder höher
- **Gradle 7.0** oder höher
- **Make** (optional, zur Verwendung von Makefile-Befehlen)

### Projekt bauen

```bash
# Vollständiger Build
make build

# Tests ausführen
make test

# Build bereinigen
make clean

# Dokumentation generieren
make javadoc

# Lokal veröffentlichen
make local
```

Oder mit Gradle direkt:

```bash
# Build
./gradlew build

# Tests
./gradlew test

# Überprüfung
./gradlew check
```

### Projektstruktur

```
Obsidian/
├── obsidian-configuration/   # Konfigurations- und .env-Modul
│   ├── src/main/java/
│   │   └── io/dotenv/
│   │       ├── annotations/   # Annotations @Env, @Default, etc
│   │       ├── core/          # Kernimplementierung
│   │       ├── processor/     # Prozessoren
│   │       └── util/          # Dienstprogramme
│   └── build.gradle.kts
│
├── obsidian-reflection/      # Reflection-Modul
│   ├── src/main/java/
│   │   └── lang/reflect/
│   │       ├── Reflect*.java  # Hauptklassen
│   │       └── exception/     # Ausnahmen
│   └── build.gradle.kts
│
├── obsidian-promise/         # Promise-Modul
│   ├── src/main/java/
│   │   └── io/obsidian/promise/
│   │       ├── api/           # Promise-API
│   │       ├── combinators/   # Promise-Kombinatoren
│   │       ├── error/         # Fehlerbehandlung
│   │       └── internal/      # Interne Implementierung
│   └── build.gradle.kts
│
├── src/main/java/obsidian/  # Kern-Utilities
│   ├── control/              # When und Kontrollfluss
│   ├── functional/           # Try<T> und funktionale Utilities
│   ├── util/                 # Box<T>, Sequence<T>, Range
│   │   ├── concurrent/       # Thread-sichere Container
│   │   └── stream/           # Stream-Utilities
│   └── api/                  # Kern-APIs
│
├── docs/                     # Dokumentation
├── gradle/                   # Gradle-Wrapper
├── build.gradle.kts          # Wurzel-Build-Konfiguration
├── settings.gradle.kts       # Unterproject-Konfiguration
├── Makefile                  # Entwicklungs-Targets
└── README.md                 # Diese Datei
```

## 📦 Abhängigkeiten

- **JetBrains Annotations** - Annotations für bessere Code-Analyse
- **Gson** - JSON-Serialisierung
- **YAML** - YAML-Unterstützung
- **Lombok** - Boilerplate-Reduktion
- **JUnit 5** - Test-Framework

## 🤝 Beitragen

Beiträge sind willkommen! Um beizutragen:

1. Forken Sie das Projekt
2. Erstellen Sie einen Branch für Ihr Feature (`git checkout -b feature/AmazingFeature`)
3. Committen Sie Ihre Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. Pushen Sie zum Branch (`git push origin feature/AmazingFeature`)
5. Öffnen Sie einen Pull Request

## 📄 Lizenz

Dieses Projekt ist unter der [Apache License 2.0](../../../LICENSE.md) lizenziert - siehe Lizenzdatei für Details.

## 👨‍💻 Autor

Entwickelt mit ❤️ von Nadezhda

## 🎯 Roadmap

- [ ] Erweiterte YAML-Unterstützung
- [ ] Intelligentes Reflection-Caching
- [ ] Automatische Feldvalidierung
- [ ] Spring Framework-Integration
- [ ] Dediziertes Gradle-Plugin
- [ ] Mehrsprachige Dokumentation

## 💡 Tipps & Tricks

### Dotenv mit Reflection verwenden

```java
@Env("DB_HOST")
String host;

// Auf alle mit @Env annotierten Felder zugreifen
Reflect.on(this)
    .fields()
    .annotated(Env.class)
    .each(f -> System.out.println(f.getName()));
```

### Methoden filtern

```java
// Alle Getter finden
List<Method> getters = Reflect.on(User.class)
    .methods()
    .getters()
    .list();

// Benutzerdefinierte Filter
Reflect.on(service)
    .methods()
    .isPublic()
    .notStatic()
    .startWith("handle")
    .each(method -> /* verarbeiten */);
```

## 🐛 Probleme melden

Einen Bug gefunden? Bitte öffnen Sie ein [Issue](https://github.com/nadezhdkov/obsidian/issues) mit:

- Klare Problembeschreibung
- Schritte zur Reproduktion
- Erwartetes vs. tatsächliches Verhalten
- Java- und Obsidian-Version

## 📞 Support

Für zusätzliche Unterstützung:

- 📧 E-Mail: seu-email@exemplo.com
- 💬 GitHub Issues
- 📚 Siehe die vollständige Dokumentation in den `/docs`-Ordnern

---

**Entwickelt mit ❤️ mit Java und Gradle**
