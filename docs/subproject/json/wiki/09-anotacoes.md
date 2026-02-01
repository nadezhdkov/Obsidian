# 9️⃣ Anotações: Controlando Serialização

## 🎯 O que São Anotações JSON?

Anotações são metadados que você coloca em seus campos e classes para controlar como eles são serializados e desserializados. Elas permitem customizar o comportamento padrão.

## ✅ Ativando Anotações

Primeiro, habilite o suporte a anotações ao criar o mapper:

```java
JsonMapper mapper = Json.builder()
    .enableAnnotations(true)  // ← Importante!
    .build()
    .buildMapper();
```

## 📋 Anotações Disponíveis

### 1️⃣ @JsonName - Renomear Campos

Use para especificar um nome diferente em JSON:

```java
public class User {
    @JsonName(\"user_id\")
    private String id;
    
    @JsonName(\"full_name\")
    private String name;
    
    private int age;  // Sem anotação - usa \"age\"
}

// JSON esperado:
// {
//   \"user_id\": \"123\",
//   \"full_name\": \"João Silva\",
//   \"age\": 30
// }
```

**Caso de Uso:**
- API com naming convention diferente (snake_case vs camelCase)
- Compatibilidade com APIs legadas

```java
// Serializar
User user = new User();
user.id = \"123\";
user.name = \"João\";
user.age = 30;

String json = mapper.stringify(mapper.encode(user));
// {\"user_id\":\"123\",\"full_name\":\"João\",\"age\":30}

// Desserializar
String json = \"{\\\"user_id\\\":\\\"123\\\",\\\"full_name\\\":\\\"João\\\",\\\"age\\\":30}\";
User loaded = mapper.decode(JsonSource.of(json), TypeRef.of(User.class));
System.out.println(loaded.name);  // \"João\"
```

### 2️⃣ @JsonIgnore - Ignorar Campos

Use para campos que não devem ser serializados:

```java
public class User {
    private String name;
    
    @JsonIgnore
    private String password;  // Nunca será serializado
    
    @JsonIgnore
    private transient String tempToken;  // Temporário
    
    private int age;
}

// Serializar
User user = new User();
user.name = \"João\";
user.password = \"secret123\";
user.age = 30;

String json = mapper.stringify(mapper.encode(user));
// {\"name\":\"João\",\"age\":30}
// ← password está ausente!
```

**Caso de Uso:**
- Dados sensíveis (senhas, tokens)
- Campos internos/transitórios
- Campos derivados que podem ser recalculados

### 3️⃣ @JsonRequired - Campos Obrigatórios

Use para marcar campos que devem estar presentes ao desserializar:

```java
public class User {
    @JsonRequired
    private String id;
    
    @JsonRequired
    private String email;
    
    private String phone;  // Opcional
}

// Desserializar com sucesso
String validJson = \"{\\\"id\\\":\\\"123\\\",\\\"email\\\":\\\"joao@example.com\\\"}\";
User user = mapper.decode(JsonSource.of(validJson), TypeRef.of(User.class));

// Desserializar com erro
String invalidJson = \"{\\\"id\\\":\\\"123\\\"}\";  // email está faltando
try {
    User user = mapper.decode(JsonSource.of(invalidJson), TypeRef.of(User.class));
} catch (JsonValidationException e) {
    System.err.println(\"Validação falhou: \" + e.getMessage());
    // \"Field 'email' is required but missing\"
}
```

**Caso de Uso:**
- Validação em tempo de desserialização
- Garantir integridade dos dados
- API contracts strict

### 4️⃣ @JsonDefault - Valores Padrão

Use para fornecer valores padrão quando o campo está ausente:

```java
public class Config {
    @JsonDefault(\"localhost\")
    private String host;
    
    @JsonDefault(\"8080\")
    private int port;
    
    @JsonDefault(\"true\")
    private boolean enableSsl;
    
    private String apiKey;  // Sem padrão
}

// Desserializar com alguns campos faltando
String json = \"{\\\"apiKey\\\":\\\"abc123\\\"}\";
Config config = mapper.decode(JsonSource.of(json), TypeRef.of(Config.class));

System.out.println(config.host);      // \"localhost\" (padrão)
System.out.println(config.port);      // 8080 (padrão)
System.out.println(config.enableSsl); // true (padrão)
System.out.println(config.apiKey);    // \"abc123\" (do JSON)
```

**Nota:** @JsonDefault funciona bem com @JsonRequired:

```java
public class User {
    @JsonRequired
    @JsonDefault(\"guest\")
    private String role;  // Obrigatório, mas com padrão
}
```

**Caso de Uso:**
- Configurações com valores padrão sensatos
- API backwards compatible
- Opções de sistema

### 5️⃣ @JsonAdapter - Codecs Customizados

Use para usar um codec customizado na serialização:

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

public class Order {
    @JsonAdapter(UuidCodec.class)
    private UUID orderId;
    
    private String description;
}

// Usar
Order order = new Order();
order.orderId = UUID.randomUUID();
order.description = \"Laptop\";

String json = mapper.stringify(mapper.encode(order));
// {\"orderId\":\"550e8400-e29b-41d4-a716-446655440000\",\"description\":\"Laptop\"}

