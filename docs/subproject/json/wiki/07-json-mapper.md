# 7️⃣ JsonMapper: Serialização e Desserialização

## 🎯 O que é JsonMapper?

`JsonMapper` é o coração da API JSON do Obsidian. Ele é responsável por:

- **Parse**: Converter JSON string/file em `JsonElement`
- **Stringify**: Converter `JsonElement` em JSON string
- **Encode**: Converter objetos Java em `JsonElement`
- **Decode**: Converter JSON em objetos Java

```
String/File ←→ JsonElement ←→ Java Object
        ↓            ↓           ↓
      Parse      Stringify     Encode/Decode
```

## 🚀 Obtendo um JsonMapper

### Mapper Padrão

```java
// Use para a maioria dos casos
JsonMapper mapper = Json.defaultMapper();
```

### Mapper Customizado

```java
JsonMapper customMapper = Json.builder()
    .prettyPrint(true)        // Formatação legível
    .serializeNulls(false)    // Omite nulls
    .enableAnnotations(true)  // Usa @Json* anotações
    .build()
    .buildMapper();
```

## 📝 Operação 1: Parse (JSON → JsonElement)

Converter JSON string/file em estruturas de dados JSON:

### Parse String

```java
JsonMapper mapper = Json.defaultMapper();

String json = "{\"name\":\"João\",\"age\":30}";
JsonElement element = mapper.parse(JsonSource.of(json));

// Acessar
JsonObject obj = element.asJsonObject();
String name = obj.getAsString("name");  // "João"
int age = obj.getAsInt("age");          // 30
```

### Parse Arquivo

```java
import java.nio.file.Paths;

// De arquivo
JsonElement element = mapper.parse(JsonSource.of(Paths.get("data.json")));

// Ou
File file = new File("data.json");
JsonElement element = mapper.parse(JsonSource.of(file));
```

### Parse Stream

```java
InputStream input = new FileInputStream("data.json");
JsonElement element = mapper.parse(JsonSource.of(input));
```

### Parse com Tratamento de Erro

```java
try {
    JsonElement element = mapper.parse(JsonSource.of(json));
} catch (JsonParseException e) {
    // JSON malformado
    System.err.println("Erro de parse: " + e.getMessage());
    System.err.println("Posição: " + e.getPath());
}
```

## 🖨️ Operação 2: Stringify (JsonElement → String)

Converter estruturas JSON em string:

### Stringify Básico

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");
obj.addProperty("age", 30);

String json = mapper.stringify(obj);
System.out.println(json);
// {"name":"João","age":30}
```

### Stringify com Pretty Print

```java
JsonMapper prettyMapper = Json.builder()
    .prettyPrint(true)
    .build()
    .buildMapper();

String json = prettyMapper.stringify(obj);
System.out.println(json);
// {
//   "name": "João",
//   "age": 30
// }
```

### Stringify para Arquivo

```java
JsonElement element = mapper.parse(JsonSource.of(json));

// Usar mapper.stringify() e salvar
String result = mapper.stringify(element);
Files.write(Paths.get("output.json"), result.getBytes());

