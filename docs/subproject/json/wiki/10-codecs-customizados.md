# 🔟 Codecs Customizados

## 🎯 O que é um Codec?

Um `JsonCodec<T>` é responsável por converter um objeto Java de tipo `T` para e de `JsonElement`. Permite customizar completamente como um tipo é serializado e desserializado.

```
Objeto Java (UUID)
       ↓
   JsonCodec
       ↓
JsonElement (String)
```

## 🏗️ Criando um Codec

Implemente a interface `JsonCodec<T>`:

```java
import io.obsidian.json.codec.JsonCodec;

public class UuidCodec implements JsonCodec<UUID> {

    @Override
    public JsonElement encode(UUID value) {
        // UUID → JsonElement
        return new JsonPrimitive(value.toString());
    }

    @Override
    public UUID decode(JsonElement element) {
        // JsonElement → UUID
        String str = element.asJsonPrimitive().asString();
        return UUID.fromString(str);
    }
}
```

## 📝 Usando Codecs

### Opção 1: Via Anotação @JsonAdapter

```java
public class Order {
    @JsonAdapter(UuidCodec.class)
    private UUID orderId;
    
    private String description;
}

// Usar normalmente - codec é aplicado automaticamente
Order order = mapper.decode(source, TypeRef.of(Order.class));
```

### Opção 2: Registrar Globalmente

```java
JsonMapper mapper = Json.builder()
    .registerCodec(UUID.class, new UuidCodec())
    .enableAnnotations(true)
    .build()
    .buildMapper();

// Agora UuidCodec é usado para todos os UUID
public class Order {
    private UUID orderId;  // Sem anotação, codec é aplicado
}
```

## 💡 Exemplos Práticos

### Exemplo 1: Codec para LocalDate

```java
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateCodec implements JsonCodec<LocalDate> {
    
    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern(\"yyyy-MM-dd\");
    
    @Override
    public JsonElement encode(LocalDate value) {
        return new JsonPrimitive(value.format(FORMATTER));
    }
    
    @Override
    public LocalDate decode(JsonElement element) {
        String str = element.asJsonPrimitive().asString();
        return LocalDate.parse(str, FORMATTER);
    }
}

// Usar
public class Event {
    private String name;
    
    @JsonAdapter(LocalDateCodec.class)
    private LocalDate eventDate;
}

// JSON: {\"name\":\"Conferência\",\"eventDate\":\"2024-01-15\"}
```

### Exemplo 2: Codec para BigDecimal

```java
import java.math.BigDecimal;

public class BigDecimalCodec implements JsonCodec<BigDecimal> {
    
    @Override
    public JsonElement encode(BigDecimal value) {
        // Sempre como string para precisão
        return new JsonPrimitive(value.toPlainString());
    }
    
    @Override
    public BigDecimal decode(JsonElement element) {
        String str = element.asJsonPrimitive().asString();
        return new BigDecimal(str);
    }
}

// Usar
public class Product {
    private String name;
    
    @JsonAdapter(BigDecimalCodec.class)
    private BigDecimal price;
}

// JSON: {\"name\":\"Laptop\",\"price\":\"1999.99\"}
```

### Exemplo 3: Codec para Enum

```java
public enum Status {
    ACTIVE(\"active\"),
    INACTIVE(\"inactive\"),
    PENDING(\"pending\");
    
    private final String code;
    
    Status(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static Status fromCode(String code) {
        for (Status status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException(\"Unknown status: \" + code);
    }
}

public class StatusCodec implements JsonCodec<Status> {
    
    @Override
    public JsonElement encode(Status value) {
        return new JsonPrimitive(value.getCode());
    }
    
    @Override
    public Status decode(JsonElement element) {
        String code = element.asJsonPrimitive().asString();
        return Status.fromCode(code);
    }
}

// Usar
public class User {
    private String name;
    
    @JsonAdapter(StatusCodec.class)
    private Status status;
}

// JSON: {\"name\":\"João\",\"status\":\"active\"}
```

### Exemplo 4: Codec para Coleções Customizadas

