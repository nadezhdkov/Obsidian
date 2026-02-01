# 5️⃣ JsonObject: Objetos JSON

## 🎯 O que é JsonObject?

`JsonObject` representa um objeto JSON com pares **chave-valor**. É como um `Map<String, JsonElement>` em Java:

```
JSON:        { "name": "João", "age": 30 }
             ↓
Java:        JsonObject
             - "name" → JsonPrimitive("João")
             - "age"  → JsonPrimitive(30)
```

## 🏗️ Criando JsonObjects

### Criar Vazio

```java
JsonObject obj = new JsonObject();
```

### Adicionar Propriedades

```java
JsonObject user = new JsonObject();

// Adicionar diferentes tipos
user.addProperty("name", "João");        // String
user.addProperty("age", 30);             // int
user.addProperty("salary", 5000.50);     // double
user.addProperty("active", true);        // boolean
user.addProperty("score", 4.5f);         // float

// Resultado JSON:
// {
//   "name": "João",
//   "age": 30,
//   "salary": 5000.50,
//   "active": true,
//   "score": 4.5
// }
```

### Adicionar Null

```java
JsonObject user = new JsonObject();
user.addProperty("name", "João");
user.add("email", JsonNull.INSTANCE);    // null

// JSON: { "name": "João", "email": null }
```

### Adicionar Elementos Complexos

```java
JsonObject person = new JsonObject();
person.addProperty("name", "João");

// Adicionar objeto aninhado
JsonObject address = new JsonObject();
address.addProperty("city", "São Paulo");
address.addProperty("country", "Brasil");
person.add("address", address);

// Adicionar array
JsonArray hobbies = new JsonArray();
hobbies.add("futebol");
hobbies.add("música");
hobbies.add("leitura");
person.add("hobbies", hobbies);

// JSON completo:
// {
//   "name": "João",
//   "address": {
//     "city": "São Paulo",
//     "country": "Brasil"
//   },
//   "hobbies": ["futebol", "música", "leitura"]
// }
```

## 🔍 Acessando Valores

### Verificar Existência

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");

// Verificar se chave existe
if (obj.has("name")) {
    System.out.println("Nome existe");
}

// Se não existir, retorna null
JsonElement email = obj.get("email");  // null
```

### Acessar Valores Básicos

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");
obj.addProperty("age", 30);
obj.addProperty("active", true);
obj.addProperty("salary", 5000.50);

// Acessar com tipo
String name = obj.getAsString("name");      // "João"
int age = obj.getAsInt("age");              // 30
boolean active = obj.getAsBoolean("active"); // true
double salary = obj.getAsDouble("salary");  // 5000.50

// Acessar genérico (retorna JsonElement)
JsonElement nameElem = obj.get("name");
```

### Acessar Objetos Aninhados

```java
JsonObject person = new JsonObject();
JsonObject address = new JsonObject();
address.addProperty("city", "São Paulo");
person.add("address", address);

// Acessar objeto aninhado
JsonObject addr = person.getAsJsonObject("address");
String city = addr.getAsString("city");  // "São Paulo"
```

### Acessar Arrays

```java
JsonObject person = new JsonObject();
JsonArray hobbies = new JsonArray();
hobbies.add("futebol");
hobbies.add("música");
person.add("hobbies", hobbies);

// Acessar array
JsonArray arr = person.getAsJsonArray("hobbies");
String first = arr.get(0).asJsonPrimitive().asString();  // "futebol"
```

## 📋 Iterando JsonObject

### Por Entradas (Chave-Valor)

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");
obj.addProperty("age", 30);
obj.addProperty("city", "São Paulo");

// Iterar com Map.Entry
for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
    String key = entry.getKey();
    JsonElement value = entry.getValue();
    System.out.println(key + " = " + value);
}

// Saída:
// name = "João"
// age = 30
// city = "São Paulo"
```

### Por Chaves

```java
for (String key : obj.keySet()) {
    JsonElement value = obj.get(key);
    System.out.println(key + " → " + value);
}
```

### Por Valores

```java
for (JsonElement value : obj.values()) {
    System.out.println(value);
}
```

### Com Streams (Java 8+)

```java
obj.entrySet().stream()
    .filter(e -> e.getValue().isJsonPrimitive())
    .forEach(e -> System.out.println(e.getKey() + " = " + e.getValue()));
```

## 🔄 Modificar JsonObject

### Atualizar Valor

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");

// Atualizar
obj.addProperty("name", "Maria");  // Substitui
System.out.println(obj.getAsString("name")); // "Maria"
```

### Remover Campo

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");
obj.addProperty("age", 30);

// Remover
obj.remove("age");

System.out.println(obj.has("age")); // false
```

### Limpar Tudo

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");
obj.addProperty("age", 30);

// Limpar
obj.clear();

System.out.println(obj.size()); // 0
```

## 📊 Informações do JsonObject

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");
obj.addProperty("age", 30);
obj.addProperty("city", "São Paulo");

// Tamanho (número de entradas)
int size = obj.size();  // 3

// Verificar se vazio
boolean empty = obj.isEmpty();  // false

// Listar todas as chaves
Set<String> keys = obj.keySet();  // {"name", "age", "city"}

