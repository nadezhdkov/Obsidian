<div align="center">
  <h1>🔮 Obsidian Framework</h1>
  <p><b>A modern, minimalist, and robust Java 21+ framework.</b></p>
  <p><i>Focused on developer ergonomics, offering advanced reflection, flexible configuration, concurrency abstractions, and high-quality utility APIs.</i></p>

  <p align="center">
    <img src="https://img.shields.io/badge/Java-21+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21+"/>
    <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle"/>
    <img src="https://img.shields.io/badge/Apache_2.0-D22128?style=for-the-badge&logo=apache&logoColor=white" alt="License"/>
    <img src="https://img.shields.io/badge/GitHub_Packages-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub Packages"/>
    <img src="https://img.shields.io/badge/Maven_Central-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven Central"/>
  </p>
</div>

---

**📖 Available Languages:** [English](./README.md) | [Português Brasileiro](./docs/i18n/pt-BR/README.md) | [Deutsch](./docs/i18n/de-DE/README.md) | [Español](./docs/i18n/es-ES/README.md)

## 📌 Overview

Obsidian is a collection of elegant, independent, and highly specialized Java modules that simplify backend development. Each module is designed to solve specific problems with a fluent, intuitive API, minimizing boilerplate and maximizing type safety.

Built for **Java 21**, Obsidian leverages modern language features to deliver a seamless developer experience.

## ✨ Core Modules

### ⚙️ [Obsidian Configuration](docs/subproject/dotenv/dotenv_readme.md)
A minimalist, annotation-driven system for mapping environment variables (`.env`) directly to Java fields.
- **Automatic mapping** and type conversion
- **Default values** and required validation
- Clean and intuitive API

```java
@Env("DB_URL")
@RequiredEnv
String dbUrl;

@Env("PORT")
@Default("8080")
int port;
```

### 🪞 [Obsidian Reflection](docs/subproject/reflect/reflect_readme.md)
A fluent and powerful API that makes working with java.lang.reflect safe and intuitive.
- Chainable, builder-pattern reflection
- Semantic filters for methods and fields
- Simple instantiation without boilerplate

```java
// Invoke all getters effortlessly
Reflect.on(user)
    .methods()
    .getters()
    .each(System.out::println);
```

### ⏳ [Obsidian Promise](docs/subproject/promise/README.md)
Modern, functional asynchronous computation built on top of Java concurrency.
- Thread-safe, immutable futures
- Advanced functional composition (`map`, `flatMap`)
- Robust error recovery and timeout management

```java
Promise<User> user = loadUser(id)
    .timeout(Duration.ofSeconds(5))
    .retry(3)
    .recover(e -> User.guest());
```

### 🧩 [Obsidian Collections & Core](#)
High-performance fundamental utilities, functional tools, and advanced data structures.
- **Try<T>** for elegant error handling without `try/catch` hell
- **Box<T>** thread-safe mutable state containers (Atomic, Volatile, Plain)
- **When API** for fluent conditional chains and pattern matching

```java
Try<Integer> score = Try.of(() -> parseScore())
    .map(n -> n * 2)
    .recover(e -> 0);

When.when(user.isPremium())
    .then(() -> applyDiscount())
    .execute();
```

---

## 🚀 Installation & Quick Start

Obsidian is modular. You can import the entire framework or just the modules you need.
**Artifact Group:** `io.obsidian`

### Maven Configuration

Add the dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>io.obsidian</groupId>
        <artifactId>obsidian</artifactId> <!-- Core Module -->
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>io.obsidian</groupId>
        <artifactId>obsidian-configuration</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    <!-- Add other modules: obsidian-reflection, obsidian-promise, obsidian-collections -->
</dependencies>
```

### Gradle Configuration (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.obsidian:obsidian:1.0.0-SNAPSHOT")
    implementation("io.obsidian:obsidian-configuration:1.0.0-SNAPSHOT")
    implementation("io.obsidian:obsidian-reflection:1.0.0-SNAPSHOT")
    implementation("io.obsidian:obsidian-promise:1.0.0-SNAPSHOT")
}
```

### 🔑 Using GitHub Packages

Obsidian is published to GitHub Packages. To pull Obsidian modules via GitHub Packages, authenticate your Gradle/Maven build using your GitHub Personal Access Token (PAT).

**For Gradle (`build.gradle.kts`):**
```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/nadezhdkov/Obsidian")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}
```
*(Store `gpr.user` and `gpr.key` securely in your `~/.gradle/gradle.properties`)*

---

## 🏗️ Development & Building

The project is structured as a multi-module Gradle build, requiring **Java 21**.

```bash
# Clone the repository
git clone https://github.com/nadezhdkov/Obsidian.git
cd Obsidian

# Compile and Build
./gradlew build

# Run all test suites
./gradlew test

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

### Project Structure
- `obsidian-configuration`: Environment variable mapping framework
- `obsidian-reflection`: Fluent Reflection API
- `obsidian-promise`: Async processing utilities
- `obsidian-collections`: Specialized collections and data streams
- `src/main/java/obsidian`: Core framework (Functional interfaces, Control Flow, Concurrent utilities)

---

## 🤝 Contributing

We welcome community contributions! Please read our guidelines to get started:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Verify your code with `./gradlew build`
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## 🐛 Issues & Support

Found an issue or have a feature request? Let us know!
- Create a [bug report](https://github.com/nadezhdkov/Obsidian/issues) on GitHub.
- For usage queries, check out the documentation in the `/docs` directory.

## 📄 License

Obsidian is completely open-source and released under the [Apache License 2.0](LICENSE.md).

<div align="center">
  <b>Developed with ❤️ using modern Java</b>
</div>
