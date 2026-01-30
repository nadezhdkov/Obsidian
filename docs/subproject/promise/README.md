# Promise<T> - Asynchronous Computation API

`Promise<T>` é uma API moderna e funcional para trabalhar com computações assíncronas em Java. Ela fornece uma forma elegante de encadear operações assíncronas, lidar com erros e gerenciar cancelamentos, sem os callbacks aninhados tradicionais.

## Visão Geral

`Promise<T>` representa um valor que será calculado no futuro. Diferentemente de `CompletableFuture`, `Promise` oferece:
- **Imutabilidade** - Cada transformação cria uma nova Promise
- **Segurança de threads** - Operações são thread-safe por padrão
- **Composição funcional** - Encadeie operações de forma legível
- **Tratamento de erros robusto** - Múltiplas estratégias de recuperação
- **Suporte a cancelamento** - Cancele operações com tokens e razões
- **Retry automático** - Políticas configuráveis de retry

## Estados

Uma Promise pode estar em um dos seguintes estados:

- **PENDING** - Computação ainda está em progresso
- **FULFILLED** - Computação completou com sucesso
- **REJECTED** - Computação falhou com uma exceção
- **CANCELLED** - Computação foi cancelada

## Criação de Promises

### Valor já resolvido

```java
// Criar uma Promise que já possui um valor
Promise<String> promise = Promises.value("Hello, World!");
```

### Erro já rejeitado

```java
// Criar uma Promise que já falhou
Promise<String> promise = Promises.error(new RuntimeException("Erro!"));
```

### Computação assíncrona

```java
// Criar a partir de uma operação assíncrona
Promise<Data> promise = Promises.async(() -> loadDataFromServer());

// Com executor customizado
Promise<Data> promise = Promises.async(
    () -> loadDataFromServer(),
    executorService
);
```

### Deferred (controle manual)

```java
// Para controle total da resolução
Deferred<String> deferred = Promises.deferred();

new Thread(() -> {
    try {
        String result = performLongOperation();
        deferred.resolve(result);  // Resolve com sucesso
    } catch (Exception e) {
        deferred.reject(e);  // Rejeita com erro
    }
}).start();

Promise<String> promise = deferred.promise();
```

### De um Callable

```java
Promise<Integer> promise = Promises.callable(() -> 42);
```

### De um CompletableFuture

```java
CompletableFuture<String> future = ...;
Promise<String> promise = Promises.from(future);
```

## Transformações

### map - Transformar valor

```java
Promise<Integer> promise = Promises.value("123")
    .map(Integer::parseInt);
    
promise.get();  // 123
```

### flatMap/then - Encadear Promises

```java
Promise<User> user = loadUser(userId)
    .flatMap(user -> loadUserPreferences(user.getId()))
    .flatMap(prefs -> applyPreferences(prefs));
```

Você pode usar `then()` como alias para `flatMap()`:

```java
Promise<User> result = loadUser(userId)
    .then(user -> loadProfile(user))
    .then(profile -> enrichProfile(profile));
```

### tap - Executar efeito colateral

```java
Promise<Data> result = operation()
    .tap(data -> System.out.println("Resultado: " + data))
    .tap(data -> database.save(data));
```

### filter - Filtrar valores

```java
Promise<Integer> result = Promises.value(10)
    .filter(n -> n > 5)  // Passa
    .filter(n -> n < 8);  // Falha com rejectionException

// Com mensagem de erro customizada
Promise<User> user = loadUser(id)
    .filter(u -> u.isActive(), () -> new UserInactiveException());
```

## Tratamento de Erros

### recover - Recuperar com valor

```java
Promise<Integer> result = riskyOperation()
    .recover(error -> {
        logger.error("Erro no cálculo, usando padrão", error);
        return 0;  // Valor padrão
    });

result.get();  // 0 se falhar
```

### recoverWith - Recuperar com nova Promise

```java
Promise<Data> result = loadFromPrimary()
    .recoverWith(error -> loadFromBackup())
    .recoverWith(error -> loadFromCache())
    .recover(error -> Data.empty());
```

### catchError - Capturar erros específicos

```java
Promise<Data> result = operation()
    .catchError(TimeoutException.class, e -> {
        logger.warn("Timeout, tentando de novo");
        return retryWithBackoff();
    })
    .catchError(IOException.class, e -> Data.cached());
```

Capturar todos os erros:

```java
Promise<Data> result = operation()
    .catchError(error -> Data.empty());
```

### mapError - Transformar erro

```java
Promise<Data> result = repository.load()
    .mapError(SQLException::class, e -> 
        new DataAccessException("Falha ao acessar dados", e)
    );
```

### finallyDo - Cleanup (como try-finally)

```java
Promise<Result> result = operation()
    .tap(r -> r.process())
    .finallyDo(() -> {
        resource.close();  // Sempre executado
    });
```

## Timing e Controle

### timeout - Aplicar timeout

```java
Promise<Data> result = slowOperation()
    .timeout(Duration.ofSeconds(5));

result.onError(e -> {
    if (e instanceof TimeoutException) {
        System.out.println("Operação expirou!");
    }
});
```

### delay - Atrasar execução

```java
Promise<String> result = Promises.value("Hello")
    .delay(Duration.ofSeconds(2))  // Espera 2 segundos
    .map(s -> s + " World");
```

### retry - Retentar automaticamente

```java
// Retentar até 3 vezes
Promise<Data> result = failingOperation()
    .retry(3);

// Com política customizada
Promise<Data> result = failingOperation()
    .retry(RetryPolicy.configure()
        .maxAttempts(5)
        .initialDelay(Duration.ofMillis(100))
        .maxDelay(Duration.ofSeconds(10))
        .backoffMultiplier(2.0)
        .build());
```

## Callbacks

