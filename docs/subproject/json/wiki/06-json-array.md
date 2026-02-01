# 6️⃣ JsonArray: Arrays JSON

## 🎯 O que é JsonArray?

`JsonArray` representa um array JSON com uma lista ordenada de elementos. É como uma `List<JsonElement>`:

```
JSON:        [ "João", 30, true, null ]
             ↓
Java:        JsonArray
             - [0] → JsonPrimitive("João")
             - [1] → JsonPrimitive(30)
             - [2] → JsonPrimitive(true)
             - [3] → JsonNull
```

## 🏗️ Criando JsonArrays

### Criar Vazio

```java
JsonArray array = new JsonArray();
```

### Adicionar Elementos

```java
JsonArray array = new JsonArray();

// Adicionar diferentes tipos
array.add("João");           // String
array.add(30);               // int
array.add(5000.50);          // double
array.add(true);             // boolean
array.add(4.5f);             // float

// Resultado JSON: ["João", 30, 5000.50, true, 4.5]
```

### Adicionar Null

```java
JsonArray array = new JsonArray();
array.add("João");
array.add((String) null);    // null
array.add(30);

// JSON: ["João", null, 30]
```

### Adicionar Objetos e Arrays Complexos

```java
// Array de objetos
JsonArray users = new JsonArray();

JsonObject user1 = new JsonObject();
user1.addProperty("name", "João");
user1.addProperty("age", 30);
users.add(user1);

JsonObject user2 = new JsonObject();
user2.addProperty("name", "Maria");
user2.addProperty("age", 28);
users.add(user2);

// JSON:
// [
//   { "name": "João", "age": 30 },
//   { "name": "Maria", "age": 28 }
// ]

// Array de arrays
JsonArray matrix = new JsonArray();
JsonArray row1 = new JsonArray();
row1.add(1);
row1.add(2);
row1.add(3);
matrix.add(row1);

JsonArray row2 = new JsonArray();
row2.add(4);
row2.add(5);
row2.add(6);
matrix.add(row2);

// JSON: [[1, 2, 3], [4, 5, 6]]
```

## 🔍 Acessando Valores

### Acessar por Índice

```java
JsonArray array = new JsonArray();
array.add("João");
array.add(30);
array.add("São Paulo");

// Acessar elemento
JsonElement first = array.get(0);           // JsonPrimitive("João")
JsonElement second = array.get(1);          // JsonPrimitive(30)
String city = array.get(2).asJsonPrimitive().asString();  // "São Paulo"

// Acessar e converter
String name = array.get(0).asJsonPrimitive().asString();  // "João"
int age = array.get(1).asJsonPrimitive().asInt();        // 30
```

### Verificar Tamanho

```java
JsonArray array = new JsonArray();
array.add("item1");
array.add("item2");
array.add("item3");

int size = array.size();      // 3
boolean empty = array.isEmpty(); // false
```

### Verificar Limites

```java
JsonArray array = new JsonArray();
array.add("item");

// Verificar índice válido
if (0 < array.size()) {
    JsonElement elem = array.get(0);
}

// Evitar IndexOutOfBoundsException
try {
    JsonElement elem = array.get(10);  // Índice inválido
} catch (IndexOutOfBoundsException e) {
    System.err.println("Índice fora dos limites");
}
```

## 📋 Iterando JsonArray

### For-each Loop

```java
JsonArray array = new JsonArray();
array.add("João");
array.add("Maria");
array.add("Pedro");

for (JsonElement element : array) {
    System.out.println(element.asJsonPrimitive().asString());
}

// Saída:
// João
// Maria
// Pedro
```

### For Loop Tradicional

```java
for (int i = 0; i < array.size(); i++) {
    JsonElement element = array.get(i);
    System.out.println(i + ": " + element);
}

// Saída:
// 0: "João"
// 1: "Maria"
// 2: "Pedro"
```

### Streams (Java 8+)