// Ou usar JsonFiles.write()
JsonFiles.write(Paths.get("output.json"), element);
```

## 🔄 Operação 3: Encode (Objeto Java → JsonElement)

Converter objetos Java em estruturas JSON:

### Encode Simples

```java
public class User {
    public String name;
    public int age;
    
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

User user = new User("João", 30);
JsonElement element = mapper.encode(user);

// Resultado: JsonObject com {"name":"João","age":30}
String json = mapper.stringify(element);
System.out.println(json);
```

### Encode com Coleções

```java
List<User> users = Arrays.asList(
    new User("João", 30),
    new User("Maria", 28),
    new User("Pedro", 35)
);

JsonElement element = mapper.encode(users);
String json = mapper.stringify(element);
// [{"name":"João","age":30},{"name":"Maria","age":28},...]
```

### Encode com Mapa

```java
Map<String, User> userMap = new HashMap<>();
userMap.put("user1", new User("João", 30));
userMap.put("user2", new User("Maria", 28));

JsonElement element = mapper.encode(userMap);
String json = mapper.stringify(element);
// {"user1":{"name":"João","age":30},"user2":{"name":"Maria","age":28}}
```

## 🎯 Operação 4: Decode (JSON → Objeto Java)

Converter JSON em objetos Java type-safe:

### Decode Simples

```java
String json = "{\"name\":\"João\",\"age\":30}";

User user = mapper.decode(
    JsonSource.of(json),
    TypeRef.of(User.class)
);

System.out.println(user.name);  // "João"
System.out.println(user.age);   // 30
```

### Decode com Tipos Genéricos

```java
String json = "[" +
    "{\"name\":\"João\",\"age\":30}," +
    "{\"name\":\"Maria\",\"age\":28}" +
    "]";

// Usar TypeRef para listas
List<User> users = mapper.decode(
    JsonSource.of(json),
    TypeRef.listOf(User.class)
);

for (User user : users) {
    System.out.println(user.name);
}
```

### Decode de Arquivo

```java
User user = JsonFiles.read(
    Paths.get("user.json"),
    TypeRef.of(User.class)
);

// Com mapper customizado
List<User> users = JsonFiles.read(
    Paths.get("users.json"),
    TypeRef.listOf(User.class),
    mapper
);
```

### Decode com Tratamento de Erro

```java
try {
    User user = mapper.decode(
        JsonSource.of(json),
        TypeRef.of(User.class)
    );
} catch (JsonMappingException e) {
    // Tipo não corresponde
    System.err.println("Erro de mapping: " + e.getMessage());
} catch (JsonValidationException e) {
    // @JsonRequired falhou
    System.err.println("Validação falhou: " + e.getMessage());
}
```

## 🔄 Pipeline Completo: String → Objeto → String

```java
// 1. Começar com JSON string
String jsonString = "{\"name\":\"João\",\"age\":30}";

// 2. Parse para JsonElement
JsonElement element = mapper.parse(JsonSource.of(jsonString));

// 3. Decode para Objeto Java
User user = mapper.decode(
    JsonSource.of(jsonString),
    TypeRef.of(User.class)
);

// 4. Modificar objeto
user.age = 31;

// 5. Encode de volta para JSON
JsonElement modified = mapper.encode(user);

// 6. Stringify para string
String outputJson = mapper.stringify(modified);
System.out.println(outputJson);
```

## 🎛️ Configurações de Serialização

### Pretty Print

```java
JsonMapper prettyMapper = Json.builder()
    .prettyPrint(true)
    .build()
    .buildMapper();

// Output formatado e legível
// {
//   "name": "João"
// }
```

### Serializar Nulls

```java
// Com nulls
JsonMapper mapper1 = Json.builder()
    .serializeNulls(true)
    .build()
    .buildMapper();
// { "name": "João", "email": null }

// Sem nulls
JsonMapper mapper2 = Json.builder()
    .serializeNulls(false)
    .build()
    .buildMapper();
// { "name": "João" }
```

### Modo Lenient

```java
JsonMapper lenient = Json.builder()
    .lenient(true)  // Aceita JSON não-estrito
    .build()
    .buildMapper();

// Aceita:
// - Strings sem aspas
// - Números sem formato
// - Comentários (dependendo da config)
```

### Formato de Data

```java
JsonMapper customDate = Json.builder()
    .dateFormat("yyyy-MM-dd")  // ISO
    .build()
    .buildMapper();

// java.time.LocalDate "2024-01-15" → "2024-01-15"
```

### Escapar HTML

```java
JsonMapper htmlEscape = Json.builder()
    .htmlEscaping(true)
    .build()
    .buildMapper();

// "<script>" → "\u003cscript\u003e"
```

## 💡 Padrões Úteis

### Padrão 1: Round-trip (Serialization/Deserialization)

```java
public class DataPersistence {
    
    private final JsonMapper mapper;
    
