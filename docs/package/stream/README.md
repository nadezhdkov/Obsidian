# Sequence<T> - Functional Stream Processing

`Sequence<T>` é uma abstração elegante sobre Java Streams que fornece uma API funcional e fluente para processar coleções de dados com suporte a operações paralelas, tratamento de erros e iteração customizada.

## Visão Geral

`Sequence<T>` encapsula um `Stream<T>` com funcionalidades adicionais:
- **API fluente** para operações em cadeia
- **Suporte paralelo** integrado
- **Tratamento de erros** robusto
- **Iteração customizada** com `loopUntil`
- **Async support** para operações não-bloqueantes
- **Memoization** de streams para operações múltiplas

## Criação de Sequence

### De uma coleção
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
Sequence<Integer> Sequence = Sequence.from(numbers);
```

### Varargs
```java
Sequence<String> Sequence = Sequence.of("a", "b", "c", "d");
```

### Ranges de números
```java
// Range inteiro [start, end) com step 1
Sequence<Integer> range = Sequence.range(0, 10);

// Range inteiro customizado
Sequence<Integer> evenNumbers = Sequence.range(0, 20, 2);

// Range longo
Sequence<Long> longRange = Sequence.range(0L, 1000000L, 1000L);
```

## Modo de Execução

### Sequenceuencial (padrão)
```java
Sequence<Integer> Sequence = Sequence.from(numbers)
    .Sequenceuential(); // Processa um elemento por vez
```

### Paralelo
```java
Sequence<Integer> Sequence = Sequence.from(numbers)
    .parallel()  // Processa em múltiplas threads
    .map(n -> expensiveOperation(n));
```

## Transformações

### filter - Selecionar elementos
```java
Sequence<Integer> result = Sequence.from(numbers)
    .filter(n -> n > 5);
```

### map - Transformar elementos
```java
Sequence<String> result = Sequence.from(numbers)
    .map(n -> "Número: " + n);
```

### flatMap - Transformar para stream e achatar
```java
Sequence<String> result = Sequence.of("hello", "world")
    .flatMap(word -> Sequence.from(word.split("")));
```

### reverse - Inverter ordem
```java
Sequence<Integer> reversed = Sequence.from(numbers)
    .reverse();
```

## Inspeção e Debug

### peek - Inspecionar elementos
```java
Sequence<Integer> result = Sequence.from(numbers)
    .peek(n -> System.out.println("Processing: " + n))
    .map(n -> n * 2);
```

### forEach - Iterar sobre elementos
```java
Sequence<Integer> Sequence = Sequence.from(numbers);
Sequence.forEach(n -> System.out.println(n));
```

### forEachAsync - Iterar de forma assíncrona
```java
CompletableFuture<Void> async = Sequence.from(largeList)
    .forEachAsync(item -> processItem(item));

// Aguardar conclusão
async.join();
```

## Redução e Coleta

### first - Obter primeiro elemento
```java
Optional<Integer> first = Sequence.from(numbers)
    .filter(n -> n > 5)
    .first();
```

### last - Obter último elemento
```java
Optional<Integer> last = Sequence.from(numbers)
    .last();
```

### toList - Coletar em lista
```java
List<Integer> list = Sequence.from(numbers)
    .filter(n -> n > 0)
    .toList();
```

### count - Contar elementos
```java
long total = Sequence.from(numbers)
    .filter(n -> n > 5)
    .count();
```

### stream - Acessar stream subjacente
```java
Stream<Integer> stream = Sequence.from(numbers)
    .stream();

// Usar operações específicas do Stream
int sum = Sequence.stream().mapToInt(Integer::intValue).sum();
```

## Limitação e Paginação

### limit - Pegar N primeiros elementos
```java
Sequence<Integer> limited = Sequence.from(numbers)
    .limit(3);  // Pega apenas os 3 primeiros
```

### skip - Pular N elementos
```java
Sequence<Integer> skipped = Sequence.from(numbers)
    .skip(5);   // Pula os 5 primeiros
```

### Paginação combinada
```java
int pageSize = 10;
int pageNumber = 2;

Sequence<Integer> page = Sequence.from(allNumbers)
    .skip((pageNumber - 1) * pageSize)
    .limit(pageSize);
```

## Tratamento de Erros

### onError - Handler customizado
```java
Sequence<Integer> Sequence = Sequence.from(numbers)
    .map(this::riskyOperation)
    .onError(error -> logger.error("Erro no processamento", error));

