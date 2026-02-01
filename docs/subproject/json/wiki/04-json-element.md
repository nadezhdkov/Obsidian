# 4️⃣ JsonElement: Trabalhando com Elementos

## 🎯 Entender JsonElement

`JsonElement` é a classe abstrata base de toda a hierarquia de elementos JSON. É a porta de entrada para manipular qualquer tipo de dado JSON.

```
Qualquer coisa em JSON é um JsonElement:
- null → JsonNull
- "texto" → JsonPrimitive
- 42 → JsonPrimitive
- true → JsonPrimitive
- { } → JsonObject
- [ ] → JsonArray
```

## 🔍 Descobrindo o Tipo

Antes de usar um `JsonElement`, você precisa saber qual tipo é:

### Método 1: Verificação Booleana

```java
JsonElement element = mapper.parse(JsonSource.of(json));

// Verificar tipo
if (element.isJsonNull()) {
    System.out.println("É null");
} else if (element.isJsonPrimitive()) {
    System.out.println("É primitivo");
} else if (element.isJsonObject()) {
    System.out.println("É objeto");
} else if (element.isJsonArray()) {
    System.out.println("É array");
}
```

### Método 2: Pattern Matching (Java 17+)

```java
String result = switch (element) {
    case JsonNull _ -> "É null";
    case JsonPrimitive _ -> "É primitivo";
    case JsonObject _ -> "É objeto";
    case JsonArray _ -> "É array";
    default -> "Desconhecido";
};
```

## 🔄 Conversão de Tipo

Uma vez que você sabe o tipo, converta para trabalhar com ele:

```java
// Seguro: converte para o tipo específico
JsonObject obj = element.asJsonObject();
JsonArray arr = element.asJsonArray();
JsonPrimitive prim = element.asJsonPrimitive();
JsonNull nullVal = element.asJsonNull();

// Atalhos úteis
String strVal = element.asJsonPrimitive().asString();
int intVal = element.asJsonPrimitive().asInt();
double doubleVal = element.asJsonPrimitive().asDouble();
boolean boolVal = element.asJsonPrimitive().asBoolean();
```

## 📝 Exemplo Completo

```java
public class ElementExplorer {
    public static void main(String[] args) throws Exception {
        JsonMapper mapper = Json.defaultMapper();
        
        String json = """
            {
                "name": "João",
                "age": 30,
                "active": true,
                "email": null,
                "hobbies": ["futebol", "musica"]
            }
            """;
        
        JsonElement root = mapper.parse(JsonSource.of(json));
        
        // É um JsonObject
        if (root.isJsonObject()) {
            JsonObject obj = root.asJsonObject();
            
            // Iterando pelas entradas
            for (String key : obj.keySet()) {
                JsonElement value = obj.get(key);
                printElement(key, value);
            }
        }
    }
    
    static void printElement(String key, JsonElement element) {
        if (element.isJsonNull()) {
            System.out.println(key + " → null");
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.asJsonPrimitive();
            if (prim.isString()) {
                System.out.println(key + " → \"" + prim.asString() + "\"");
            } else if (prim.isNumber()) {
                System.out.println(key + " → " + prim.asNumber());
            } else if (prim.isBoolean()) {
                System.out.println(key + " → " + prim.asBoolean());
            }
        } else if (element.isJsonObject()) {
            System.out.println(key + " → {object}");
        } else if (element.isJsonArray()) {
            System.out.println(key + " → [array]");
        }
    }
}
```

## 🔀 Comparando JsonElements

Você pode comparar elementos:

```java
JsonElement elem1 = new JsonPrimitive("texto");
JsonElement elem2 = new JsonPrimitive("texto");
JsonElement elem3 = new JsonPrimitive("outro");

// Igualdade
System.out.println(elem1.equals(elem2)); // true
System.out.println(elem1.equals(elem3)); // false
```

## 🎛️ Acessando Propriedades

### JsonObject

```java
JsonObject obj = new JsonObject();
obj.addProperty("name", "João");

// Acessar por chave
boolean exists = obj.has("name");          // true
JsonElement value = obj.get("name");       // JsonPrimitive
String name = obj.getAsString("name");     // "João"

// Chaves
Set<String> keys = obj.keySet();           // {"name"}
Collection<JsonElement> values = obj.values();
```

### JsonArray

```java
JsonArray array = new JsonArray();
array.add("item1");
array.add("item2");

// Acessar por índice
JsonElement first = array.get(0);          // JsonPrimitive("item1")
String firstStr = array.get(0).asJsonPrimitive().asString();

// Tamanho
int size = array.size();                   // 2

// Verificar vazio
boolean empty = array.isEmpty();           // false
```

### JsonPrimitive

```java
JsonPrimitive prim = new JsonPrimitive("42");

// Verificar tipo
boolean isString = prim.isString();        // true
boolean isNumber = prim.isNumber();        // false
boolean isBoolean = prim.isBoolean();      // false

// Converter
String str = prim.asString();              // "42"
```

## 🔗 Navegando Estruturas Complexas

