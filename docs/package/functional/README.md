# Try<T> - Functional Error Handling

A `Try<T>` é uma monada que encapsula uma operação que pode ter sucesso ou falhar, fornecendo um mecanismo funcional e seguro para lidar com erros em Java.

## Visão Geral

`Try<T>` representa uma computação que pode resultar em:
- **Success**: Um valor do tipo T obtido com sucesso
- **Failure**: Uma exceção (Throwable) capturada durante a execução

Em vez de lançar exceções ou usar null, `Try<T>` força você a lidar explicitamente com cenários de erro, levando a um código mais robusto e previsível.

## Construção de Try

### Criando um Try com sucesso
```java
Try<Integer> success = Try.success(42);
```

### Criando um Try com falha
```java
Try<Integer> failure = Try.failure(new IllegalArgumentException("Invalid input"));
```

### Wrappando uma operação que pode falhar
```java
Try<Integer> result = Try.of(() -> Integer.parseInt("123"));
```

Se a operação lançar uma exceção, ela será capturada e envolvida em um `Failure`.

### Executando um Runnable que pode falhar
```java
Try<Void> result = Try.run(() -> {
    // código que pode falhar
    System.out.println("Executando...");
});
```

### Com recursos (try-with-resources)
```java
FileInputStream fis = new FileInputStream("file.txt");
Try<String> result = Try.of(Try.withResources(content -> {
    // processar recurso
    return content.toString();
})).apply(fis);
```

## Verificando o resultado

### Consultando o estado
```java
Try<String> result = Try.of(() -> getValue());

if (result.isSuccess()) {
    String value = result.get();
} else if (result.isFailure()) {
    Throwable error = result.exception().orElse(null);
}
```

### Obtendo o valor
```java
// Obtém o valor ou lança uma exceção encapsulada
String value = result.get();

// Obtém o valor de forma segura (checkedGet lança Exception)
try {
    String value = result.checkedGet();
} catch (Exception e) {
    // tratar exceção
}

// Obtém o valor ou um valor padrão
String value = result.getOrElse("default");
```

### Acessando a exceção
```java
Optional<Throwable> error = result.exception();
error.ifPresent(e -> System.err.println("Erro: " + e.getMessage()));
```

## Transformações Funcionais

### map - Transformar o valor de sucesso
```java
Try<Integer> number = Try.of(() -> Integer.parseInt("123"));
Try<String> text = number.map(n -> "Número: " + n);
```

### mapTry - Transformar em uma operação que pode falhar
```java
Try<Integer> result = Try.of(() -> Integer.parseInt("123"))
    .mapTry(n -> findUser(n)); // findUser pode lançar exceção
```

### flatMap - Encadear operações que retornam Try
```java
Try<User> user = Try.of(() -> getUserId())
    .flatMap(id -> Try.of(() -> database.findUser(id)));
```

### filter - Filtrar valores com base em um predicado
```java
Try<Integer> number = Try.of(() -> Integer.parseInt("123"));
Try<Integer> filtered = number.filter(n -> n > 0);

// Se o predicado não passar, retorna uma Failure
// com NoSuchElementException
```

## Recuperação de Erros

### recover - Recuperar com um valor
```java
Try<String> result = Try.of(() -> riskyOperation())
    .recover(error -> "valor_padrao");
```

### recover com tipo específico
```java
Try<String> result = Try.of(() -> riskyOperation())
    .recover(IOException.class, error -> "IO error: " + error.getMessage());
```

### recoverWith - Recuperar com outra operação Try
```java
Try<String> result = Try.of(() -> primarySource())
    .recoverWith(error -> Try.of(() -> fallbackSource()));
```

### mapFailure - Transformar a exceção
```java
Try<String> result = Try.of(() -> riskyOperation())
    .mapFailure(error -> new CustomException("Falha na operação", error));
```

## Inspecionando valores

### forEach - Executar ação se bem-sucedido
```java
Try<String> result = Try.of(() -> getValue());
result.forEach(value -> System.out.println("Sucesso: " + value));
```

### peek - Inspecionar sem modificar
```java
Try<String> result = Try.of(() -> getValue())
    .peek(value -> System.out.println("Valor: " + value))
    .map(String::toUpperCase);
```

### peekFailure / onFailure - Inspecionar erro
```java
Try<String> result = Try.of(() -> riskyOperation())
    .onFailure(error -> logger.error("Erro: " + error.getMessage()));
```

## Encadeamento de Operações

### andThen - Executar ação e continuar com o valor
```java
Try<String> result = Try.of(() -> getValue())
    .andThen(value -> System.out.println("Got: " + value))
    .andThen(value -> validateData(value));
```

## Transformações Avançadas

### transform - Tratar sucesso e falha
```java
String message = Try.of(() -> Integer.parseInt("abc"))
    .transform(
        success -> Try.success("Valor: " + success),
        failure -> Try.success("Erro: " + failure.getMessage())
    )
    .getOrElse("Desconhecido");
```

### fold - Reduzir a um valor final
```java
String message = Try.of(() -> Integer.parseInt("123"))
    .fold(
        value -> "Sucesso: " + value,
        error -> "Erro: " + error.getMessage()
    );
```

### failed - Converter sucesso em falha
```java
Try<Throwable> errorTry = Try.success("valor")
    .failed(); // Lança UnsupportedOperationException
```

## Conversão para outros tipos

### toOptional - Converter para Optional
```java
Try<String> result = Try.of(() -> getValue());
Optional<String> optional = result.toOptional();
```

### orElse - Usar outro Try como fallback
```java
Try<String> result = Try.of(() -> primarySource())
    .orElse(Try.of(() -> fallbackSource()));
```

## Tratamento de Erros Avançado

### getOrThrow - Obter ou lançar exceção customizada
```java
String value = Try.of(() -> getValue())
    .getOrThrow(error -> new CustomException("Falha", error));
```

### flatten - Achatar Try aninhado
```java
Try<Try<String>> nested = Try.success(Try.success("valor"));
Try<String> flattened = Try.flatten(nested);
```

## Casos de Uso Comuns

### Parsing seguro
```java
Try<Integer> parseNumber(String input) {
    return Try.of(() -> Integer.parseInt(input));
}

// Usar
parseNumber("123")
    .map(n -> n * 2)
    .forEach(result -> System.out.println(result));
```

### Operações de banco de dados
```java
Try<User> getUser(int id) {
    return Try.of(() -> database.findUser(id))
        .recover(SQLException.class, 
                 e -> defaultUser());
}
```

### Encadeamento de operações com fallback
```java
Try<String> getData() {
    return Try.of(() -> remoteAPI.fetch())
        .recover(e -> Try.of(() -> cache.get()).get())
        .recover(e -> "Sem dados disponíveis");
}
```

### Limpeza de recursos
```java
Try<String> readFile(String path) {
    return Try.of(() -> {
        try (FileReader reader = new FileReader(path)) {
            return IOUtils.toString(reader);
        }
    });
}
```

## Melhores Práticas

1. **Use Try para APIs públicas**: Quando você quer forçar o chamador a lidar com erros
2. **Prefira map/flatMap a get()**: Evite extrair valores e trabalhar diretamente com Try
3. **Use recover para lógica específica**: Em vez de try-catch, use recover para tratar erros esperados
4. **Combine com Seq<T>**: Use Try com Seq para processamento funcional de coleções
5. **Mantenha as chains curtas**: Operações muito longas podem se tornar difíceis de ler

## Licença

Este código é parte do projeto Obsidian e está sob a licença especificada no projeto.