### onSuccess - Sucesso

```java
Promise<String> promise = loadData();
promise.onSuccess(data -> {
    System.out.println("Sucesso: " + data);
});
```

### onError - Erro

```java
promise.onError(error -> {
    logger.error("Falha", error);
});
```

### onCancelled - Cancelamento

```java
promise.onCancelled(() -> {
    System.out.println("Operação foi cancelada");
});
```

### onComplete - Qualquer conclusão

```java
promise.onComplete(() -> {
    System.out.println("Operação terminou (sucesso, erro ou cancelamento)");
});
```

## Checando Estado

```java
Promise<Data> promise = operation();

if (promise.isPending()) {
    System.out.println("Ainda em progresso");
}

if (promise.isFulfilled()) {
    System.out.println("Completou com sucesso");
}

if (promise.isRejected()) {
    System.out.println("Falhou com erro");
}

if (promise.isCancelled()) {
    System.out.println("Foi cancelada");
}
```

## Cancelamento

### Cancelar simples

```java
Promise<Data> promise = longRunningOperation();

boolean cancelled = promise.cancel();  // true se cancelou, false se já completo
```

### Cancelar com razão

```java
promise.cancel("Usuário clicou em cancelar");
```

### Tokens de cancelamento

```java
CancellationToken token = CancellationSource.token();

Promise<Data> promise = operation(token);

// Cancelar todas as operações ligadas a este token
token.source().cancel("Timeout global");
```

## Operações de Bloqueio

### get - Bloquear e obter resultado

```java
try {
    String result = promise.get();
    System.out.println("Resultado: " + result);
} catch (Throwable e) {
    System.err.println("Operação falhou: " + e.getMessage());
}
```

### get com timeout

```java
try {
    String result = promise.get(Duration.ofSeconds(10));
} catch (Throwable e) {
    System.err.println("Operação expirou ou falhou");
}
```

### getOrDefault

```java
String result = promise.getOrDefault("valor padrão");
```

### getOrElse

```java
String result = promise.getOrElse(() -> computeDefault());
```

## Composição de Múltiplas Promises

### Promises.all - Todas as promises

```java
Promise<List<Data>> result = Promises.all(
    loadUser(id),
    loadPreferences(id),
    loadAnalytics(id)
);

// Bloqueia até todas completarem ou uma falhar
List<Data> data = result.get();
```

Com arrays:

```java
Promise<Integer>[] promises = new Promise[3];
promises[0] = Promises.value(1);
promises[1] = Promises.value(2);
promises[2] = Promises.value(3);

Promise<List<Integer>> result = Promises.all(promises);
```

### Promises.any - Primeira a completar com sucesso

```java
Promise<Data> result = Promises.any(
    loadFromServer1(),
    loadFromServer2(),
    loadFromServer3()
);

// Completa com sucesso da primeira fonte disponível
Data data = result.get();
```

### Promises.race - Primeira a completar

```java
Promise<Response> result = Promises.race(
    timeoutPromise(Duration.ofSeconds(5)),
    networkRequest()
);

// Retorna qualquer que complete primeiro
Response response = result.get();
```

## Interoperabilidade

### Converter para CompletableFuture

```java
Promise<String> promise = ...;
CompletableFuture<String> future = promise.toCompletableFuture();

// Agora pode usar com APIs que esperam CompletableFuture
future.thenAccept(System.out::println);
```

### Converter para Future

```java
Future<String> future = promise.toFuture();
```

## Casos de Uso Comuns

### Chamar API externa com retry e timeout

```java
Promise<User> user = Promises.async(() -> apiClient.getUser(id))
    .timeout(Duration.ofSeconds(10))
    .retry(RetryPolicy.configure()
        .maxAttempts(3)
        .initialDelay(Duration.ofMillis(100))
        .backoffMultiplier(2.0)
        .build())
    .recover(error -> User.empty());
```

### Processamento paralelo com fallback

```java
Promise<Data> data = Promises.any(
    loadFromCache(),
    loadFromPrimary(),
    loadFromBackup()
)
.recoverWith(error -> {
    logger.error("Todas as fontes falharam", error);
    return Promises.value(Data.empty());
});
```

### Encadeamento de operações dependentes

```java
Promise<Report> report = loadUser(userId)
    .then(user -> loadData(user))
    .then(data -> processData(data))
    .then(processed -> generateReport(processed))
    .tap(report -> saveReport(report))
    .tap(report -> sendNotification(report));
```

### Operação com limpeza garantida

```java
Promise<Result> result = acquireResource()
    .then(resource -> {
        return processWithResource(resource);
    })
    .finallyDo(() -> {
        releaseResource();  // Garantido executar
    });
```

## Melhores Práticas

1. **Prefira composição a callbacks** - Use `map()`, `flatMap()`, `tap()` ao invés de `onSuccess()`
2. **Use `recover` para erros conhecidos** - `recover()` ou `recoverWith()` para erros esperados
3. **Combine Promises para operações paralelas** - Use `Promises.all()` quando possível
4. **Especifique timeouts** - Sempre defina `.timeout()` para operações de rede
5. **Use retry com políticas** - Configure backoff exponencial para retries
6. **Mantenha chains legíveis** - Divida em múltiplas linhas quando necessário
7. **Teste com `Promises.value()` e `Promises.error()`** - Para testes unitários
8. **Evite bloquear em hot paths** - Use callbacks ou async/await patterns

## Limitações

- Promises só podem ser consumidas uma vez (como Streams)
- Se precisar reutilizar, converta para `CompletableFuture` ou armazene o resultado
- Bloqueiar em thread main UI pode causar travamento
- Certos erros não podem ser recuperados (OutOfMemoryError, StackOverflowError)

## Licença

Este código é parte do projeto Obsidian e está sob a licença Apache 2.0.
