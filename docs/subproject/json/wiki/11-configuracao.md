# 1️⃣1️⃣ Configuração: JsonConfig Builder

## 🎯 O que é JsonConfig?

`JsonConfig` é o builder que permite customizar completamente o comportamento do `JsonMapper`. Controla desde formatação até comportamento de validação.

## 🏗️ Usando o Builder

```java
// Padrão: sem configurações
JsonMapper defaultMapper = Json.defaultMapper();

// Customizado: com configurações
JsonMapper customMapper = Json.builder()
    .option1(value1)
    .option2(value2)
    .option3(value3)
    .build()
    .buildMapper();
```

## 📋 Opções Disponíveis

### 1. Pretty Print (Formatação)

```java
JsonMapper prettyMapper = Json.builder()
    .prettyPrint(true)  // ← Habilita formatação
    .build()
    .buildMapper();

// Sem prettyPrint:
// {\"name\":\"João\",\"age\":30}

// Com prettyPrint:
// {
//   \"name\": \"João\",
//   \"age\": 30
// }
```

**Use quando:**
- ✅ Debug e logging
- ✅ APIs que precisam ser legíveis para humanos
- ❌ Produção (aumenta tamanho)

### 2. Serialize Nulls

```java
// Incluir valores null
JsonMapper withNulls = Json.builder()
    .serializeNulls(true)
    .build()
    .buildMapper();
// {\"name\":\"João\",\"email\":null}

// Omitir valores null
JsonMapper withoutNulls = Json.builder()
    .serializeNulls(false)  // padrão
    .build()
    .buildMapper();
// {\"name\":\"João\"}
```

**Use quando:**
- ✅ `true`: Precisar distinguir entre "ausente" e "null"
- ✅ `false`: Reduzir tamanho do JSON

### 3. Lenient Mode

```java
JsonMapper lenient = Json.builder()
    .lenient(true)
    .build()
    .buildMapper();

// Aceita JSON não-conforme:
// - Strings sem aspas: {name: \"João\"}
// - Números inválidos: {age: NaN}
// - Comentários: {name: \"João\" /* comentário */}
```

**Use quando:**
- ✅ Parsing de JSON legado ou malformado
- ❌ Validação strict

### 4. Date Format

```java
// ISO 8601 (padrão)
JsonMapper iso = Json.builder()
    .dateFormat(\"ISO\")
    .build()
    .buildMapper();
// 2024-01-15T10:30:00Z

// Customizado
JsonMapper custom = Json.builder()
    .dateFormat(\"yyyy-MM-dd\")  // Padrão específico
    .build()
    .buildMapper();
// 2024-01-15

JsonMapper br = Json.builder()
    .dateFormat(\"dd/MM/yyyy\")  // Formato brasileiro
    .build()
    .buildMapper();
// 15/01/2024
```

**Use quando:**
- ✅ Trabalhar com diferentes locales
- ✅ Integração com APIs legadas

### 5. HTML Escaping

```java
// Sem escaping
JsonMapper noEscape = Json.builder()
    .htmlEscaping(false)
    .build()
    .buildMapper();
// {\"html\":\"<script>alert('xss')</script>\"}

// Com escaping
JsonMapper escaped = Json.builder()
    .htmlEscaping(true)
    .build()
    .buildMapper();
// {\"html\":\"\\u003cscript\\u003ealert('xss')\\u003c/script\\u003e\"}
```

**Use quando:**
- ✅ `true`: JSON será inserido em HTML
- ✅ `false`: Segurança via outra camada

### 6. Enable Annotations

```java
JsonMapper withAnnotations = Json.builder()
    .enableAnnotations(true)
    .build()
    .buildMapper();

// Agora pode usar @JsonName, @JsonIgnore, etc
```

**Sempre ative se usar anotações @Json*!**

## 🔧 Combinações Práticas

### Padrão: Produção

```java
JsonMapper production = Json.builder()
    .prettyPrint(false)       // Compacto
    .serializeNulls(false)    // Menos dados
    .lenient(false)           // Strict
    .enableAnnotations(true)  // Usar anotações
    .htmlEscaping(true)       // Seguro
    .build()
    .buildMapper();
```

