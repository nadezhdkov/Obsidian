# 3️⃣ Conceitos Fundamentais

## 🏗️ Entendendo a Estrutura Base

Antes de começar a trabalhar com a API, é importante entender os conceitos fundamentais que permeiam todo o design.

## 📊 JsonElement: A Hierarquia

`JsonElement` é a classe abstrata base que representa **qualquer elemento JSON**. Tudo em JSON é um `JsonElement`:

```
      JsonElement (abstract)
           ↙ ↓ ↘
     null  Object  Array
      ↓      ↓       ↓
   JsonNull JsonObject JsonArray
        ↓
      Primitivos
        ↓
   JsonPrimitive
```

### Visualizando a Hierarquia

```java
JsonElement {
    JsonNull             → null
    JsonPrimitive        → "string", 42, 3.14, true, false
    JsonObject           → { "chave": valor }
    JsonArray            → [ elemento1, elemento2, ... ]
}
```

### Exemplo Prático

```java
// JsonElement é a base de tudo
JsonElement null_element = new JsonNull();
JsonElement primitive_element = new JsonPrimitive("texto");
JsonElement object_element = new JsonObject();
JsonElement array_element = new JsonArray();

// Você pode tratar todos como JsonElement
List<JsonElement> elements = Arrays.asList(
    null_element,
    primitive_element,
    object_element,
    array_element
);

// Mas para usar tipo-específico, converte
JsonObject obj = object_element.asJsonObject();
JsonArray arr = array_element.asJsonArray();
```

## 🔄 Conversão Entre Tipos

Você pode converter um `JsonElement` para seus subtipos:

```java
JsonElement element = mapper.parse(JsonSource.of(json));

// Converter para o tipo específico
if (element.isJsonObject()) {
    JsonObject obj = element.asJsonObject();
}

if (element.isJsonArray()) {
    JsonArray arr = element.asJsonArray();
}

if (element.isJsonPrimitive()) {
    JsonPrimitive prim = element.asJsonPrimitive();
}

if (element.isJsonNull()) {
    JsonNull null_elem = element.asJsonNull();
}
```

## 🎯 JsonObject: Chave-Valor

`JsonObject` representa um objeto JSON com pares chave-valor:

```java
// JSON: { "name": "João", "age": 30 }

JsonObject obj = new JsonObject();
obj.addProperty("name", "João");      // String
obj.addProperty("age", 30);           // int
obj.addProperty("active", true);      // boolean
obj.addProperty("salary", 5000.50);   // double

// Acessar valores
String name = obj.getAsString("name");
int age = obj.getAsInt("age");
boolean active = obj.getAsBoolean("active");
double salary = obj.getAsDouble("salary");
```

## 📋 JsonArray: Listas

`JsonArray` representa um array JSON:

```java
// JSON: [ "João", 30, true ]

JsonArray array = new JsonArray();
array.add("João");    // String
array.add(30);        // int
array.add(true);      // boolean

// Acessar por índice
JsonElement first = array.get(0);  // JsonPrimitive("João")
JsonElement second = array.get(1); // JsonPrimitive(30)

// Tamanho
int size = array.size();  // 3

// Iterar
for (JsonElement elem : array) {
    System.out.println(elem);
}
```

## 🔤 JsonPrimitive: Valores Básicos

`JsonPrimitive` representa valores primitivos (string, número, boolean):

```java
// Criar primitivos
JsonPrimitive string = new JsonPrimitive("texto");
JsonPrimitive number = new JsonPrimitive(42);
JsonPrimitive decimal = new JsonPrimitive(3.14);
JsonPrimitive bool = new JsonPrimitive(true);

// Converter para tipos Java
String str = string.asString();
int num = number.asInt();
double dec = decimal.asDouble();
boolean b = bool.asBoolean();
```

## ⏰ JsonNull: Null

`JsonNull` representa o valor `null` em JSON:

```java
// Criar null
JsonElement nullElem = JsonNull.INSTANCE;

// Ou
JsonElement nullElem2 = new JsonNull();

// Verificar se é null
if (element.isJsonNull()) {
    System.out.println("É nulo");
}

// Em um objeto
JsonObject obj = new JsonObject();
obj.add("email", JsonNull.INSTANCE);
// JSON: { "email": null }
```

## 🚀 JsonMapper: Conversão de Dados

`JsonMapper` é responsável por converter entre diferentes formatos:

```
String/File ←→ JsonElement ←→ Objeto Java
                   (Parser)   (Codec)
```

### As 4 Operações Principais

```java
JsonMapper mapper = Json.defaultMapper();

// 1. PARSE: String/File → JsonElement
JsonElement element = mapper.parse(JsonSource.of(jsonString));

// 2. STRINGIFY: JsonElement → String
String json = mapper.stringify(element);

// 3. ENCODE: Objeto Java → JsonElement
JsonElement encoded = mapper.encode(myObject);

// 4. DECODE: String/File → Objeto Java
MyClass decoded = mapper.decode(
    JsonSource.of(jsonString),
    TypeRef.of(MyClass.class)
);
```

## 📝 JsonSource & JsonSink

### JsonSource: De Onde Vem os Dados

```java
// De string
JsonSource s1 = JsonSource.of("{\"name\":\"João\"}");

// De arquivo
JsonSource s2 = JsonSource.of(new File("data.json"));

// De Path
JsonSource s3 = JsonSource.of(Paths.get("data.json"));

// De InputStream
JsonSource s4 = JsonSource.of(inputStream);

// De Reader
JsonSource s5 = JsonSource.of(reader);
```