```java
array.stream()
    .filter(e -> e.isJsonPrimitive())
    .map(e -> e.asJsonPrimitive().asString())
    .forEach(System.out::println);

// Mais avançado
List<String> names = array.stream()
    .filter(e -> e.isJsonPrimitive())
    .map(e -> e.asJsonPrimitive().asString())
    .collect(Collectors.toList());
```

### Iterator

```java
Iterator<JsonElement> iterator = array.iterator();
while (iterator.hasNext()) {
    JsonElement element = iterator.next();
    System.out.println(element);
}
```

## 🔄 Modificar JsonArray

### Atualizar Elemento

```java
JsonArray array = new JsonArray();
array.add("João");
array.add("Maria");

// Atualizar índice 0
array.set(0, new JsonPrimitive("Pedro"));

System.out.println(array.get(0).asJsonPrimitive().asString()); // "Pedro"
```

### Inserir Elemento

```java
JsonArray array = new JsonArray();
array.add("João");
array.add("Maria");

// Método padrão: add() adiciona ao final
array.add("Pedro");  // ["João", "Maria", "Pedro"]

// Para inserir no meio, precisamos de um helper:
public static void insertAt(JsonArray array, int index, JsonElement element) {
    if (index < 0 || index > array.size()) {
        throw new IndexOutOfBoundsException("Índice inválido");
    }
    
    JsonArray newArray = new JsonArray();
    
    // Adicionar antes do índice
    for (int i = 0; i < index; i++) {
        newArray.add(array.get(i));
    }
    
    // Adicionar novo elemento
    newArray.add(element);
    
    // Adicionar resto
    for (int i = index; i < array.size(); i++) {
        newArray.add(array.get(i));
    }
    
    return newArray;
}

// Usar
JsonArray updated = insertAt(array, 1, new JsonPrimitive("Carlos"));
// Resultado: ["João", "Carlos", "Maria", "Pedro"]
```

### Remover Elemento

```java
JsonArray array = new JsonArray();
array.add("João");
array.add("Maria");
array.add("Pedro");

// Remover por índice
array.remove(1);  // Remove "Maria"
// Resultado: ["João", "Pedro"]
```

### Limpar Array

```java
JsonArray array = new JsonArray();
array.add("João");
array.add("Maria");

array.clear();

System.out.println(array.isEmpty()); // true
```

## 🛡️ Trabalhando com Tipos Mistos

Quando um array contém tipos diferentes:

```java
JsonArray array = new JsonArray();
array.add("texto");
array.add(42);
array.add(true);
array.add(JsonNull.INSTANCE);

// Processar com segurança
for (JsonElement element : array) {
    if (element.isJsonPrimitive()) {
        JsonPrimitive prim = element.asJsonPrimitive();
        
        if (prim.isString()) {
            System.out.println("String: " + prim.asString());
        } else if (prim.isNumber()) {
            System.out.println("Número: " + prim.asNumber());
        } else if (prim.isBoolean()) {
            System.out.println("Boolean: " + prim.asBoolean());
        }
    } else if (element.isJsonNull()) {
        System.out.println("Null");
    }
}
```

## 💡 Padrões Úteis

### Padrão 1: Builder Fluente

```java
public class UserListBuilder {
    private final JsonArray array = new JsonArray();
    
    public UserListBuilder addUser(String name, int age) {
        JsonObject user = new JsonObject();
        user.addProperty("name", name);
        user.addProperty("age", age);
        array.add(user);
        return this;
    }
    
    public JsonArray build() {
        return array;
    }
}

// Usar
JsonArray users = new UserListBuilder()
    .addUser("João", 30)
    .addUser("Maria", 28)
    .addUser("Pedro", 35)
    .build();
```

### Padrão 2: Filtro e Transformação