// Listar todos os valores
Collection<JsonElement> values = obj.values();

// Listar entradas
Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();
```

## 🛡️ Tratamento de Erros

### Chave Não Existe

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");

// Tentar acessar chave inexistente
try {
    // Pode lançar exceção
    String email = obj.getAsString("email");
} catch (NullPointerException e) {
    System.err.println("Chave 'email' não existe");
}

// Alternativa segura:
String email = obj.has("email") ? obj.getAsString("email") : null;
```

### Tipo Incorreto

```java
JsonObject obj = new JsonObject();
obj.addProperty("age", 30);

try {
    // age é um número, não string
    String age = obj.getAsString("age");  // Erro!
} catch (ClassCastException e) {
    System.err.println("'age' não é uma string");
}

// Alternativa:
JsonElement ageElem = obj.get("age");
if (ageElem.isJsonPrimitive()) {
    JsonPrimitive prim = ageElem.asJsonPrimitive();
    if (prim.isNumber()) {
        int age = prim.asInt();
    }
}
```

## 💡 Padrões Úteis

### Padrão 1: Builder Fluente

```java
public class UserBuilder {
    private final JsonObject obj = new JsonObject();
    
    public UserBuilder name(String name) {
        obj.addProperty("name", name);
        return this;
    }
    
    public UserBuilder age(int age) {
        obj.addProperty("age", age);
        return this;
    }
    
    public UserBuilder city(String city) {
        obj.addProperty("city", city);
        return this;
    }
    
    public JsonObject build() {
        return obj;
    }
}

// Usar
JsonObject user = new UserBuilder()
    .name("João")
    .age(30)
    .city("São Paulo")
    .build();
```

### Padrão 2: Merge de Objetos

```java
public static JsonObject merge(JsonObject obj1, JsonObject obj2) {
    JsonObject result = new JsonObject();
    
    // Adicionar obj1
    for (Map.Entry<String, JsonElement> entry : obj1.entrySet()) {
        result.add(entry.getKey(), entry.getValue());
    }
    
    // Adicionar obj2 (sobrescreve duplicatas)
    for (Map.Entry<String, JsonElement> entry : obj2.entrySet()) {
        result.add(entry.getKey(), entry.getValue());
    }
    
    return result;
}

// Usar
JsonObject base = new JsonObject();
base.addProperty("name", "João");
base.addProperty("age", 30);

JsonObject updates = new JsonObject();
updates.addProperty("age", 31);
updates.addProperty("city", "São Paulo");

JsonObject merged = merge(base, updates);
// Resultado: { "name": "João", "age": 31, "city": "São Paulo" }
```

### Padrão 3: Validação

```java
public class UserValidator {
    
    public static boolean isValid(JsonObject user) {
        return hasRequiredFields(user) && hasValidTypes(user);
    }
    
    private static boolean hasRequiredFields(JsonObject user) {
        return user.has("name") && 
               user.has("email") && 
               user.has("age");
    }
    
    private static boolean hasValidTypes(JsonObject user) {
        try {
            user.getAsString("name");
            user.getAsString("email");
            user.getAsInt("age");
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}

// Usar
if (UserValidator.isValid(user)) {
    // Processar
} else {
    System.err.println("Usuário inválido");
}
```

## 🎯 Exemplo Completo

```java
public class UserManagement {
    
    private final JsonMapper mapper;
    
    public UserManagement() {
        this.mapper = Json.defaultMapper();
    }
    
    public JsonObject createUser(String name, int age, String city) {
        JsonObject user = new JsonObject();
        user.addProperty("id", UUID.randomUUID().toString());
        user.addProperty("name", name);
        user.addProperty("age", age);
        
        JsonObject address = new JsonObject();
        address.addProperty("city", city);
        user.add("address", address);
        
        return user;
    }
    
    public void updateUser(JsonObject user, String name, int age) {
        user.addProperty("name", name);
        user.addProperty("age", age);
    }
    
    public String getUserInfo(JsonObject user) {
        String name = user.getAsString("name");
        int age = user.getAsInt("age");
        String city = user.getAsJsonObject("address")
                          .getAsString("city");
        
        return String.format("%s, %d anos, %s", name, age, city);
    }
    
    public static void main(String[] args) {
        UserManagement um = new UserManagement();
        
        // Criar usuário
        JsonObject user = um.createUser("João", 30, "São Paulo");
        System.out.println("Criado: " + um.getUserInfo(user));
        
        // Atualizar
        um.updateUser(user, "João Silva", 31);
        System.out.println("Atualizado: " + um.getUserInfo(user));
        
        // Serializar
        String json = um.mapper.stringify(user);
        System.out.println("JSON: " + json);
    }
}
```

## 📚 Próximos Passos

1. **[JsonArray](./06-json-array.md)** - Trabalhar com arrays
2. **[JsonMapper](./07-json-mapper.md)** - Serialização avançada
3. **[Anotações](./09-anotacoes.md)** - Controlar serialização

---

**Anterior:** [4. JsonElement](./04-json-element.md)  
**Próximo:** [6. JsonArray: Arrays JSON](./06-json-array.md)
