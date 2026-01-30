# 📘 Obsidian JSON Module
## Architecture & Design Document

---

## 1. Visão Geral

O módulo **Obsidian JSON** tem como objetivo fornecer uma **API própria, estável e extensível** para leitura, escrita, serialização e desserialização de arquivos JSON, utilizando o **Google Gson como engine interno**, porém **sem expor suas classes ou conceitos na API pública**.

O sistema foi projetado para:

- desacoplar o core do Obsidian de bibliotecas externas
- fornecer uma experiência fluente e previsível
- permitir evolução futura (ex: troca do Gson por outro engine)
- oferecer suporte opcional a annotations
- centralizar configuração via builder imutável

---

## 2. Princípios Arquiteturais

### 2.1 Princípios adotados

- API-first design
- Separation of concerns
- Engine encapsulation
- Imutabilidade
- Fail-fast configurável
- Baixo acoplamento
- Alta coesão
- Extensibilidade controlada

---

## 3. Organização Geral do Projeto

```text
obsidian-json
└── br.com.obsidian.json
    ├── api
    │   ├── Json.java
    │   ├── JsonMapper.java
    │   ├── JsonConfig.java
    │   ├── JsonElement.java
    │   ├── JsonObject.java
    │   ├── JsonArray.java
    │   ├── JsonPrimitive.java
    │   ├── JsonNull.java
    │   └── codec
    │       ├── JsonCodec.java
    │       └── TypeRef.java
    │
    ├── annotations
    │   ├── JsonName.java
    │   ├── JsonIgnore.java
    │   ├── JsonAdapter.java
    │   ├── JsonRequired.java
    │   └── JsonDefault.java
    │
    ├── io
    │   ├── JsonSource.java
    │   ├── JsonSink.java
    │   ├── JsonFiles.java
    │   └── JsonCharset.java
    │
    ├── error
    │   ├── JsonException.java
    │   ├── JsonParseException.java
    │   ├── JsonMappingException.java
    │   ├── JsonValidationException.java
    │   ├── JsonIoException.java
    │   └── JsonPath.java
    │
    ├── internal
    │   └── gson
    │       ├── GsonEngine.java
    │       ├── GsonMapper.java
    │       ├── GsonElementBridge.java
    │       ├── GsonAnnotationProcessor.java
    │       └── adapter
    │           └── GsonCodecAdapter.java
    │
    └── util
        └── JsonPrettyPrinter.java
```

---

## 4. API Pública

### 4.1 Json (Facade)

Classe de entrada do sistema.

Responsável por:

- fornecer mapper padrão
- aplicar configurações
- esconder engine interno

```java
JsonMapper mapper = Json.defaultMapper();

JsonMapper custom = Json.builder()
        .prettyPrint(true)
        .failOnUnknownFields(false)
        .enableAnnotations(true)
        .build();
```

---

### 4.2 JsonMapper

Contrato principal do sistema.

```java
public interface JsonMapper {

    JsonElement parse(JsonSource source);

    <T> T decode(JsonSource source, TypeRef<T> type);

    <T> T decode(JsonElement element, TypeRef<T> type);

    JsonElement encode(Object value);

    String stringify(JsonElement element);
}
```

---

### 4.3 JsonConfig + Builder

Configuração imutável.

```java
JsonConfig config = JsonConfig.builder()
        .prettyPrint(true)
        .serializeNulls(false)
        .lenient(true)
        .dateFormat("yyyy-MM-dd")
        .enableAnnotations(true)
        .annotationsMode(AnnotationsMode.OBSIDIAN_ONLY)
        .build();
```

Após criado, o config não pode ser modificado.

---

## 5. Sistema de Annotations

### Filosofia

Annotations são uma camada de conveniência, não o núcleo do sistema.

Elas:

- não substituem codecs
- não criam lógica complexa de serialização
- apenas influenciam o mapeamento

Podem ser completamente desativadas via configuração.

---

## 6. Annotations Disponíveis

### @JsonName

Define o nome do campo no JSON.

```java
@JsonName("user_name")
private String username;
```

---

### @JsonIgnore

Ignora o campo durante encode/decode.

---

### @JsonAdapter

Define um codec específico.

```java
@JsonAdapter(UuidCodec.class)
private UUID id;
```

---

### @JsonRequired

Campo obrigatório durante decode.

---

### @JsonDefault

Define valor padrão caso ausente ou null.

```java
@JsonDefault("localhost")
private String host;
```

---

## 7. JsonCodec

```java
public interface JsonCodec<T> {

    JsonElement encode(T value);

    T decode(JsonElement element);
}
```

---

## 8. TypeRef

Utilitário para tipos genéricos.

```java
TypeRef<List<User>> users = TypeRef.listOf(User.class);
```

---

## 9. Sistema de IO

Separado do parsing.

```java
User user = JsonFiles.read(path, TypeRef.of(User.class));
JsonFiles.write(path, user);
```

---

## 10. Engine Interno (Gson)

Localizado em `internal.gson`.

Responsável por:

- criar GsonBuilder
- aplicar JsonConfig
- processar annotations
- converter JsonCodec em TypeAdapter

Nenhuma classe pública depende diretamente do Gson.

---

## 11. Fluxo de Decode

```
JsonSource
   ↓
Parser
   ↓
JsonElement
   ↓
Annotation processor
   ↓
Validation (@JsonRequired)
   ↓
Defaults (@JsonDefault)
   ↓
Mapping
   ↓
Objeto final
```

---

## 12. Tratamento de Erros

Hierarquia:

```
JsonException
 ├── JsonParseException
 ├── JsonMappingException
 ├── JsonValidationException
 └── JsonIoException
```

Cada exceção contém:

- JsonPath
- origem do erro
- causa raiz
- contexto

---

## 13. Benefícios

- API limpa
- Engine desacoplado
- Evolução segura
- Testável
- Ideal para arquivos de configuração
- Excelente DX

---

## 14. Evoluções Futuras

- suporte a múltiplos engines
- hot reload de arquivos
- schema validation
- merge de configs
- observadores
- versionamento

---

## 15. Conclusão

O Obsidian JSON não é apenas um wrapper do Gson.

Ele é um subsistema de serialização independente, projetado para crescer junto com o core do Obsidian, mantendo estabilidade, clareza e liberdade arquitetural.

