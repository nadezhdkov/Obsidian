# 1️⃣2️⃣ Tratamento de Erros

## 🎯 Entender as Exceções

Todas as exceções JSON extendem `JsonException` e incluem informações de contexto úteis para debug.

```
JsonException (base)
├── JsonParseException    → JSON malformado
├── JsonMappingException  → Tipo incompatível
├── JsonValidationException → Validação falhou
└── JsonIoException       → Erro de I/O
```

## 📋 Tipos de Exceção

### 1. JsonParseException

Lançada quando o JSON é sintaticamente inválido:

```java
String invalidJson = \"{\\\"name\\\":\\\"João\\\",}\";  // Vírgula extra

try {
    JsonElement elem = mapper.parse(JsonSource.of(invalidJson));
} catch (JsonParseException e) {
    System.err.println(\"JSON malformado: \" + e.getMessage());
    System.err.println(\"Linha: \" + e.getLine());
    System.err.println(\"Coluna: \" + e.getColumn());
}
```

**Exemplos:**
- `{\"name\":\"João\",}` - Vírgula extra
- `{\"name\":\"João\"` - Chave não fechada
- `{name:\"João\"}` - Chave sem aspas (strict mode)

### 2. JsonMappingException

Lançada quando o tipo não corresponde durante desserialização:

```java
public class User {
    public String name;
    public int age;
}

String json = \"{\\\"name\\\":\\\"João\\\",\\\"age\\\":\\\"trinta\\\"}\";

try {
    User user = mapper.decode(JsonSource.of(json), TypeRef.of(User.class));
} catch (JsonMappingException e) {
    System.err.println(\"Tipo incompatível: \" + e.getMessage());
    System.err.println(\"Campo: \" + e.getPath());
    // \"age\" é string, esperado int
}
```

**Exemplos:**
- String quando esperado número
- Número quando esperado boolean
- Objeto quando esperado array

### 3. JsonValidationException

Lançada quando validação falha (ex: @JsonRequired):

```java
public class User {
    @JsonRequired
    public String id;
    
    @JsonRequired
    public String email;
}

String json = \"{\\\"id\\\":\\\"123\\\"}\";  // email faltando

try {
    User user = mapper.decode(JsonSource.of(json), TypeRef.of(User.class));
} catch (JsonValidationException e) {
    System.err.println(\"Validação falhou: \" + e.getMessage());
    // \"Field 'email' is required but missing\"
}
```

### 4. JsonIoException

Lançada quando há erro de I/O ao ler/escrever:

```java
try {
    User user = JsonFiles.read(
        Paths.get(\"/arquivo/inexistente.json\"),
        TypeRef.of(User.class)
    );
} catch (JsonIoException e) {
    System.err.println(\"Erro de I/O: \" + e.getMessage());
    System.err.println(\"Causa: \" + e.getCause());
}
```

## 🛡️ Tratamento Robusto

### Padrão 1: Catch Específico

```java
try {
    User user = mapper.decode(source, TypeRef.of(User.class));
    processUser(user);
} catch (JsonParseException e) {
    // JSON malformado
    logger.error(\"JSON inválido em linha: \" + e.getLine(), e);
} catch (JsonMappingException e) {
    // Tipo incompatível
    logger.error(\"Tipo incompatível no campo: \" + e.getPath(), e);
} catch (JsonValidationException e) {
    // Validação falhou
    logger.warn(\"Validação falhou: \" + e.getMessage());
} catch (JsonIoException e) {
    // Erro de I/O
    logger.error(\"Erro ao ler arquivo\", e);
} catch (JsonException e) {
    // Outro erro JSON
    logger.error(\"Erro JSON desconhecido\", e);
}
```

### Padrão 2: Fallback com Optional

```java
public Optional<User> loadUser(String json) {
    try {
        return Optional.of(
            mapper.decode(JsonSource.of(json), TypeRef.of(User.class))
        );
    } catch (JsonException e) {
        logger.error(\"Erro ao desserializar usuário\", e);
        return Optional.empty();
    }
}

// Usar
loadUser(json)
    .ifPresent(user -> System.out.println(\"Usuário: \" + user.name))
    .ifPresentOrElse(
        user -> processUser(user),
        () -> handleError()
    );
```

### Padrão 3: Try-with-Resources

```java
try (InputStream input = new FileInputStream(\"data.json\")) {
    User user = mapper.decode(
        JsonSource.of(input),
        TypeRef.of(User.class)
    );
} catch (IOException e) {
    logger.error(\"Erro de I/O\", e);
} catch (JsonException e) {
    logger.error(\"Erro JSON\", e);
}
```

## 🔍 Informações de Debug

### JsonPath

```java
try {
    Map<String, User> users = mapper.decode(
        JsonSource.of(json),
        new TypeRef<Map<String, User>>() {}
    );
} catch (JsonMappingException e) {
    // Saber exatamente onde o erro ocorreu
    System.err.println(\"Erro no caminho: \" + e.getPath());
    // $.users[0].email (por exemplo)
}
```

### Informações de Linha e Coluna

```java
try {
    JsonElement elem = mapper.parse(JsonSource.of(json));
} catch (JsonParseException e) {
    System.err.println(\"Erro em \" + e.getLine() + 
                      \":\" + e.getColumn());
    System.err.println(\"Mensagem: \" + e.getMessage());
}
```

## 💡 Padrões Úteis

### Padrão 1: Wrapper com Recovery

