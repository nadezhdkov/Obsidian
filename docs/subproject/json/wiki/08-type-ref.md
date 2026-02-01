# 8️⃣ TypeRef: Generics e Tipos Complexos

## 🎯 O Problema com Generics em Java

Em Java, devido a **type erasure**, informações de tipo genérico são perdidas em runtime:

```java
// ❌ Isso NÃO funciona
List<User> users = mapper.decode(source, List.class);
// Type erasure: perdemos <User> em runtime!

// ✅ Isso funciona
List<User> users = mapper.decode(
    source,
    new TypeRef<List<User>>() {}  // TypeRef preserva tipo
);
```

A classe `TypeRef` soluciona este problema mantendo informações de tipo genérico em runtime.

## 📦 Usando TypeRef

### Para Tipos Simples

```java
// Sem generics - direto com Class
User user = mapper.decode(source, TypeRef.of(User.class));

// Equivalente
User user = mapper.decode(source, User.class);
```

### Para Tipos Genéricos (Anônimo)

```java
// List<User>
List<User> users = mapper.decode(
    source,
    new TypeRef<List<User>>() {}
);

// Map<String, User>
Map<String, User> userMap = mapper.decode(
    source,
    new TypeRef<Map<String, User>>() {}
);

// Set<String>
Set<String> tags = mapper.decode(
    source,
    new TypeRef<Set<String>>() {}
);
```

### Para Tipos Complexos (Aninhados)

```java
// Map<String, List<User>>
Map<String, List<User>> data = mapper.decode(
    source,
    new TypeRef<Map<String, List<User>>>() {}
);

// List<Map<String, Object>>
List<Map<String, Object>> records = mapper.decode(
    source,
    new TypeRef<List<Map<String, Object>>>() {}
);

// Map<String, Map<String, User>>
Map<String, Map<String, User>> nested = mapper.decode(
    source,
    new TypeRef<Map<String, Map<String, User>>>() {}
);
```

## 🏭 Factory Methods Convenientes

Para evitar sintaxe anônima verbosa:

### Coleções Simples

```java
// List
TypeRef<List<String>> strings = TypeRef.listOf(String.class);
TypeRef<List<User>> users = TypeRef.listOf(User.class);

// Set
TypeRef<Set<Integer>> numbers = TypeRef.setOf(Integer.class);
TypeRef<Set<String>> tags = TypeRef.setOf(String.class);

// Map
TypeRef<Map<String, User>> map = TypeRef.mapOf(String.class, User.class);
TypeRef<Map<String, Integer>> counts = TypeRef.mapOf(String.class, Integer.class);
```

### Exemplos Práticos

```java
// Ler lista de strings
List<String> lines = mapper.decode(
    source,
    TypeRef.listOf(String.class)
);

// Ler mapa de usuários
Map<String, User> users = mapper.decode(
    source,
    TypeRef.mapOf(String.class, User.class)
);

// Ler conjunto de IDs
Set<Integer> ids = mapper.decode(
    source,
    TypeRef.setOf(Integer.class)
);
```

## 📚 Exemplos Detalhados

### Exemplo 1: Array de Objetos

```json
[
  { "name": "João", "age": 30 },
  { "name": "Maria", "age": 28 }
]
```

```java
// Decodificar
List<User> users = mapper.decode(
    JsonSource.of(json),
    TypeRef.listOf(User.class)
);

// Iterar
for (User user : users) {
    System.out.println(user.name + " - " + user.age);
}
```

### Exemplo 2: Mapa de Objetos

```json
{
  "user1": { "name": "João", "age": 30 },
  "user2": { "name": "Maria", "age": 28 }
}
```

```java
// Decodificar
Map<String, User> userMap = mapper.decode(
    JsonSource.of(json),
    TypeRef.mapOf(String.class, User.class)
);

// Acessar
User user = userMap.get("user1");  // João
```

### Exemplo 3: Estrutura Aninhada

```json
{
  "admin": {
    "name": "Admin",
    "permissions": ["read", "write", "delete"]
  },
  "user": {
    "name": "User",
    "permissions": ["read"]
  }
}
```

```java
// Decodificar
Map<String, Map<String, Object>> data = mapper.decode(
    JsonSource.of(json),
    new TypeRef<Map<String, Map<String, Object>>>() {}
);

// Ou melhor - criar classe para isso
public class RoleConfig {
    public String name;
    public List<String> permissions;
}

Map<String, RoleConfig> roles = mapper.decode(
    JsonSource.of(json),
    new TypeRef<Map<String, RoleConfig>>() {}
);
```

## 🔀 Conversão Genérica

```java
public class JsonConverter {
    
    private final JsonMapper mapper;
    
    public JsonConverter() {
        this.mapper = Json.defaultMapper();
    }
    
    // Converter qualquer tipo
    public <T> T fromJson(String json, TypeRef<T> type) {
        return mapper.decode(JsonSource.of(json), type);
    }
    
    // Converter de volta
    public <T> String toJson(T obj) {
        return mapper.stringify(mapper.encode(obj));
    }
    
    // Copiar e transformar
    public <T> T convert(Object obj, TypeRef<T> targetType) {
        String json = toJson(obj);
        return fromJson(json, targetType);
    }
}

// Usar
JsonConverter converter = new JsonConverter();

// De JSON para List<User>
List<User> users = converter.fromJson(json, TypeRef.listOf(User.class));

// De objeto para JSON
String json = converter.toJson(users);

// De um tipo para outro
List<User> userList = Arrays.asList(new User("João", 30));
Map<String, User> userMap = converter.convert(
    userList,
    new TypeRef<Map<String, User>>() {}
);
```