```java
String json = """
    {
        "user": {
            "profile": {
                "name": "João",
                "age": 30
            }
        }
    }
    """;

JsonElement root = mapper.parse(JsonSource.of(json));

// Navegar aninhado
JsonObject user = root.asJsonObject().getAsJsonObject("user");
JsonObject profile = user.getAsJsonObject("profile");
String name = profile.getAsString("name");  // "João"

System.out.println("Nome: " + name);
```

## 🛡️ Segurança de Tipo

Sempre trate situações onde a conversão pode falhar:

```java
JsonElement element = mapper.parse(JsonSource.of(json));

try {
    // Pode lançar exceção se não for o tipo esperado
    JsonObject obj = element.asJsonObject();
    String name = obj.getAsString("name");
} catch (ClassCastException e) {
    System.err.println("Elemento não é um JsonObject");
} catch (NullPointerException e) {
    System.err.println("Chave 'name' não existe");
}
```

## 📊 Iterando Estruturas

### Iterar JsonObject

```java
JsonObject obj = root.asJsonObject();

// Por entradas
for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
    String key = entry.getKey();
    JsonElement value = entry.getValue();
    System.out.println(key + ": " + value);
}

// Por chaves
for (String key : obj.keySet()) {
    JsonElement value = obj.get(key);
}

// Por valores
for (JsonElement value : obj.values()) {
    System.out.println(value);
}
```

### Iterar JsonArray

```java
JsonArray array = root.asJsonArray();

// Foreach
for (JsonElement element : array) {
    System.out.println(element);
}

// Por índice
for (int i = 0; i < array.size(); i++) {
    JsonElement element = array.get(i);
    System.out.println(i + ": " + element);
}

// Streams (Java 8+)
array.stream()
    .filter(e -> e.isJsonPrimitive())
    .map(e -> e.asJsonPrimitive().asString())
    .forEach(System.out::println);
```

## 🎯 Casos de Uso Práticos

### Caso 1: Processar API Response

```java
public class ApiResponse {
    public static void handleResponse(String json, JsonMapper mapper) {
        try {
            JsonElement response = mapper.parse(JsonSource.of(json));
            
            if (response.isJsonObject()) {
                JsonObject obj = response.asJsonObject();
                
                if (obj.has("error")) {
                    String error = obj.getAsString("error");
                    System.err.println("Erro: " + error);
                } else if (obj.has("data")) {
                    JsonElement data = obj.get("data");
                    System.out.println("Dados: " + data);
                }
            }
        } catch (JsonParseException e) {
            System.err.println("Response inválido: " + e.getMessage());
        }
    }
}
```

### Caso 2: Processar Array de Objetos

```java
public class DataProcessor {
    public static List<String> extractNames(String json, JsonMapper mapper) {
        List<String> names = new ArrayList<>();
        
        JsonElement root = mapper.parse(JsonSource.of(json));
        
        if (root.isJsonArray()) {
            JsonArray array = root.asJsonArray();
            for (JsonElement elem : array) {
                if (elem.isJsonObject()) {
                    JsonObject obj = elem.asJsonObject();
                    if (obj.has("name")) {
                        names.add(obj.getAsString("name"));
                    }
                }
            }
        }
        
        return names;
    }
}
```

### Caso 3: Validar Estrutura

```java
public class JsonValidator {
    public static boolean isValidUser(String json, JsonMapper mapper) {
        try {
            JsonElement elem = mapper.parse(JsonSource.of(json));
            
            if (!elem.isJsonObject()) return false;
            
            JsonObject obj = elem.asJsonObject();
            
            // Campos obrigatórios
            return obj.has("id") &&
                   obj.has("email") &&
                   obj.has("name");
            
        } catch (JsonParseException e) {
            return false;
        }
    }
}
```

## 🚀 Performance e Boas Práticas

### ✅ Faça

```java
// Cache o mapper
JsonMapper mapper = Json.defaultMapper();

// Verificar tipo antes de converter
if (element.isJsonObject()) {
    JsonObject obj = element.asJsonObject();
}

// Usar try-catch para operações arriscadas
try {
    JsonObject obj = element.asJsonObject();
} catch (ClassCastException e) {
    // Fallback
}
```

### ❌ Evite

```java
// ❌ Não crie novo mapper a cada operação
for (String json : jsonList) {
    JsonMapper newMapper = Json.defaultMapper();  // Ineficiente!
    // ...
}

// ❌ Não suponha tipos sem verificar
JsonObject obj = element.asJsonObject();  // Pode falhar

// ❌ Não ignore exceções
try {
    // código
} catch (JsonParseException e) {
    // Não ignore!
}
```

## 📚 Próximos Passos

Agora que entende JsonElement:

1. **[JsonObject](./05-json-object.md)** - Trabalhar com objetos em detalhes
2. **[JsonArray](./06-json-array.md)** - Trabalhar com arrays em detalhes
3. **[JsonMapper](./07-json-mapper.md)** - Serialização avançada

---

**Anterior:** [3. Conceitos Fundamentais](./03-conceitos-fundamentais.md)  
**Próximo:** [5. JsonObject: Objetos JSON](./05-json-object.md)