// Desserializar
Order loaded = mapper.decode(JsonSource.of(json), TypeRef.of(Order.class));
System.out.println(loaded.orderId);  // UUID object, não string
```

**Caso de Uso:**
- Tipos customizados (UUID, BigDecimal, etc)
- Formatos especiais (data, hora, moeda)
- Validação durante conversão

## 🔄 Combinando Anotações

Você pode combinar múltiplas anotações:

```java
public class User {
    @JsonRequired
    @JsonName(\"user_id\")
    private String id;
    
    @JsonRequired
    @JsonDefault(\"Unknown\")
    private String name;
    
    @JsonDefault(\"0\")
    @JsonName(\"birth_year\")
    private int birthYear;
    
    @JsonIgnore
    private String internalId;
    
    @JsonAdapter(UuidCodec.class)
    @JsonName(\"verification_code\")
    private UUID verificationCode;
}

// JSON esperado:
// {
//   \"user_id\": \"123\",
//   \"name\": \"João\",
//   \"birth_year\": 1990,
//   \"verification_code\": \"550e8400-e29b-41d4-a716-446655440000\"
// }
// (internalId não aparece)
```

## 🛡️ Validação com Anotações

```java
public class ValidationExample {
    
    public static void main(String[] args) {
        JsonMapper mapper = Json.builder()
            .enableAnnotations(true)
            .build()
            .buildMapper();
        
        String validJson = \"{\\\"id\\\":\\\"1\\\",\\\"email\\\":\\\"test@example.com\\\"}\";
        String invalidJson = \"{\\\"id\\\":\\\"1\\\"}\";  // Sem email
        
        try {
            // ✅ Válido
            User user1 = mapper.decode(
                JsonSource.of(validJson),
                TypeRef.of(User.class)
            );
            System.out.println(\"Usuário carregado: \" + user1.email);
        } catch (JsonValidationException e) {
            System.err.println(\"Validação falhou: \" + e.getMessage());
        }
        
        try {
            // ❌ Inválido - email é @JsonRequired
            User user2 = mapper.decode(
                JsonSource.of(invalidJson),
                TypeRef.of(User.class)
            );
        } catch (JsonValidationException e) {
            System.err.println(\"Validação falhou: \" + e.getMessage());
            // \"Field 'email' is required but missing\"
        }
    }
}
```

## 💡 Padrões Úteis

### Padrão 1: DTO com Anotações

```java
public class UserDTO {
    @JsonRequired
    @JsonName(\"user_id\")
    public String id;
    
    @JsonRequired
    public String email;
    
    @JsonDefault(\"Active\")
    public String status;
    
    @JsonIgnore
    public String internalNotes;
}

// Converter para entidade
public User toEntity() {
    User user = new User();
    user.id = this.id;
    user.email = this.email;
    user.status = this.status;
    return user;
}
```

### Padrão 2: Validação em Camadas

```java
public class UserValidator {
    
    private final JsonMapper mapper;
    
    public UserValidator() {
        this.mapper = Json.builder()
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    // Validar durante desserialização
    public User validateAndDecode(String json) {
        try {
            return mapper.decode(JsonSource.of(json), TypeRef.of(User.class));
        } catch (JsonValidationException e) {
            throw new InvalidUserException(\"Campos obrigatórios faltando\", e);
        } catch (JsonMappingException e) {
            throw new InvalidUserException(\"Tipos incompatíveis\", e);
        }
    }
}
```

### Padrão 3: API Response com Anotações

```java
public class ApiResponse<T> {
    @JsonRequired
    public String status;
    
    @JsonDefault(\"Unknown\")
    public String message;
    
    public T data;
    
    @JsonIgnore
    private long timestamp = System.currentTimeMillis();
}

public class ApiClient {
    
    private final JsonMapper mapper;
    
    public ApiClient() {
        this.mapper = Json.builder()
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    public <T> ApiResponse<T> parseResponse(String json, TypeRef<T> dataType) {
        // Validação acontece automaticamente via @JsonRequired
        return mapper.decode(
            JsonSource.of(json),
            new TypeRef<ApiResponse<T>>() {}
        );
    }
}
```

## 🎯 Exemplo Completo

```java
public class UserManagement {
    
    @Data
    public static class User {
        @JsonRequired
        @JsonName(\"user_id\")
        private String id;
        
        @JsonRequired
        private String email;
        
        @JsonDefault(\"Regular\")
        @JsonName(\"user_role\")
        private String role;
        
        @JsonDefault(\"0\")
        @JsonName(\"birth_year\")
        private int birthYear;
        
        @JsonAdapter(LocalDateCodec.class)
        @JsonName(\"created_at\")
        private LocalDate createdAt;
        
        @JsonIgnore
        private String password;
        
        @JsonIgnore
        private String internalNotes;
    }
    
    public static void main(String[] args) throws IOException {
        JsonMapper mapper = Json.builder()
            .enableAnnotations(true)
            .prettyPrint(true)
            .build()
            .buildMapper();
        
        // Criar usuário
        User user = new User();
        user.id = \"usr_123\";
        user.email = \"joao@example.com\";
        user.role = \"Premium\";
        user.password = \"secret123\";
        
        // Serializar - password não aparece
        String json = mapper.stringify(mapper.encode(user));
        System.out.println(\"Serializado:\");
        System.out.println(json);
        
        // Desserializar
        String apiJson = \"{\\\"user_id\\\":\\\"usr_456\\\",\\\"email\\\":\\\"maria@example.com\\\"}\";
        User loaded = mapper.decode(JsonSource.of(apiJson), TypeRef.of(User.class));
        System.out.println(\"\\nDesserializado:\");
        System.out.println(\"ID: \" + loaded.id);
        System.out.println(\"Email: \" + loaded.email);
        System.out.println(\"Role (padrão): \" + loaded.role);
    }
}
```

## 📚 Próximos Passos

1. **[Codecs Customizados](./10-codecs-customizados.md)** - Criar lógica custom
2. **[Configuração](./11-configuracao.md)** - Ajustar comportamento
3. **[Exemplos Completos](./15-exemplos-completos.md)** - Aplicações reais

---

**Anterior:** [8. TypeRef](./08-type-ref.md)  
**Próximo:** [10. Codecs Customizados](./10-codecs-customizados.md)