Sequence.forEach(System.out::println);
```

O handler padrão imprime a stack trace no stderr. Use `onError()` para customizar:

```java
Sequence<Integer> Sequence = Sequence.from(numbers)
    .onError(error -> {
        metrics.recordError(error);
        alerts.send("Erro em processamento: " + error.getMessage());
    });
```

## Iteração Avançada

### loopUntil - Iterar até condição
```java
Sequence<Integer> Sequence = Sequence.from(numbers)
    .loopUntil(n -> n == 10);  // Para quando n == 10

Sequence.forEach(System.out::println);
```

Útil para processamento contínuo até atingir uma condição:

```java
Sequence<Event> events = Sequence.from(eventQueue)
    .loopUntil(event -> event.type() == EventType.STOP);

events.forEach(this::handleEvent);
```

## Casos de Uso Comuns

### Processamento de lista
```java
List<User> users = getUsersFromDatabase();

Sequence.from(users)
    .filter(user -> user.isActive())
    .map(user -> user.getEmail())
    .forEach(email -> sendNotification(email));
```

### Transformação de dados
```java
Sequence.of("1", "2", "3", "abc", "5")
    .map(str -> Try.of(() -> Integer.parseInt(str)))
    .filter(Try::isSuccess)
    .map(Try::get)
    .toList();
    // Resultado: [1, 2, 3, 5]
```

### Processamento paralelo de grande volume
```java
Sequence.from(millionOfRecords)
    .parallel()
    .map(record -> expensiveComputation(record))
    .peek(result -> database.save(result))
    .count();
```

### Paginação de resultados
```java
public List<Product> getProducts(int page, int size) {
    return Sequence.from(repository.findAll())
        .filter(Product::isAvailable)
        .skip((page - 1) * size)
        .limit(size)
        .toList();
}
```

### Range para loops
```java
// Imprimir números de 1 a 100
Sequence.range(1, 101)
    .forEach(System.out::println);

// Processamento em steps
Sequence.range(0, 1000, 10)
    .map(i -> computeValue(i))
    .forEach(this::saveResult);
```

### Combinação com Try
```java
List<String> urls = getUrls();

Sequence.from(urls)
    .map(url -> Try.of(() -> fetchData(url)))
    .peek(result -> result.onFailure(e -> log.warn("Erro ao buscar", e)))
    .filter(Try::isSuccess)
    .map(Try::get)
    .toList();
```

### Processamento async
```java
// Iniciar processamento assíncrono de grande lista
Sequence.from(tasks)
    .parallel()
    .map(task -> heavyComputation(task))
    .forEachAsync(result -> updateUI(result))
    .thenRun(() -> System.out.println("Concluído!"))
    .join();
```

## Performance e Otimizações

### Lazy Evaluation
Todas as operações de transformação (map, filter, flatMap, etc) são lazy - não executam até que você chame uma operação terminal como `forEach()`, `toList()`, `count()`, etc.

```java
// Nada é executado aqui ainda
Sequence<Integer> chain = Sequence.from(numbers)
    .map(n -> expensive(n))
    .filter(n -> n > 100);

// Agora sim, o processamento acontece
chain.forEach(System.out::println);
```

### Paralelo para operações pesadas
```java
Sequence.from(millionOfItems)
    .parallel()
    .map(item -> complexCalculation(item))
    .toList();
```

### Evitar múltiplas iterações
```java
// Evite isto (itera 3 vezes)
long count = Sequence.count();
List<Integer> list = Sequence.toList();
long sum = Sequence.stream().mapToInt(Integer::intValue).sum();

// Faça isto (itera 1 vez)
List<Integer> list = Sequence.toList();
long count = list.size();
long sum = list.stream().mapToInt(Integer::intValue).sum();
```

## Melhores Práticas

1. **Use ranges para iteração numérica**: Mais legível que `Sequence.from(numbers)`
2. **Paralelize operations caras**: Use `.parallel()` para computações pesadas
3. **Combine com Try para segurança**: Trate erros de forma funcional
4. **Mantenha chains legíveis**: Divida em múltiplas linhas se necessário
5. **Use peek para debugging**: Não use forEach intermediário para debug
6. **Limite streams longos**: Use `limit()` para evitar processamento excessivo
7. **Prefira toList() para múltiplas iterações**: Evite reavaliar o stream

## Limitações

- Uma `Sequence` só pode ser consumida uma vez (como um `Stream`)
- Se precisar usar múltiplas vezes, colete em uma lista primeiro
- `loopUntil` executa em loop infinito até a condição ser alcançada

## Licença

Este código é parte do projeto Obsidian e está sob a licença especificada no projeto.
