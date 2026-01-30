# Obsidian JSON Module

[![Java](https://img.shields.io/badge/Java-11+-blue.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-7.0+-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](../../../LICENSE.md)

A modern, clean, and extensible JSON library for Java that provides its own stable API while using Google Gson as an internal engine.

## 🎯 Design Goals

- **API-First Design**: Clean, intuitive public API that never exposes internal implementation details
- **Engine Independence**: Gson is used internally but can be swapped without breaking client code
- **Immutability**: Thread-safe, immutable configuration
- **Fail-Fast**: Clear, actionable error messages with JSON path tracking
- **Extensibility**: Custom codecs, annotations, and configuration options

## 📦 Installation

### Gradle

```gradle
implementation 'obsidian.lib:obsidian-json:1.0.0'
```

### Maven

```xml
<dependency>
    <groupId>obsidian.lib</groupId>
    <artifactId>obsidian-json</artifactId>
    <version>1.0.0</version>
</dependency>
```


## 🚀 Quick Start

### Basic Usage

```java
import io.json.api.*;
import io.json.api.codec.TypeRef;
import io.json.io.*;

// Parse JSON string
JsonMapper mapper = Json.defaultMapper();
JsonElement element = mapper.parse(JsonSource.of("{\"name\":\"John\"}"));

// Decode to object
User user = mapper.decode(
    JsonSource.of("{\"name\":\"John\",\"age\":30}"),
    TypeRef.of(User.class)
);

// Encode object to JSON
JsonElement encoded = mapper.encode(user);
String json = mapper.stringify(encoded);

// File operations
User fileUser = JsonFiles.read(Paths.get("user.json"), TypeRef.of(User.class));
JsonFiles.write(Paths.get("output.json"), user);
```

### Custom Configuration

```java
JsonMapper customMapper = Json.builder()
    .prettyPrint(true)
    .serializeNulls(false)
    .lenient(true)
    .dateFormat("yyyy-MM-dd")
    .enableAnnotations(true)
    .annotationsMode(AnnotationsMode.OBSIDIAN_ONLY)
    .htmlEscaping(false)
    .build()
    .buildMapper();
```

## 📚 Core Components

### JsonElement Hierarchy

```java
JsonElement (abstract)
├── JsonObject   - Key-value pairs
├── JsonArray    - Ordered list
├── JsonPrimitive - String, number, boolean
└── JsonNull     - JSON null value
```

### Working with JsonObject

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "Alice");
obj.addProperty("age", 25);
obj.addProperty("active", true);

// Nested objects
JsonObject address = new JsonObject();
address.addProperty("city", "New York");
obj.add("address", address);

// Access values
String name = obj.get("name").asJsonPrimitive().asString();
int age = obj.getAsJsonPrimitive("age").asInt();
```

### Working with JsonArray

```java
JsonArray array = new JsonArray();
array.add("value1");
array.add(42);
array.add(true);

// Iterate
for (JsonElement element : array) {
    System.out.println(element);
}

// Access by index
JsonElement first = array.get(0);
```

## 🏷️ Annotations

### @JsonName

Specify the JSON field name:

```java
public class User {
    @JsonName("user_name")
    private String username;
    
    @JsonName("first_name")
    private String firstName;
}
```

### @JsonIgnore

Exclude fields from serialization:

```java
public class User {
    private String username;
    
    @JsonIgnore
    private String password;  // Never serialized
}
```

### @JsonRequired

Mark fields as required during deserialization:

```java
public class User {
    @JsonRequired
    private String id;  // Must be present in JSON
    
    @JsonRequired
    private String email;
    
    private String nickname;  // Optional
}
```

### @JsonDefault

Provide default values:

```java
public class Config {
    @JsonDefault("localhost")
    private String host;
    
    @JsonDefault("8080")
    private int port;
    
    @JsonDefault("true")
    private boolean enableSsl;
}
```

### @JsonAdapter

Use custom codecs:

```java
public class Order {
    @JsonAdapter(UuidCodec.class)
    private UUID orderId;
    
    @JsonAdapter(LocalDateCodec.class)
    private LocalDate orderDate;
}
```

## 🔧 Custom Codecs

Create custom serialization logic:

```java
public class UuidCodec implements JsonCodec<UUID> {
    
    @Override
    public JsonElement encode(UUID value) {
        return new JsonPrimitive(value.toString());
    }
    
    @Override
    public UUID decode(JsonElement element) {
        return UUID.fromString(element.asJsonPrimitive().asString());
    }
}
```

## 📝 TypeRef for Generics

Handle generic types safely:

```java
// Simple types
TypeRef<User> userType = TypeRef.of(User.class);

// Generic types
TypeRef<List<User>> userList = new TypeRef<List<User>>() {};
TypeRef<Map<String, User>> userMap = new TypeRef<Map<String, User>>() {};

// Convenience methods
TypeRef<List<String>> strings = TypeRef.listOf(String.class);
TypeRef<Set<Integer>> numbers = TypeRef.setOf(Integer.class);
TypeRef<Map<String, Object>> map = TypeRef.mapOf(String.class, Object.class);
```

## 🚨 Error Handling

All exceptions extend `JsonException` and include JSON path information:

```java
try {
    User user = mapper.decode(source, TypeRef.of(User.class));
} catch (JsonParseException e) {
    // Malformed JSON
    System.err.println("Parse error at: " + e.getPath());
} catch (JsonMappingException e) {
    // Type mismatch or mapping error
    System.err.println("Mapping error at: " + e.getPath());
} catch (JsonValidationException e) {
    // Validation failed (@JsonRequired)
    System.err.println("Validation error at: " + e.getPath());
} catch (JsonIoException e) {
    // File I/O error
    System.err.println("I/O error: " + e.getMessage());
}
```

## 🏗️ Architecture

```
┌─────────────────────────────────────┐
│         Public API Layer            │
│  Json, JsonMapper, JsonElement      │
│  JsonConfig, Annotations, Codecs    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       Internal Layer (private)      │
│    GsonMapper, GsonEngine           │
│    GsonElementBridge                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Google Gson (engine)        │
│      Never exposed to users         │
└─────────────────────────────────────┘
```

## 🎓 Examples

### Complete CRUD Example

```java
public class UserRepository {
    
    private final JsonMapper mapper = Json.builder()
        .prettyPrint(true)
        .build()
        .buildMapper();
    
    private final Path storageFile = Paths.get("users.json");
    
    public void saveUser(User user) {
        List<User> users = loadAllUsers();
        users.add(user);
        JsonFiles.write(storageFile, users, mapper);
    }
    
    public List<User> loadAllUsers() {
        if (!Files.exists(storageFile)) {
            return new ArrayList<>();
        }
        return JsonFiles.read(storageFile, TypeRef.listOf(User.class), mapper);
    }
    
    public Optional<User> findById(String id) {
        return loadAllUsers().stream()
            .filter(u -> u.getId().equals(id))
            .findFirst();
    }
}
```

### Configuration File Example

```java
@Data
public class AppConfig {
    
    @JsonRequired
    @JsonDefault("myapp")
    private String appName;
    
    @JsonRequired
    @JsonDefault("8080")
    private int port;
    
    @JsonDefault("localhost")
    private String host;
    
    private DatabaseConfig database;
    private LoggingConfig logging;
}

// Load configuration
AppConfig config = JsonFiles.read(
    Paths.get("config.json"),
    TypeRef.of(AppConfig.class)
);
```

## 🧪 Testing

```bash
./gradlew test
```

## 📄 License

This project is licensed under the [Apache License 2.0](../../../LICENSE.md) - see the license file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For issues, questions, or suggestions, please open an issue on GitHub.

---

**Built with ❤️ by the Obsidian Team**