### JsonSink: Para Onde Vão os Dados

```java
// Para string
JsonSink sink1 = JsonSink.ofString();
String result = sink1.getString();

// Para arquivo
JsonSink sink2 = JsonSink.ofFile(new File("output.json"));

// Para Path
JsonSink sink3 = JsonSink.ofPath(Paths.get("output.json"));

// Para OutputStream
JsonSink sink4 = JsonSink.ofStream(outputStream);

// Para Writer
JsonSink sink5 = JsonSink.ofWriter(writer);
```

## 🔐 TypeRef: Tipos Genéricos

Como representar tipos genéricos de forma type-safe:

```java
// Tipo simples - sem problema
User user = mapper.decode(source, TypeRef.of(User.class));

// Tipo genérico - precisa de TypeRef
List<User> users = mapper.decode(
    source,
    new TypeRef<List<User>>() {}  // TypeRef anônimo
);

// Ou usar factory methods
TypeRef<List<String>> listRef = TypeRef.listOf(String.class);
TypeRef<Set<Integer>> setRef = TypeRef.setOf(Integer.class);
TypeRef<Map<String, User>> mapRef = TypeRef.mapOf(String.class, User.class);
```

## 🎭 Anotações: Controlando Comportamento

Anotações permitem customizar como um objeto é serializado:

```java
public class User {
    @JsonRequired              // Obrigatório
    private String id;
    
    @JsonName("user_name")     // Nome diferente
    private String username;
    
    @JsonDefault("guest")      // Valor padrão
    private String role;
    
    @JsonIgnore                // Não serializa
    private String password;
    
    @JsonAdapter(UuidCodec.class)  // Codec custom
    private UUID uuid;
}
```

## 📦 JsonFiles: Operações de Arquivo

Atalho conveniente para ler/escrever arquivos JSON:

```java
import io.obsidian.json.io.JsonFiles;
import java.nio.file.Paths;

// Ler de arquivo
User user = JsonFiles.read(
    Paths.get("user.json"),
    TypeRef.of(User.class)
);

// Escrever para arquivo
JsonFiles.write(
    Paths.get("user.json"),
    user
);

// Com mapper customizado
JsonMapper mapper = Json.builder().prettyPrint(true).build().buildMapper();
List<User> users = JsonFiles.read(
    Paths.get("users.json"),
    TypeRef.listOf(User.class),
    mapper
);
```

## 🚨 Exceções: Tratamento de Erros

Todas as exceções estendem `JsonException`:

```
JsonException (base)
├── JsonParseException      → JSON malformado
├── JsonMappingException    → Tipo incompatível
├── JsonValidationException → Validação falhou
└── JsonIoException         → Erro de I/O
```

### Tratando Exceções

```java
try {
    User user = mapper.decode(source, TypeRef.of(User.class));
} catch (JsonParseException e) {
    // JSON está mal-formado
    System.err.println("Erro de parse: " + e.getMessage());
    System.err.println("Caminho: " + e.getPath());
} catch (JsonMappingException e) {
    // Tipo não corresponde
    System.err.println("Erro de mapping: " + e.getMessage());
} catch (JsonValidationException e) {
    // @JsonRequired não foi encontrado
    System.err.println("Validação falhou: " + e.getMessage());
} catch (JsonException e) {
    // Qualquer outra exceção JSON
    System.err.println("Erro JSON: " + e.getMessage());
}
```

## 🔀 Fluxo de Dados Típico

```
┌─────────────────────────────┐
│  Dados Externos             │
│  (JSON String/File)         │
└────────────┬────────────────┘
             ↓
     ┌──────────────┐
     │  JsonSource  │
     └────────┬─────┘
              ↓
      ┌──────────────┐
      │  Mapper      │
      │  - parse()   │
      │  - decode()  │
      └────────┬─────┘
               ↓
       ┌──────────────┐
       │  JsonElement │
       │  ou Objeto   │
       └────────┬─────┘
                ↓
        ┌──────────────┐
        │ Seu Código   │
        │ Processando  │
        └────────┬─────┘
                 ↓
         ┌──────────────┐
         │  Mapper      │
         │  - encode()  │
         │  - stringify()
         └────────┬─────┘
                  ↓
          ┌──────────────┐
          │  JsonSink    │
          └────────┬─────┘
                   ↓
        ┌──────────────────┐
        │  Dados Salvos    │
        │  (JSON/File)     │
        └──────────────────┘
```

## 💡 Dicas Importantes

1. **Sempre use `TypeRef` para generics** - Evita type erasure issues
2. **Entenda o JsonElement hierarchy** - Tudo é JsonElement
3. **Use anotações com sabedoria** - @JsonRequired, @JsonName, etc
4. **Crie um mapper singleton** - Melhor performance
5. **Trate exceções apropriadamente** - Cada tipo tem significado diferente
6. **Use JsonFiles para operações de arquivo** - Mais seguro

## 📚 Próximos Passos

Agora que você entende os conceitos:

1. **[JsonElement](./04-json-element.md)** - Trabalhar com elementos
2. **[JsonObject](./05-json-object.md)** - Criar e manipular objetos
3. **[JsonArray](./06-json-array.md)** - Trabalhar com arrays

---

**Anterior:** [2. Instalação](./02-instalacao.md)  
**Próximo:** [4. JsonElement: Trabalhando com Elementos](./04-json-element.md)
