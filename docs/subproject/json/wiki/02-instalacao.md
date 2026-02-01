# 2️⃣ Instalação e Configuração

## 📦 Adicionar à Seu Projeto

### 🔨 Com Gradle

Adicione à sua `build.gradle` ou `build.gradle.kts`:

**Gradle (Groovy)**
```gradle
dependencies {
    implementation 'io.github.nadezhdkov:obsidian-json:0.1.0'
}
```

**Gradle (Kotlin)**
```kotlin
dependencies {
    implementation("io.github.nadezhdkov:obsidian-json:0.1.0")
}
```

### 📗 Com Maven

Adicione ao seu `pom.xml`:

```xml
<dependency>
    <groupId>io.github.nadezhdkov</groupId>
    <artifactId>obsidian-json</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 📥 Imports Essenciais

Os imports mais comuns que você usará:

```java
// API Principal
import io.obsidian.json.api.Json;
import io.obsidian.json.api.JsonMapper;
import io.obsidian.json.api.JsonElement;
import io.obsidian.json.api.JsonObject;
import io.obsidian.json.api.JsonArray;
import io.obsidian.json.api.JsonPrimitive;
import io.obsidian.json.api.JsonNull;

// Tipos e Codecs
import io.obsidian.json.codec.TypeRef;
import io.obsidian.json.codec.JsonCodec;

// I/O
import io.obsidian.json.io.JsonSource;
import io.obsidian.json.io.JsonSink;
import io.obsidian.json.io.JsonFiles;

// Anotações
import io.obsidian.json.annotations.JsonName;
import io.obsidian.json.annotations.JsonIgnore;
import io.obsidian.json.annotations.JsonRequired;
import io.obsidian.json.annotations.JsonDefault;
import io.obsidian.json.annotations.JsonAdapter;

// Configuração
import io.obsidian.json.api.JsonConfig;

// Exceções
import io.obsidian.json.error.JsonException;
import io.obsidian.json.error.JsonParseException;
import io.obsidian.json.error.JsonMappingException;
import io.obsidian.json.error.JsonValidationException;
import io.obsidian.json.error.JsonIoException;
import io.obsidian.json.error.JsonPath;
```

## 🚀 Uso Imediato

### Mapper Padrão

Para a maioria dos casos, use o mapper padrão:

```java
// Obter o mapper padrão
JsonMapper mapper = Json.defaultMapper();

// Pronto para usar!
JsonElement element = mapper.parse(JsonSource.of("{\"name\":\"João\"}"));
```

### Configuração Básica

Se precisar customizar:

```java
JsonMapper mapper = Json.builder()
    .prettyPrint(true)           // JSON formatado
    .serializeNulls(false)       // Omite nulls
    .buildMapper();
```

## ✅ Verificando a Instalação

Crie um programa simples para verificar se tudo está funcionando:

```java


public class TestInstallation {
    public static void main(String[] args) {
        // 1. Criar mapper
        JsonMapper mapper = Json.defaultMapper();

        // 2. Parsear JSON
        JsonElement element = mapper.parse(
                JsonSource.of("{\"message\":\"Obsidian JSON está funcionando!\"}")
        );

        // 3. Acessar valor
        JsonObject obj = element.asJsonObject();
        String message = obj.getAsString("message");

        // 4. Resultado
        System.out.println(message);
        // Saída: Obsidian JSON está funcionando!
    }
}
```

Execute e você deve ver a mensagem impressa.

## 🔧 Configurações Comuns

Aqui estão as configurações mais usadas:

### 1. Pretty Print (Formatação Legível)

```java
JsonMapper mapper = Json.builder()
    .prettyPrint(true)  // Indenta e quebra linhas
    .buildMapper();

// Resultado:
// {
//   "name": "João",
//   "age": 30
// }
```

### 2. Serializar Nulls

```java
// Com nulls
JsonMapper mapper1 = Json.builder()
    .serializeNulls(true)  // Inclui campos null
    .buildMapper();

// { "name": "João", "email": null }

// Sem nulls
JsonMapper mapper2 = Json.builder()
    .serializeNulls(false) // Omite campos null
    .buildMapper();