### Padrão: Debug

```java
JsonMapper debug = Json.builder()
    .prettyPrint(true)        // Legível
    .serializeNulls(true)     // Ver tudo
    .lenient(true)            // Flexível
    .enableAnnotations(true)  // Usar anotações
    .htmlEscaping(false)      // Ver conteúdo real
    .build()
    .buildMapper();
```

### Padrão: API REST

```java
JsonMapper api = Json.builder()
    .prettyPrint(false)       // Compacto
    .serializeNulls(true)     // Explícito
    .enableAnnotations(true)  // Customizar fields
    .dateFormat(\"ISO\")      // Padrão ISO
    .htmlEscaping(true)       // Seguro
    .build()
    .buildMapper();
```

### Padrão: Config File

```java
JsonMapper configMapper = Json.builder()
    .prettyPrint(true)        // Legível para humanos
    .serializeNulls(true)     // Documentar defaults
    .enableAnnotations(true)  // Customizar
    .lenient(true)            // Flexível com espaços/comentários
    .build()
    .buildMapper();
```

## 🌍 Localizando por Locale

```java
import java.text.SimpleDateFormat;
import java.util.Locale;

// Para diferentes regiões
JsonMapper[] mappers = {
    // Estados Unidos
    Json.builder()
        .dateFormat(\"MM/dd/yyyy\")
        .build()
        .buildMapper(),
    
    // Brasil
    Json.builder()
        .dateFormat(\"dd/MM/yyyy\")
        .build()
        .buildMapper(),
    
    // Europa
    Json.builder()
        .dateFormat(\"dd.MM.yyyy\")
        .build()
        .buildMapper()
};
```

## 💡 Padrões de Configuração

### Padrão 1: Singleton com Lazy Initialization

```java
public class JsonMapperProvider {
    
    private static volatile JsonMapper instance;
    
    public static JsonMapper getInstance() {
        if (instance == null) {
            synchronized (JsonMapperProvider.class) {
                if (instance == null) {
                    instance = Json.builder()
                        .prettyPrint(false)
                        .serializeNulls(false)
                        .enableAnnotations(true)
                        .build()
                        .buildMapper();
                }
            }
        }
        return instance;
    }
}

// Usar
JsonMapper mapper = JsonMapperProvider.getInstance();
```

### Padrão 2: Factory por Perfil

```java
public class JsonMapperFactory {
    
    public enum Profile {
        DEVELOPMENT, STAGING, PRODUCTION
    }
    
    public static JsonMapper createMapper(Profile profile) {
        switch (profile) {
            case DEVELOPMENT:
                return createDevelopmentMapper();
            case STAGING:
                return createStagingMapper();
            case PRODUCTION:
                return createProductionMapper();
            default:
                throw new IllegalArgumentException(\"Unknown profile: \" + profile);
        }
    }
    
    private static JsonMapper createDevelopmentMapper() {
        return Json.builder()
            .prettyPrint(true)
            .serializeNulls(true)
            .lenient(true)
            .enableAnnotations(true)
            .htmlEscaping(false)
            .build()
            .buildMapper();
    }
    
    private static JsonMapper createStagingMapper() {
        return Json.builder()
            .prettyPrint(false)
            .serializeNulls(false)
            .lenient(false)
            .enableAnnotations(true)
            .htmlEscaping(true)
            .build()
            .buildMapper();
    }
    
    private static JsonMapper createProductionMapper() {
        return Json.builder()
            .prettyPrint(false)
            .serializeNulls(false)
            .lenient(false)
            .enableAnnotations(true)
            .htmlEscaping(true)
            .build()
            .buildMapper();
    }
}

// Usar
JsonMapper devMapper = JsonMapperFactory.createMapper(Profile.DEVELOPMENT);
JsonMapper prodMapper = JsonMapperFactory.createMapper(Profile.PRODUCTION);
```

