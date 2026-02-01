# 1️⃣ Introdução à API JSON do Obsidian

## 🎯 O que é?

A API JSON do Obsidian é uma biblioteca moderna e leve para manipulação de JSON em Java. Ela fornece:

- **Interface limpa e intuitiva** para trabalhar com JSON
- **Abstração completa** do engine interno (Google Gson)
- **Type-safe operations** com suporte a generics
- **Anotações poderosas** para customizar comportamento
- **Extensibilidade** através de codecs customizados
- **Excelente tratamento de erros** com rastreamento de caminho

## 🚀 Por Que Usar?

### ❌ Sem a API JSON do Obsidian

```java
// Usando Gson diretamente - acoplado ao Gson
Gson gson = new Gson();
String json = "{\"name\":\"João\",\"age\":30}";
JsonObject obj = gson.fromJson(json, JsonObject.class);
String name = obj.get("name").getAsString(); // Detalhado e verboso
```

### ✅ Com a API JSON do Obsidian

```java
// Usando Obsidian - desacoplado, limpo
JsonMapper mapper = Json.defaultMapper();
JsonObject obj = mapper.parse(JsonSource.of(json)).asJsonObject();
String name = obj.getAsString("name"); // Simples e elegante
```

## 📦 Componentes Principais

### JsonElement Hierarchy

Todo elemento JSON é representado por `JsonElement`:

```
JsonElement (abstract base)
├── JsonNull        → null
├── JsonPrimitive   → string, number, boolean
├── JsonArray       → [ ]
└── JsonObject      → { }
```

### JsonMapper

Responsável pela serialização e desserialização:

```java
JsonMapper mapper = Json.defaultMapper();

// Parse - string → JsonElement
JsonElement element = mapper.parse(JsonSource.of(jsonString));

// Decode - JSON → Object
MyClass obj = mapper.decode(JsonSource.of(jsonString), TypeRef.of(MyClass.class));

// Encode - Object → JSON
JsonElement json = mapper.encode(myObject);

// Stringify - JsonElement → String
String result = mapper.stringify(element);
```

### JsonConfig Builder

Personaliza o comportamento do mapper:

```java
JsonMapper customMapper = Json.builder()
    .prettyPrint(true)           // Formatação legível
    .serializeNulls(false)       // Ignora nulls
    .lenient(true)               // JSON não-estrito
    .dateFormat("yyyy-MM-dd")    // Formato de data
    .enableAnnotations(true)     // Usa anotações
    .buildMapper();
```

## 💡 Conceitos-Chave

### 1. JsonSource

Representa a fonte de dados JSON (string, arquivo, stream):

```java
JsonSource source1 = JsonSource.of(jsonString);
JsonSource source2 = JsonSource.of(file);
JsonSource source3 = JsonSource.of(inputStream);
```

### 2. JsonSink

Representa o destino para JSON (string, arquivo, stream):

```java
JsonSink sink1 = JsonSink.ofString();
JsonSink sink2 = JsonSink.ofFile(file);
JsonSink sink3 = JsonSink.ofStream(outputStream);
```

### 3. TypeRef

Para trabalhar com tipos genéricos de forma type-safe:

```java
// Simples
TypeRef<User> userType = TypeRef.of(User.class);

// Genérico
TypeRef<List<User>> listType = new TypeRef<List<User>>() {};

// Conveniência
TypeRef<List<String>> strings = TypeRef.listOf(String.class);
TypeRef<Map<String, User>> mapType = TypeRef.mapOf(String.class, User.class);
```

### 4. Anotações

Controlam o comportamento de serialização:

```java
public class User {
    @JsonName("user_id")           // Nome diferente em JSON
    private String id;
    
    @JsonRequired                  // Obrigatório ao desserializar
    private String email;
    
    @JsonDefault("guest")          // Valor padrão
    private String username;
    
    @JsonIgnore                    // Não serializa
    private String password;
    
    @JsonAdapter(UuidCodec.class)  // Codec customizado
    private UUID uuid;
}
```

## 🔄 Fluxo Típico

```
Dados Externos (JSON String/File)
         ↓
    JsonSource
         ↓
   JsonMapper.parse() / decode()
         ↓
   JsonElement ou Objeto
         ↓
  Processamento e Manipulação
         ↓
   JsonMapper.encode() / stringify()
         ↓
    JsonSink
         ↓
Dados Salvos (String/File/Stream)
```

## 📊 Arquitetura em Camadas

```
┌─────────────────────────────────────────┐
│    Seu Código da Aplicação              │
│  (Usa JsonMapper, JsonElement, etc)     │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   API Pública (o que você vê)           │
│  Json, JsonMapper, JsonElement,         │
│  Anotações, TypeRef, JsonFiles          │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Implementação Interna (Gson)          │
│  GsonMapper, GsonEngine, etc             │
│  Você NUNCA trabalha aqui diretamente    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│    Google Gson (engine)                 │
│  Completamente oculto                   │
└─────────────────────────────────────────┘
```

## 🎯 Quando Usar

✅ **Use a API JSON do Obsidian quando:**
- Precisa trabalhar com JSON em Java
- Quer uma API limpa e type-safe
- Precisa de anotações para customização
- Quer desacoplar do Gson (ou outro engine)
- Trabalha com tipos genéricos frequentemente
- Precisa de excelente tratamento de erros

## 🏗️ Estrutura da Biblioteca

```
io.obsidian.json
├── api/                    # Interface pública
│   ├── Json                # Ponto de entrada
│   ├── JsonMapper          # Serialização/Desserialização
│   ├── JsonElement         # Hierarquia de elementos
│   ├── JsonObject          # Objeto JSON
│   ├── JsonArray           # Array JSON
│   ├── JsonPrimitive       # Primitivos
│   ├── JsonNull            # Null
│   └── JsonConfig          # Configuração
├── annotations/            # Anotações @Json*
│   ├── @JsonName           # Nome customizado
│   ├── @JsonIgnore         # Ignora campo
│   ├── @JsonRequired       # Campo obrigatório
│   ├── @JsonDefault        # Valor padrão
│   └── @JsonAdapter        # Codec customizado
├── codec/                  # Extensibilidade
│   ├── JsonCodec<T>        # Interface para codecs
│   └── TypeRef             # Tipos genéricos
├── io/                     # Entrada/Saída
│   ├── JsonSource          # Fonte de dados
│   ├── JsonSink            # Destino de dados
│   └── JsonFiles           # Operações de arquivo
├── error/                  # Exceções
│   ├── JsonException       # Base
│   ├── JsonParseException  # Parse error
│   ├── JsonMappingException# Mapping error
│   ├── JsonValidationException# Validation error
│   └── JsonIoException     # I/O error
└── internal/               # Implementação (privado)
    └── gson/               # Tudo relativo a Gson
```

## 📌 Próximos Passos

1. **Instalação** - [Veja como instalar](./02-instalacao.md)
2. **Conceitos Fundamentais** - [Entenda os fundamentos](./03-conceitos-fundamentais.md)
3. **Primeiros Passos Práticos** - [JsonElement básico](./04-json-element.md)

---

**Próximo:** [2. Instalação e Configuração](./02-instalacao.md)