## 🛡️ Type Safety

TypeRef garante type-safety em compile-time:

```java
// ✅ Correto - tipo verificado
List<String> strings = mapper.decode(
    source,
    TypeRef.listOf(String.class)
);

// ❌ Errado - tipo incompatível
List<Integer> numbers = mapper.decode(
    source,
    TypeRef.listOf(String.class)  // Erro de compilação!
);
```

## 💡 Padrões Úteis

### Padrão 1: Generic Repository

```java
public abstract class Repository<T> {
    
    protected final JsonMapper mapper;
    protected final Path dataFile;
    protected final TypeRef<List<T>> listType;
    
    public Repository(Path dataFile, TypeRef<List<T>> listType) {
        this.mapper = Json.defaultMapper();
        this.dataFile = dataFile;
        this.listType = listType;
    }
    
    public void save(T item) throws IOException {
        List<T> items = loadAll();
        items.add(item);
        String json = mapper.stringify(mapper.encode(items));
        Files.write(dataFile, json.getBytes());
    }
    
    public List<T> loadAll() throws IOException {
        if (!Files.exists(dataFile)) {
            return new ArrayList<>();
        }
        String json = Files.readString(dataFile);
        return mapper.decode(JsonSource.of(json), listType);
    }
}

// Usar
class UserRepository extends Repository<User> {
    public UserRepository() {
        super(Paths.get("users.json"), TypeRef.listOf(User.class));
    }
}

// Aplicar
UserRepository repo = new UserRepository();
repo.save(new User("João", 30));
List<User> users = repo.loadAll();
```

### Padrão 2: API Response Handler

```java
public class ApiResponse<T> {
    public String status;
    public T data;
    public String error;
}

public class ApiClient {
    
    private final JsonMapper mapper;
    
    public ApiClient() {
        this.mapper = Json.defaultMapper();
    }
    
    // Fazer requisição genérica
    public <T> T request(String url, TypeRef<T> responseType) {
        // Fazer chamada HTTP (pseudo-código)
        String response = getHttpResponse(url);
        
        // Decodificar com tipo genérico
        return mapper.decode(JsonSource.of(response), responseType);
    }
    
    // Especializadas
    public User getUser(String id) {
        return request("/users/" + id, TypeRef.of(User.class));
    }
    
    public List<User> getAllUsers() {
        return request("/users", TypeRef.listOf(User.class));
    }
    
    public Map<String, Object> getMetadata() {
        return request("/metadata", 
            new TypeRef<Map<String, Object>>() {}
        );
    }
}
```

### Padrão 3: Data Transformation

```java
public class DataTransformer {
    
    private final JsonMapper mapper;
    
    public DataTransformer() {
        this.mapper = Json.defaultMapper();
    }
    
    // Transformar dados mantendo tipo
    public <T> List<T> filter(List<T> items, Predicate<T> predicate) {
        return items.stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }
    
    // Transformar com JSON como intermediário
    public <S, T> List<T> transformList(
        String json,
        TypeRef<List<S>> sourceType,
        TypeRef<List<T>> targetType,
        Function<S, T> transformer) {
        
        List<S> source = mapper.decode(JsonSource.of(json), sourceType);
        List<T> target = source.stream()
            .map(transformer)
            .collect(Collectors.toList());
        
        return target;
    }
}

// Usar
DataTransformer transformer = new DataTransformer();

// Transformar List<UserDTO> para List<User>
List<User> users = transformer.transformList(
    json,
    TypeRef.listOf(UserDTO.class),
    TypeRef.listOf(User.class),
    UserDTO::toEntity
);
```

## 🎯 Exemplo Completo

```java
public class DataService {
    
    private final JsonMapper mapper;
    
    public DataService() {
        this.mapper = Json.defaultMapper();
    }
    
    // Trabalhar com diferentes tipos
    public void processData(String json) {
        // Parse genérico para Map
        Map<String, Object> data = mapper.decode(
            JsonSource.of(json),
            new TypeRef<Map<String, Object>>() {}
        );
        
        // Acessar dados
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
    
    // Trabalhar com arrays complexos
    public List<User> parseUsers(String json) {
        return mapper.decode(
            JsonSource.of(json),
            TypeRef.listOf(User.class)
        );
    }
    
    // Trabalhar com mapas de listas
    public Map<String, List<User>> parseUsersByRole(String json) {
        return mapper.decode(
            JsonSource.of(json),
            new TypeRef<Map<String, List<User>>>() {}
        );
    }
    
    // Salvar dados mantendo tipo
    public <T> void save(String filename, List<T> items, TypeRef<List<T>> type) {
        String json = mapper.stringify(mapper.encode(items));
        try {
            Files.write(Paths.get(filename), json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 📚 Próximos Passos

1. **[Anotações](./09-anotacoes.md)** - Controlar serialização
2. **[Codecs Customizados](./10-codecs-customizados.md)** - Lógica custom
3. **[Tratamento de Erros](./12-tratamento-erros.md)** - Robustez

---

**Anterior:** [7. JsonMapper](./07-json-mapper.md)  
**Próximo:** [9. Anotações: Controlando Serialização](./09-anotacoes.md)