### Padrão 3: Spring Configuration

```java
@Configuration
public class JsonConfiguration {
    
    @Bean
    @Profile(\"dev\")
    public JsonMapper devMapper() {
        return Json.builder()
            .prettyPrint(true)
            .serializeNulls(true)
            .lenient(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    @Bean
    @Profile(\"prod\")
    public JsonMapper prodMapper() {
        return Json.builder()
            .prettyPrint(false)
            .serializeNulls(false)
            .lenient(false)
            .enableAnnotations(true)
            .htmlEscaping(true)
            .build()
            .buildMapper();
    }
    
    @Bean
    public JsonMapper jsonMapper(JsonMapper prodMapper) {
        return prodMapper;  // Padrão
    }
}
```

### Padrão 4: Configuração de Arquivo

```java
public class ConfigurableJsonMapperFactory {
    
    public static JsonMapper createFromProperties(Properties props) {
        return Json.builder()
            .prettyPrint(
                Boolean.parseBoolean(
                    props.getProperty(\"json.prettyPrint\", \"false\")
                )
            )
            .serializeNulls(
                Boolean.parseBoolean(
                    props.getProperty(\"json.serializeNulls\", \"false\")
                )
            )
            .lenient(
                Boolean.parseBoolean(
                    props.getProperty(\"json.lenient\", \"false\")
                )
            )
            .enableAnnotations(
                Boolean.parseBoolean(
                    props.getProperty(\"json.annotations\", \"true\")
                )
            )
            .dateFormat(props.getProperty(\"json.dateFormat\", \"ISO\"))
            .build()
            .buildMapper();
    }
}

// application.properties
// json.prettyPrint=false
// json.serializeNulls=false
// json.annotations=true
// json.dateFormat=yyyy-MM-dd

// Usar
Properties props = new Properties();
props.load(new FileInputStream(\"application.properties\"));
JsonMapper mapper = ConfigurableJsonMapperFactory.createFromProperties(props);
```

## 🎯 Exemplo Completo

```java
public class ConfigurationDemo {
    
    public static void main(String[] args) {
        // JSON de exemplo
        String json = \"{\\\"name\\\":\\\"João\\\",\\\"email\\\":null,\\\"age\\\":30}\";
        
        System.out.println(\"=== DESENVOLVIMENTO ===\");
        demonstrateProfile(\"dev\", true, true, true);
        
        System.out.println(\"\\n=== STAGING ===\");
        demonstrateProfile(\"staging\", false, false, false);
        
        System.out.println(\"\\n=== PRODUÇÃO ===\");
        demonstrateProfile(\"prod\", false, false, false);
    }
    
    static void demonstrateProfile(
        String profile,
        boolean pretty,
        boolean nulls,
        boolean lenient) {
        
        JsonMapper mapper = Json.builder()
            .prettyPrint(pretty)
            .serializeNulls(nulls)
            .lenient(lenient)
            .enableAnnotations(true)
            .build()
            .buildMapper();
        
        System.out.println(\"Perfil: \" + profile);
        System.out.println(\"PrettyPrint: \" + pretty);
        System.out.println(\"SerializeNulls: \" + nulls);
        System.out.println(\"Lenient: \" + lenient);
        
        // Exemplo
        JsonObject obj = new JsonObject();
        obj.addProperty(\"name\", \"Maria\");
        obj.add(\"email\", JsonNull.INSTANCE);
        obj.addProperty(\"age\", 28);
        
        String output = mapper.stringify(obj);
        System.out.println(\"Saída:\\n\" + output);
    }
}
```

## 📚 Próximos Passos

1. **[Tratamento de Erros](./12-tratamento-erros.md)** - Robustez
2. **[I/O de Arquivos](./13-arquivo-io.md)** - Operações avançadas
3. **[Padrões Práticos](./14-padroes-praticos.md)** - Best practices

---

**Anterior:** [10. Codecs Customizados](./10-codecs-customizados.md)  
**Próximo:** [12. Tratamento de Erros](./12-tratamento-erros.md)
