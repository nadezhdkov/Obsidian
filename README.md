# 🔮 Obsidian

A modern and minimalist Java framework with powerful utility libraries to simplify common development tasks.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.md)
[![Java Version](https://img.shields.io/badge/java-11%2B-orange.svg)](https://www.java.com/)
[![Maven Central](https://img.shields.io/badge/maven--central-1.0.0-brightgreen.svg)]()

**📖 Available Languages:** [English](./README.md) | [Português Brasileiro](./docs/i18n/pt-BR/README.md) | [Deutsch](./docs/i18n/de-DE/README.md) | [Español](./docs/i18n/es-ES/README.md)

## 📚 About

Obsidian is a collection of independent and highly specialized modules that make Java development easier. Each module is designed to solve specific problems with a clear, fluent, and easy-to-use API.

### Available Modules

#### 🔧 [Obsidian Configuration](docs/subproject/dotenv/dotenv_readme.md)

Minimalist **annotations** system to map environment variables (`.env`) to Java fields.

**Features:**
- ✅ Automatic environment variable mapping
- ✅ Default values support
- ✅ Required variables validation
- ✅ Automatic prefixes
- ✅ Automatic type conversion
- ✅ Ignore specific fields

**Quick Example:**
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

#### 🔬 [Obsidian Reflection](docs/subproject/reflect/reflect_readme.md)

Fluent and powerful API for working with reflection in Java in a simple and intuitive way.

**Features:**
- ✅ Fluent API with method chaining
- ✅ Powerful filters for fields and methods
- ✅ Type-safe whenever possible
- ✅ Built-in builder pattern
- ✅ Easy annotation manipulation
- ✅ Zero boilerplate code

**Quick Example:**
```java
// Access fields
Reflect.on(User.class)
    .fields()
    .named("name")
    .set(user, "John");

// Invoke methods
Reflect.on(user)
    .methods()
    .getters()
    .each(System.out::println);

// Create instances
User user = Reflect.on(User.class).newInstance("John", 30);
```

#### 🔄 [Obsidian Promise](docs/subproject/promise/README.md)

Modern asynchronous computation API for handling async operations with elegant composition and error handling.

**Features:**
- ✅ Immutable and thread-safe promises
- ✅ Functional composition (map, flatMap, filter)
- ✅ Robust error handling (recover, recoverWith, catchError)
- ✅ Timeout and retry support
- ✅ Cancellation with tokens
- ✅ Promise combinators (all, any, race)

**Quick Example:**
```java
Promise<User> user = loadUser(userId)
    .timeout(Duration.ofSeconds(10))
    .retry(3)
    .flatMap(u -> loadPreferences(u.getId()))
    .recover(e -> User.empty());

user.onSuccess(u -> System.out.println("User: " + u))
    .onError(e -> System.err.println("Failed: " + e));
```

#### 📦 [Obsidian Functional](docs/package/functional/README.md)

Functional programming utilities including Try<T> for error handling and Failable interfaces.

**Features:**
- ✅ Try<T> for functional error handling
- ✅ Failable interfaces (Consumer, Function, Supplier, Runnable)
- ✅ Elegant failure chaining
- ✅ Type-safe operations
- ✅ Interop with Java Streams

**Quick Example:**
```java
Try<Integer> result = Try.of(() -> Integer.parseInt("123"))
    .map(n -> n * 2)
    .filter(n -> n > 100)
    .recover(e -> 0);

result.ifSuccess(System.out::println)
    .ifFailure(e -> System.err.println(e.getMessage()));
```

#### 🎛️ [Obsidian Control](docs/package/control/README.md)

Fluent conditional expressions and pattern matching utilities (When API).

**Features:**
- ✅ Fluent condition building
- ✅ Pattern matching with match()
- ✅ Precondition validation
- ✅ Chainable decisions
- ✅ Lazy evaluation

**Quick Example:**
```java
String message = When.choose(
    age >= 18,
    () -> "Adult access",
    () -> "Restricted access"
);

When.chain()
    .when(status == PENDING)
    .then(() -> processOrder())
    .when(status == SHIPPED)
    .then(() -> notifyUser())
    .execute();
```

#### 🔄 [Obsidian Concurrent](docs/package/concurrent/README.md)

Thread-safe mutable containers with different synchronization strategies.

**Features:**
- ✅ AtomicBox with CAS operations
- ✅ VolatileBox for visibility
- ✅ PlainBox for single-threaded
- ✅ Box views (read-only transforms)
- ✅ Atomic updates and transformations

**Quick Example:**
```java
Box<Integer> counter = Box.atomic(0);

// Atomic operations
counter.compareAndSet(0, 1);
counter.updateAndGet(n -> n + 1);

// Views
Box<String> view = counter.view(String::valueOf);
```

#### ⚡ [Obsidian Stream](docs/package/stream/README.md)

Utility classes for sequences and ranges with lazy evaluation.

**Features:**
- ✅ Sequence<T> for lazy stream-like operations
- ✅ Range utilities for numeric sequences
- ✅ Efficient transformations
- ✅ Fluent API

**Quick Example:**
```java
Sequence<Integer> nums = Range.from(1).to(100)
    .map(n -> n * 2)
    .filter(n -> n % 3 == 0);

nums.forEach(System.out::println);
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

<dependency>
    <groupId>obsidian.lib</groupId>
    <artifactId>obsidian-promise</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>obsidian.lib</groupId>
    <artifactId>obsidian-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle
```gradle
implementation 'obsidian.lib:obsidian-configuration:1.0.0'
implementation 'obsidian.lib:obsidian-reflection:1.0.0'
implementation 'obsidian.lib:obsidian-promise:1.0.0'
implementation 'obsidian.lib:obsidian-core:1.0.0'
```

### Complete Example

```java
import io.dotenv.annotations.Env;
import io.dotenv.annotations.Default;
import io.dotenv.core.Dotenv;
import lang.reflect.Reflect;
import io.obsidian.promise.api.Promise;
import io.obsidian.promise.api.Promises;
import obsidian.control.When;
import obsidian.util.concurrent.Box;

public class Application {
    
    @Env("APP_NAME")
    @Default("MyApp")
    String appName;
    
    @Env("PORT")
    @Default("8080")
    int port;
    
    public static void main(String[] args) {
        // Load configuration from .env
        Application app = new Application();
        Dotenv.load(app);
        
        // Use reflection to inspect application
        Reflect.on(app)
            .fields()
            .each(f -> System.out.println(f.getName() + " = " + f.get(app)));
        
        // Use promises for async operations
        loadUserData(1)
            .timeout(Duration.ofSeconds(5))
            .retry(2)
            .onSuccess(user -> System.out.println("Loaded: " + user))
            .onError(e -> System.err.println("Error: " + e.getMessage()));
        
        // Use When for fluent conditions
        When.when(app.port > 0)
            .then(() -> System.out.println("Port is valid: " + app.port))
            .execute();
        
        // Use Box for thread-safe values
        Box<Integer> counter = Box.atomic(0);
        counter.updateAndGet(c -> c + 1);
        System.out.println("Counter: " + counter.get());
    }
}
```

## 📖 Documentation

- [Obsidian Configuration - Complete Documentation](docs/subproject/dotenv/dotenv_readme.md)
- [Obsidian Reflection - Complete Documentation](docs/subproject/reflect/reflect_readme.md)
- [Obsidian Promise - Complete Documentation](docs/subproject/promise/README.md)
- [JSON Architecture](docs/subproject/json/json_architecture_and_design.md)
- [Try<T> - Functional Error Handling](docs/package/functional/README.md)
- [When - Fluent Conditionals](docs/package/control/README.md)
- [Box<T> - Thread-Safe Containers](docs/package/concurrent/README.md)
- [Sequence<T> - Lazy Streams](docs/package/stream/README.md)

## 🛠️ Development

### Requirements

- **Java 11** or higher
- **Gradle 7.0** or higher
- **Make** (optional, for using Makefile commands)

### Build the Project

```bash
# Complete build
make build

# Run tests
make test

# Clean build
make clean

# Generate documentation
make javadoc

# Publish locally
make local
```

Or using Gradle directly:

```bash
# Build
./gradlew build

# Tests
./gradlew test

# Check
./gradlew check
```

### Project Structure

```
Obsidian/
├── obsidian-configuration/   # Configuration and .env module
│   ├── src/main/java/
│   │   └── io/dotenv/
│   │       ├── annotations/   # Annotations @Env, @Default, etc
│   │       ├── core/          # Core implementation
│   │       ├── processor/     # Processors
│   │       └── util/          # Utilities
│   └── build.gradle.kts
│
├── obsidian-reflection/      # Reflection module
│   ├── src/main/java/
│   │   └── lang/reflect/
│   │       ├── Reflect*.java  # Main classes
│   │       └── exception/     # Exceptions
│   └── build.gradle.kts
│
├── obsidian-promise/         # Promise module
│   ├── src/main/java/
│   │   └── io/obsidian/promise/
│   │       ├── api/           # Promise API
│   │       ├── combinators/   # Promise combinators
│   │       ├── error/         # Error handling
│   │       └── internal/      # Internal implementation
│   └── build.gradle.kts
│
├── src/main/java/obsidian/  # Core utilities
│   ├── control/              # When and control flow
│   ├── functional/           # Try<T> and functional utilities
│   ├── util/                 # Box<T>, Sequence<T>, Range
│   │   ├── concurrent/       # Thread-safe containers
│   │   └── stream/           # Stream utilities
│   └── api/                  # Core APIs
│
├── docs/                     # Documentation
├── gradle/                   # Gradle wrapper
├── build.gradle.kts          # Root build config
├── settings.gradle.kts       # Subproject configuration
├── Makefile                  # Development targets
└── README.md                 # This file
```

## 📦 Dependencies

- **JetBrains Annotations** - Annotations for better code analysis
- **Gson** - JSON serialization
- **YAML** - YAML support
- **Lombok** - Boilerplate reduction
- **JUnit 5** - Testing framework

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the project
2. Create a branch for your feature (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE.md) - see the license file for details.

## 👨‍💻 Author

Developed with ❤️ by Nadezhda

## 🎯 Roadmap

- [ ] Advanced YAML support
- [ ] Intelligent reflection caching
- [ ] Automatic field validation
- [ ] Spring Framework integration
- [ ] Dedicated Gradle plugin
- [ ] Multi-language documentation

## 💡 Tips & Tricks

### Using Dotenv with Reflection

```java
@Env("DB_HOST")
String host;

// Access all fields annotated with @Env
Reflect.on(this)
    .fields()
    .annotated(Env.class)
    .each(f -> System.out.println(f.getName()));
```

### Filtering Methods

```java
// Find all getters
List<Method> getters = Reflect.on(User.class)
    .methods()
    .getters()
    .list();

// Custom filters
Reflect.on(service)
    .methods()
    .isPublic()
    .notStatic()
    .startWith("handle")
    .each(method -> /* process */);
```

## 🐛 Report Issues

Found a bug? Please open an [issue](https://github.com/nadezhdkov/obsidian/issues) with:

- Clear problem description
- Steps to reproduce
- Expected vs actual behavior
- Java and Obsidian version

## 📞 Support

For additional support:

- 📧 Email: seu-email@exemplo.com
- 💬 GitHub Issues
- 📚 Check the complete documentation in the `/doc` folders

---

**Developed with ❤️ using Java and Gradle**