```java
public class SafeJsonMapper {
    
    private final JsonMapper mapper;
    
    public SafeJsonMapper(JsonMapper mapper) {
        this.mapper = mapper;
    }
    
    public <T> T decode(String json, TypeRef<T> type, T defaultValue) {
        try {
            return mapper.decode(JsonSource.of(json), type);
        } catch (JsonException e) {
            logger.warn(\"Erro ao decodificar, usando default\", e);
            return defaultValue;
        }
    }
    
    public <T> List<T> decodeList(String json, TypeRef<List<T>> type) {
        try {
            return mapper.decode(JsonSource.of(json), type);
        } catch (JsonException e) {
            logger.error(\"Erro ao decodificar lista\", e);
            return new ArrayList<>();  // Lista vazia
        }
    }
}

// Usar
SafeJsonMapper safe = new SafeJsonMapper(mapper);
User user = safe.decode(json, TypeRef.of(User.class), new User());
List<User> users = safe.decodeList(json, TypeRef.listOf(User.class));
```

### Padrão 2: Custom Exception

```java
public class DataValidationException extends RuntimeException {
    
    private final String field;
    private final Object value;
    
    public DataValidationException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getValue() {
        return value;
    }
}

public class DataValidator {
    
    private final JsonMapper mapper;
    
    public <T> T validate(String json, TypeRef<T> type) {
        try {
            return mapper.decode(JsonSource.of(json), type);
        } catch (JsonMappingException e) {
            throw new DataValidationException(
                e.getPath().toString(),
                null,
                \"Tipo incompatível: \" + e.getMessage()
            );
        } catch (JsonValidationException e) {
            throw new DataValidationException(
                e.getPath().toString(),
                null,
                \"Campo obrigatório faltando: \" + e.getMessage()
            );
        }
    }
}
```

### Padrão 3: Logging Estruturado

```java
public class LoggingJsonMapper {
    
    private final JsonMapper mapper;
    private final Logger logger;
    
    public <T> T decodeWithLogging(String json, TypeRef<T> type, String context) {
        long startTime = System.currentTimeMillis();
        try {
            T result = mapper.decode(JsonSource.of(json), type);
            long duration = System.currentTimeMillis() - startTime;
            logger.info(\"Desserialização bem-sucedida\",
                Map.of(
                    \"context\", context,
                    \"duration_ms\", duration,
                    \"type\", type.toString()
                )
            );
            return result;
        } catch (JsonException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error(\"Erro na desserialização\",
                Map.of(
                    \"context\", context,
                    \"duration_ms\", duration,
                    \"error\", e.getMessage(),
                    \"path\", e.getPath()
                ),
                e
            );
            throw e;
        }
    }
}
```

## 🎯 Exemplo Completo

```java
public class RobustDataService {
    
    private final JsonMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(RobustDataService.class);
    
    public RobustDataService() {
        this.mapper = Json.builder()
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    public Optional<User> loadUser(String json) {
        try {
            User user = mapper.decode(JsonSource.of(json), TypeRef.of(User.class));
            logger.debug(\"Usuário carregado: {}\", user.id);
            return Optional.of(user);
        } catch (JsonParseException e) {
            logger.error(\"JSON malformado em linha {}: {}\",
                e.getLine(), e.getMessage());
            return Optional.empty();
        } catch (JsonMappingException e) {
            logger.error(\"Tipo incompatível no campo {}: {}\",
                e.getPath(), e.getMessage());
            return Optional.empty();
        } catch (JsonValidationException e) {
            logger.warn(\"Validação falhou: {}\", e.getMessage());
            return Optional.empty();
        } catch (JsonException e) {
            logger.error(\"Erro desconhecido ao processar JSON\", e);
            return Optional.empty();
        }
    }
    
    public List<User> loadUsers(String json) {
        try {
            return mapper.decode(
                JsonSource.of(json),
                TypeRef.listOf(User.class)
            );
        } catch (JsonException e) {
            logger.error(\"Erro ao carregar lista de usuários\", e);
            return Collections.emptyList();
        }
    }
    
    public boolean saveUser(User user, Path file) {
        try {
            String json = mapper.stringify(mapper.encode(user));
            JsonFiles.write(file, json, mapper);
            logger.info(\"Usuário salvo em: {}\", file);
            return true;
        } catch (JsonIoException e) {
            logger.error(\"Erro ao salvar usuário em: {}\", file, e);
            return false;
        } catch (JsonException e) {
            logger.error(\"Erro ao serializar usuário\", e);
            return false;
        }
    }
}

// Usar
RobustDataService service = new RobustDataService();

String json = \"{\\\"id\\\":\\\"123\\\",\\\"email\\\":\\\"joao@example.com\\\"}\";
Optional<User> user = service.loadUser(json);
user.ifPresent(u -> System.out.println(\"Carregado: \" + u.email));

List<User> users = service.loadUsers(listJson);
System.out.println(\"Usuários: \" + users.size());

User newUser = new User();
newUser.id = \"456\";
newUser.email = \"maria@example.com\";
boolean saved = service.saveUser(newUser, Paths.get(\"user.json\"));
```

## 📚 Próximos Passos

1. **[I/O de Arquivos](./13-arquivo-io.md)** - Operações com arquivos
2. **[Padrões Práticos](./14-padroes-praticos.md)** - Best practices
3. **[Exemplos Completos](./15-exemplos-completos.md)** - Aplicações reais

---

**Anterior:** [11. Configuração](./11-configuracao.md)  
**Próximo:** [13. I/O de Arquivos](./13-arquivo-io.md)