// { "name": "João" }
```

### 3. Modo Lenient (JSON não-estrito)

```java
JsonMapper mapper = Json.builder()
    .lenient(true)  // Aceita JSON malformado
    .buildMapper();

// Aceita strings sem aspas, comentários, etc.
```

### 4. Formato de Data

```java
JsonMapper mapper = Json.builder()
    .dateFormat("yyyy-MM-dd")  // "2024-01-15"
    // ou
    .dateFormat("dd/MM/yyyy")  // "15/01/2024"
    // ou
    .dateFormat("ISO")         // ISO 8601
    .buildMapper();
```

### 5. Escapar HTML

```java
JsonMapper mapper = Json.builder()
    .htmlEscaping(true)   // Escapa caracteres HTML
    .buildMapper();

// "<script>" vira "\u003cscript\u003e"
```

### 6. Habilitar Anotações

```java
JsonMapper mapper = Json.builder()
    .enableAnnotations(true)  // Usa @Json* anotações
    .buildMapper();
```

## 🎯 Padrões de Inicialização Recomendados

### Padrão 1: Mapper Singleton (Recomendado)

Para melhor performance, crie uma única instância do mapper:

```java
public class JsonMapperHolder {
    private static final JsonMapper MAPPER = Json.builder()
        .prettyPrint(true)
        .enableAnnotations(true)
        .buildMapper();
    
    public static JsonMapper getMapper() {
        return MAPPER;
    }
}

// Usar
JsonMapper mapper = JsonMapperHolder.getMapper();
```

### Padrão 2: Factory Method

```java
public class AppConfig {
    
    public static JsonMapper createDefaultMapper() {
        return Json.builder()
            .prettyPrint(false)        // Produção
            .serializeNulls(false)
            .enableAnnotations(true)
            .buildMapper();
    }
    
    public static JsonMapper createPrettyMapper() {
        return Json.builder()
            .prettyPrint(true)         // Debug
            .serializeNulls(true)
            .enableAnnotations(true)
            .buildMapper();
    }
}
```

### Padrão 3: Spring Bean

Se você usa Spring:

```java
@Configuration
public class JsonConfig {
    
    @Bean
    public JsonMapper jsonMapper() {
        return Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .buildMapper();
    }
}

// Injetar em qualquer lugar
@Service
public class UserService {
    @Autowired
    private JsonMapper mapper;
    
    public void processUser(String json) {
        User user = mapper.decode(
            JsonSource.of(json),
            TypeRef.of(User.class)
        );
        // ...
    }
}
```

## 📋 Checklist de Configuração

- ✅ Adicionou a dependência ao seu build file (gradle/maven)
- ✅ Executou `gradle build` ou `mvn clean install`
- ✅ Importou as classes necessárias
- ✅ Criou uma instância de `JsonMapper`
- ✅ Testou um exemplo simples
- ✅ Escolheu um padrão de inicialização

## 🐛 Troubleshooting

### Erro: "Symbol not found: Json"

**Solução:** Verifique se você importou corretamente:
```java
import io.obsidian.json.api.Json;  // ✅ Correto
import io.json.api.Json;           // ❌ Errado
```

### Erro: "Cannot resolve symbol JsonMapper"

**Solução:** Certifique-se de que a dependência foi adicionada ao `build.gradle` ou `pom.xml` e recarregue seu projeto.

### ClassNotFoundException em Runtime

**Solução:** Algumas IDEs não sincronizam as dependências automaticamente. Tente:
- **IntelliJ:** File → Invalidate Caches → Restart
- **Eclipse:** Project → Clean
- **VS Code:** Recarregue a janela

## 📚 Próximos Passos

Agora que você tem tudo configurado:

1. **[Conceitos Fundamentais](./03-conceitos-fundamentais.md)** - Entenda como tudo funciona
2. **[JsonElement](./04-json-element.md)** - Comece a trabalhar com JSON
3. **[JsonObject](./05-json-object.md)** - Crie objetos JSON

---

**Anterior:** [1. Introdução](./01-introducao.md)  
**Próximo:** [3. Conceitos Fundamentais](./03-conceitos-fundamentais.md)