```java
public class EmailListCodec implements JsonCodec<Set<String>> {
    
    @Override
    public JsonElement encode(Set<String> value) {
        JsonArray array = new JsonArray();
        for (String email : value) {
            array.add(email);
        }
        return array;
    }
    
    @Override
    public Set<String> decode(JsonElement element) {
        Set<String> emails = new LinkedHashSet<>();
        JsonArray array = element.asJsonArray();
        for (JsonElement item : array) {
            emails.add(item.asJsonPrimitive().asString());
        }
        return emails;
    }
}

// Usar
public class Contact {
    private String name;
    
    @JsonAdapter(EmailListCodec.class)
    private Set<String> emails;
}

// JSON: {\"name\":\"João\",\"emails\":[\"joao@example.com\",\"joao.silva@company.com\"]}
```

### Exemplo 5: Codec com Validação

```java
public class EmailCodec implements JsonCodec<String> {
    
    private static final String EMAIL_REGEX = 
        \"^[A-Za-z0-9+_.-]+@(.+)$\";
    
    @Override
    public JsonElement encode(String value) {
        return new JsonPrimitive(value);
    }
    
    @Override
    public String decode(JsonElement element) {
        String email = element.asJsonPrimitive().asString();
        
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException(
                \"Email inválido: \" + email
            );
        }
        
        return email.toLowerCase();  // Normalizar
    }
}

// Usar
public class User {
    @JsonAdapter(EmailCodec.class)
    private String email;
}

// Desserializar - valida e normaliza automaticamente
String json = \"{\\\"email\\\":\\\"JOAO@EXAMPLE.COM\\\"}\";
User user = mapper.decode(JsonSource.of(json), TypeRef.of(User.class));
System.out.println(user.email);  // joao@example.com (normalizado!)
```

### Exemplo 6: Codec com Criptografia

```java
import java.util.Base64;

public class EncryptedStringCodec implements JsonCodec<String> {
    
    @Override
    public JsonElement encode(String value) {
        // Criptografar antes de serializar
        String encrypted = Base64.getEncoder()
            .encodeToString(value.getBytes());
        return new JsonPrimitive(encrypted);
    }
    
    @Override
    public String decode(JsonElement element) {
        // Descriptografar ao desserializar
        String encrypted = element.asJsonPrimitive().asString();
        byte[] decoded = Base64.getDecoder().decode(encrypted);
        return new String(decoded);
    }
}

// Usar
public class Secret {
    private String name;
    
    @JsonAdapter(EncryptedStringCodec.class)
    private String apiKey;
}

// JSON tem a chave criptografada
// {\"name\":\"API Secret\",\"apiKey\":\"YWJjMTIz\"}
// Ao desserializar, é automaticamente descriptografada
```

## 🛡️ Tratamento de Erros em Codecs

```java
public class SafeCodec implements JsonCodec<MyType> {
    
    @Override
    public JsonElement encode(MyType value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        
        try {
            // Lógica de encoding
            return new JsonPrimitive(value.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                \"Erro ao encodar: \" + e.getMessage(), e
            );
        }
    }
    
    @Override
    public MyType decode(JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        }
        
        try {
            String str = element.asJsonPrimitive().asString();
            // Lógica de decoding
            return MyType.parse(str);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                \"Erro ao decodificar: \" + e.getMessage(), e
            );
        }
    }
}
```

## 💡 Padrões Úteis

### Padrão 1: Codec Factory

```java
public class CodecFactory {
    
    public static <T extends Enum<T>> JsonCodec<T> enumCodec(Class<T> enumType) {
        return new JsonCodec<T>() {
            @Override
            public JsonElement encode(T value) {
                return new JsonPrimitive(value.name());
            }
            
            @Override
            public T decode(JsonElement element) {
                String name = element.asJsonPrimitive().asString();
                return Enum.valueOf(enumType, name);
            }
        };
    }
    
    public static JsonCodec<LocalDate> dateCodec(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return new JsonCodec<LocalDate>() {
            @Override
            public JsonElement encode(LocalDate value) {
                return new JsonPrimitive(value.format(formatter));
            }
            
            @Override
            public LocalDate decode(JsonElement element) {
                String str = element.asJsonPrimitive().asString();
                return LocalDate.parse(str, formatter);
            }
        };
    }
}

// Usar
JsonMapper mapper = Json.builder()
    .registerCodec(Status.class, CodecFactory.enumCodec(Status.class))
    .registerCodec(LocalDate.class, CodecFactory.dateCodec(\"dd/MM/yyyy\"))
    .build()
    .buildMapper();
```