    public DataPersistence() {
        mapper = Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    // Salvar objeto
    public void save(String filename, User user) throws IOException {
        String json = mapper.stringify(mapper.encode(user));
        Files.write(Paths.get(filename), json.getBytes());
    }
    
    // Carregar objeto
    public User load(String filename) throws IOException {
        String json = Files.readString(Paths.get(filename));
        return mapper.decode(
            JsonSource.of(json),
            TypeRef.of(User.class)
        );
    }
}
```

### Padrão 2: Conversão Entre Formatos

```java
public class DataConverter {
    
    private final JsonMapper mapper;
    
    public DataConverter() {
        mapper = Json.defaultMapper();
    }
    
    // Converter objeto para Map
    public Map<String, Object> toMap(Object obj) {
        JsonElement element = mapper.encode(obj);
        // Converter JsonObject para Map
        return jsonObjectToMap(element.asJsonObject());
    }
    
    private Map<String, Object> jsonObjectToMap(JsonObject obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            map.put(entry.getKey(), jsonElementToObject(entry.getValue()));
        }
        return map;
    }
    
    private Object jsonElementToObject(JsonElement elem) {
        if (elem.isJsonNull()) return null;
        if (elem.isJsonPrimitive()) {
            JsonPrimitive prim = elem.asJsonPrimitive();
            if (prim.isString()) return prim.asString();
            if (prim.isNumber()) return prim.asNumber();
            if (prim.isBoolean()) return prim.asBoolean();
        }
        if (elem.isJsonObject()) return jsonObjectToMap(elem.asJsonObject());
        if (elem.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement e : elem.asJsonArray()) {
                list.add(jsonElementToObject(e));
            }
            return list;
        }
        return null;
    }
}
```

### Padrão 3: Cópia Profunda

```java
public class ObjectCloner {
    
    private final JsonMapper mapper;
    
    public ObjectCloner() {
        mapper = Json.defaultMapper();
    }
    
    // Copiar objeto através de serialização
    public <T> T deepCopy(T obj, TypeRef<T> type) {
        // Objeto → JSON → Objeto (cópia profunda!)
        JsonElement json = mapper.encode(obj);
        return mapper.decode(
            JsonSource.of(mapper.stringify(json)),
            type
        );
    }
}

// Usar
User original = new User("João", 30);
User copy = cloner.deepCopy(original, TypeRef.of(User.class));
copy.name = "Maria";  // original não é afetado
```

## 🎯 Exemplo Completo

```java
public class UserService {
    
    private final JsonMapper mapper;
    private final Path usersFile = Paths.get("users.json");
    
    public UserService() {
        this.mapper = Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    // Carregar todos os usuários
    public List<User> loadAllUsers() {
        if (!Files.exists(usersFile)) {
            return new ArrayList<>();
        }
        
        String json = Files.readString(usersFile);
        return mapper.decode(
            JsonSource.of(json),
            TypeRef.listOf(User.class)
        );
    }
    
    // Salvar usuários
    public void saveAllUsers(List<User> users) {
        String json = mapper.stringify(mapper.encode(users));
        Files.write(usersFile, json.getBytes());
    }
    
    // Adicionar usuário
    public void addUser(User user) {
        List<User> users = loadAllUsers();
        users.add(user);
        saveAllUsers(users);
    }
    
    // Encontrar por ID
    public Optional<User> findById(String id) {
        return loadAllUsers().stream()
            .filter(u -> u.id.equals(id))
            .findFirst();
    }
}
```

## 📚 Próximos Passos

1. **[TypeRef](./08-type-ref.md)** - Trabalhar com tipos genéricos
2. **[Anotações](./09-anotacoes.md)** - Controlar serialização
3. **[I/O de Arquivos](./13-arquivo-io.md)** - Operações avançadas

---

**Anterior:** [6. JsonArray](./06-json-array.md)  
**Próximo:** [8. TypeRef: Generics e Tipos Complexos](./08-type-ref.md)
