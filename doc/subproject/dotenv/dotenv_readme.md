# Dotenv Annotations

Sistema minimalista de annotations para mapear variáveis de ambiente (.env) para campos Java.

## 📦 Estrutura

```
io.dotenv.annotations/
├── Env.java              - Mapeia variável de ambiente
├── Default.java          - Define valor padrão
├── RequiredEnv.java      - Marca como obrigatório
├── EnvPrefix.java        - Prefixo automático
├── EnvIgnore.java        - Ignora campo
├── DotenvScanner.java    - API principal
└── core/
    ├── DotenvInjector.java
    ├── DotenvTypeConverter.java
    └── DotenvInjectionException.java
```

## 🚀 Uso Básico

### 1. Configuração Simples

```java
public class AppConfig {
    
    @Env("DB_HOST")
    String host;
    
    @Env("DB_PORT")
    @Default("3306")
    int port;
    
    @Env("DEBUG")
    boolean debug;
}

// Uso
AppConfig config = new AppConfig();
DotenvScanner.scan(config);
```

### 2. Usando Prefixo

```java
@EnvPrefix("DB_")
public class DatabaseConfig {
    
    @Env("HOST")        // DB_HOST
    String host;
    
    @Env("PORT")        // DB_PORT
    @Default("5432")
    int port;
    
    @Env("USER")        // DB_USER
    @RequiredEnv
    String user;
}
```

### 3. Campos Obrigatórios

```java
public class ApiConfig {
    
    @Env("API_KEY")
    @RequiredEnv
    String apiKey;  // Lança exceção se não existir
    
    @Env("API_SECRET")
    @RequiredEnv(message = "API_SECRET é obrigatório")
    String apiSecret;
}
```

### 4. Ignorar Campos

```java
public class Config {
    
    @Env("DB_HOST")
    String host;
    
    @EnvIgnore
    String tempData;  // Não será injetado
}
```

## 📋 Annotations

### @Env
Mapeia variável de ambiente para o campo.

```java
@Env("DB_HOST")
String host;
```

### @Default
Define valor padrão se variável não existir.

```java
@Env("DB_PORT")
@Default("3306")
int port;
```

### @RequiredEnv
Marca variável como obrigatória.

```java
@Env("API_KEY")
@RequiredEnv
String apiKey;
```

### @EnvPrefix
Prefixo automático para toda a classe.

```java
@EnvPrefix("REDIS_")
class RedisConfig {
    @Env("HOST") String host;  // REDIS_HOST
}
```

### @EnvIgnore
Ignora campo na injeção.

```java
@EnvIgnore
String debugInfo;
```

## 🔄 Tipos Suportados

- **Primitivos**: `int`, `long`, `double`, `float`, `boolean`
- **Wrappers**: `Integer`, `Long`, `Double`, `Float`, `Boolean`
- **String**: texto simples
- **Enum**: conversão automática
- **Collections**: `List<String>`, `Set<String>` (separados por vírgula)
- **Java Time**: `Duration`
- **IO**: `Path`, `File`

### Exemplo de Tipos

```java
public class ComplexConfig {
    
    @Env("ALLOWED_IPS")
    List<String> allowedIps;  // "127.0.0.1,192.168.0.1"
    
    @Env("LOG_LEVEL")
    LogLevel logLevel;  // Enum
    
    @Env("TIMEOUT")
    Duration timeout;  // "PT30S"
    
    @Env("DATA_DIR")
    Path dataDir;
}
```

## 📝 Arquivo .env

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_USER=admin
DB_PASSWORD=secret

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# API
API_KEY=your-key-here
API_TIMEOUT=30

# Lists
ALLOWED_IPS=127.0.0.1,192.168.0.1,10.0.0.1
```

## 🎯 API Principal

### DotenvScanner.scan()

```java
// Usa .env padrão
DotenvScanner.scan(config);

// Usa Dotenv customizado
Dotenv dotenv = Dotenv.configure()
    .filename(".env.prod")
    .load();
    
DotenvScanner.scan(config, dotenv);
```

## 🧪 Exemplo Completo

```java
@EnvPrefix("APP_")
public class ApplicationConfig {
    
    @Env("NAME")
    @RequiredEnv
    String name;
    
    @Env("PORT")
    @Default("8080")
    int port;
    
    @Env("DEBUG")
    boolean debug;
    
    @Env("ALLOWED_HOSTS")
    List<String> allowedHosts;
    
    @EnvIgnore
    String tempData;
}

// .env
// APP_NAME=MyApp
// APP_PORT=8080
// APP_DEBUG=true
// APP_ALLOWED_HOSTS=localhost,example.com

public class Main {
    public static void main(String[] args) {
        ApplicationConfig config = new ApplicationConfig();
        DotenvScanner.scan(config);
        
        System.out.println("Name: " + config.name);
        System.out.println("Port: " + config.port);
        System.out.println("Debug: " + config.debug);
        System.out.println("Hosts: " + config.allowedHosts);
    }
}
```

## ⚠️ Tratamento de Erros

```java
try {
    DotenvScanner.scan(config);
} catch (DotenvInjectionException e) {
    System.err.println("Erro na injeção: " + e.getMessage());
}
```

## 🎨 Conversores Customizados

```java
// Registrar conversor customizado
DotenvTypeConverter.register(URL.class, value -> {
    try {
        return new URL(value);
    } catch (MalformedURLException e) {
        throw new IllegalArgumentException("Invalid URL: " + value, e);
    }
});

// Usar
@Env("API_URL")
URL apiUrl;
```

## 📚 Princípios de Design

### 1 annotation = 1 responsabilidade

Este sistema **NÃO** inclui:
- ❌ Criptografia (`@Decrypt`)
- ❌ Profiles complexos (`@Profile`)
- ❌ Hot reload (`@Reloadable`)
- ❌ Dependency injection pesado

Essas responsabilidades devem ficar em módulos separados.

## 🔧 Integração

```java
// Inicialização típica de aplicação
public class Application {
    
    public static void main(String[] args) {
        // 1. Carrega dotenv
        Dotenv dotenv = Dotenv.load();
        
        // 2. Cria configurações
        DatabaseConfig dbConfig = new DatabaseConfig();
        RedisConfig redisConfig = new RedisConfig();
        
        // 3. Injeta
        DotenvScanner.scan(dbConfig, dotenv);
        DotenvScanner.scan(redisConfig, dotenv);
        
        // 4. Usa
        connectToDatabase(dbConfig);
        connectToRedis(redisConfig);
    }
}
```

## 📄 Licença

Mesmo modelo de licença da biblioteca base `io.dotenv`.