```java
public class ArrayProcessor {
    
    // Filtrar array
    public static JsonArray filter(JsonArray array, Predicate<JsonElement> predicate) {
        JsonArray result = new JsonArray();
        for (JsonElement elem : array) {
            if (predicate.test(elem)) {
                result.add(elem);
            }
        }
        return result;
    }
    
    // Mapear array
    public static JsonArray map(JsonArray array, Function<JsonElement, JsonElement> mapper) {
        JsonArray result = new JsonArray();
        for (JsonElement elem : array) {
            result.add(mapper.apply(elem));
        }
        return result;
    }
}

// Usar
JsonArray numbers = new JsonArray();
numbers.add(1); numbers.add(2); numbers.add(3); numbers.add(4);

// Filtrar números pares
JsonArray evens = ArrayProcessor.filter(numbers, e -> {
    int num = e.asJsonPrimitive().asInt();
    return num % 2 == 0;
});  // [2, 4]

// Dobrar valores
JsonArray doubled = ArrayProcessor.map(numbers, e -> {
    int num = e.asJsonPrimitive().asInt();
    return new JsonPrimitive(num * 2);
});  // [2, 4, 6, 8]
```

### Padrão 3: Redução

```java
public class ArrayReducer {
    
    public static int sumNumbers(JsonArray array) {
        int sum = 0;
        for (JsonElement elem : array) {
            if (elem.isJsonPrimitive()) {
                sum += elem.asJsonPrimitive().asInt();
            }
        }
        return sum;
    }
    
    public static String concat(JsonArray array, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            if (i > 0) sb.append(separator);
            sb.append(array.get(i).asJsonPrimitive().asString());
        }
        return sb.toString();
    }
}

// Usar
JsonArray numbers = new JsonArray();
numbers.add(1); numbers.add(2); numbers.add(3);
int total = ArrayReducer.sumNumbers(numbers);  // 6

JsonArray names = new JsonArray();
names.add("João"); names.add("Maria"); names.add("Pedro");
String joined = ArrayReducer.concat(names, ", ");  // "João, Maria, Pedro"
```

## 🎯 Exemplo Completo

```java
public class DataAnalysis {
    
    private final JsonMapper mapper;
    
    public DataAnalysis() {
        this.mapper = Json.defaultMapper();
    }
    
    public JsonArray createDataset() {
        JsonArray dataset = new JsonArray();
        
        String[] names = {"João", "Maria", "Pedro", "Ana", "Carlos"};
        int[] scores = {85, 92, 78, 88, 95};
        
        for (int i = 0; i < names.length; i++) {
            JsonObject record = new JsonObject();
            record.addProperty("name", names[i]);
            record.addProperty("score", scores[i]);
            dataset.add(record);
        }
        
        return dataset;
    }
    
    public double getAverageScore(JsonArray dataset) {
        double sum = 0;
        for (JsonElement elem : dataset) {
            JsonObject record = elem.asJsonObject();
            int score = record.getAsInt("score");
            sum += score;
        }
        return sum / dataset.size();
    }
    
    public JsonArray getTopScores(JsonArray dataset, int top) {
        JsonArray result = new JsonArray();
        
        dataset.stream()
            .map(e -> e.asJsonObject())
            .sorted((a, b) -> Integer.compare(
                b.getAsInt("score"),
                a.getAsInt("score")
            ))
            .limit(top)
            .forEach(result::add);
        
        return result;
    }
    
    public static void main(String[] args) {
        DataAnalysis da = new DataAnalysis();
        
        JsonArray dataset = da.createDataset();
        System.out.println("Dataset:");
        System.out.println(da.mapper.stringify(dataset));
        
        System.out.println("\nMédia: " + da.getAverageScore(dataset));
        
        JsonArray top3 = da.getTopScores(dataset, 3);
        System.out.println("\nTop 3:");
        System.out.println(da.mapper.stringify(top3));
    }
}
```

## 📚 Próximos Passos

1. **[JsonMapper](./07-json-mapper.md)** - Serialização avançada
2. **[TypeRef](./08-type-ref.md)** - Trabalhar com tipos genéricos
3. **[Anotações](./09-anotacoes.md)** - Controlar serialização

---

**Anterior:** [5. JsonObject](./05-json-object.md)  
**Próximo:** [7. JsonMapper: Serialização e Desserialização](./07-json-mapper.md)