### Padrão 2: Codec Wrapper

```java
public class WrappedValue<T> {
    private final T value;
    
    public WrappedValue(T value) {
        this.value = value;
    }
    
    public T getValue() {
        return value;
    }
}

public class WrappedValueCodec<T> implements JsonCodec<WrappedValue<T>> {
    
    private final JsonCodec<T> innerCodec;
    
    public WrappedValueCodec(JsonCodec<T> innerCodec) {
        this.innerCodec = innerCodec;
    }
    
    @Override
    public JsonElement encode(WrappedValue<T> value) {
        return innerCodec.encode(value.getValue());
    }
    
    @Override
    public WrappedValue<T> decode(JsonElement element) {
        T innerValue = innerCodec.decode(element);
        return new WrappedValue<>(innerValue);
    }
}
```

## 🎯 Exemplo Completo

```java
public class PaymentSystem {
    
    // Codec customizado para moeda
    public static class MoneyCodec implements JsonCodec<BigDecimal> {
        @Override
        public JsonElement encode(BigDecimal value) {
            return new JsonPrimitive(String.format(\"%.2f\", value));
        }
        
        @Override
        public BigDecimal decode(JsonElement element) {
            String str = element.asJsonPrimitive().asString();
            return new BigDecimal(str);
        }
    }
    
    // Codec customizado para status de pagamento
    public static class PaymentStatusCodec implements JsonCodec<PaymentStatus> {
        @Override
        public JsonElement encode(PaymentStatus value) {
            return new JsonPrimitive(value.getCode());
        }
        
        @Override
        public PaymentStatus decode(JsonElement element) {
            String code = element.asJsonPrimitive().asString();
            return PaymentStatus.fromCode(code);
        }
    }
    
    public enum PaymentStatus {
        PENDING(\"pending\"),
        COMPLETED(\"completed\"),
        FAILED(\"failed\");
        
        private final String code;
        
        PaymentStatus(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
        
        public static PaymentStatus fromCode(String code) {
            for (PaymentStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException(\"Unknown status: \" + code);
        }
    }
    
    public static class Payment {
        @JsonName(\"payment_id\")
        public String id;
        
        @JsonAdapter(MoneyCodec.class)
        public BigDecimal amount;
        
        @JsonAdapter(PaymentStatusCodec.class)
        public PaymentStatus status;
    }
    
    public static void main(String[] args) {
        JsonMapper mapper = Json.builder()
            .enableAnnotations(true)
            .prettyPrint(true)
            .build()
            .buildMapper();
        
        // Criar pagamento
        Payment payment = new Payment();
        payment.id = \"pay_123\";
        payment.amount = new BigDecimal(\"1999.99\");
        payment.status = PaymentStatus.COMPLETED;
        
        // Serializar
        String json = mapper.stringify(mapper.encode(payment));
        System.out.println(json);
        // {
        //   \"payment_id\": \"pay_123\",
        //   \"amount\": \"1999.99\",
        //   \"status\": \"completed\"
        // }
        
        // Desserializar
        Payment loaded = mapper.decode(JsonSource.of(json), TypeRef.of(Payment.class));
        System.out.println(\"Status: \" + loaded.status);  // PaymentStatus.COMPLETED
    }
}
```

## 📚 Próximos Passos

1. **[Configuração](./11-configuracao.md)** - Ajustar comportamento global
2. **[Tratamento de Erros](./12-tratamento-erros.md)** - Robustez
3. **[Exemplos Completos](./15-exemplos-completos.md)** - Aplicações reais

---

**Anterior:** [9. Anotações](./09-anotacoes.md)  
**Próximo:** [11. Configuração: JsonConfig Builder](./11-configuracao.md)
