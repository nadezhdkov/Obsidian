# When - Expressões Condicionais Fluentes

`When` é uma classe utilitária que fornece uma API fluente e expressiva para trabalhar com condições, padrões de matching e lógica de decisão em Java. Transforma estruturas de controle tradicionais em expressões funcionais legíveis.

## Visão Geral

Em vez de escrever:

```java
if (condition) {
    // ação
} else if (otherCondition) {
    // outra ação
}
```

Com `When` você escreve:

```java
When.when(condition)
    .then(action)
    .when(otherCondition)
    .then(otherAction)
    .execute();
```

Ou obtém um valor:

```java
T result = When.choose(condition, valueIfTrue, valueIfFalse);
```

## Condições Simples

### when - Testar condição booleana

```java
When.when(age >= 18)
    .then(() -> System.out.println("Maior de idade"))
    .execute();
```

### whenNotNull - Verificar se não é nulo

```java
Object obj = getUserData();

When.whenNotNull(obj)
    .then(() -> System.out.println("Objeto existe: " + obj))
    .execute();
```

### whenNull - Verificar se é nulo

```java
String value = getUserName();

When.whenNull(value)
    .then(() -> System.out.println("Valor é nulo"))
    .execute();
```

### whenPresent - Verificar se Optional está presente

```java
Optional<User> user = findUser(id);

When.whenPresent(user)
    .then(() -> System.out.println("Usuário encontrado: " + user.get()))
    .execute();
```

### whenEmpty - Verificar se Optional está vazio

```java
Optional<User> user = findUser(id);

When.whenEmpty(user)
    .then(() -> System.out.println("Usuário não encontrado"))
    .execute();
```

### when com Supplier - Condição lazy

```java
When.when(() -> expensiveCheck())  // Avalia apenas se necessário
    .then(() -> doSomething())
    .execute();
```

## Escolhas (if-then-else)

### choose - Selecionar valor

```java
String message = When.choose(
    age >= 18,
    "Você é maior de idade",
    "Você é menor de idade"
);
```

Com suppliers (lazy evaluation):

```java
String message = When.choose(
    age >= 18,
    () -> generateAdultMessage(),     // Só avalia se true
    () -> generateMinorMessage()      // Só avalia se false
);
```

### ifElse - Executar ações

```java
When.ifElse(
    isLoggedIn,
    () -> showDashboard(),     // Se true
    () -> showLoginPage()      // Se false
);
```

### onlyIf - Executar se verdadeiro

```java
When.onlyIf(user.isAdmin(), () -> {
    System.out.println("Usuário é administrador");
    grantAdminAccess();
});
```

### unless - Executar se falso

```java
When.unless(user.isBlocked(), () -> {
    System.out.println("Usuário pode acessar");
    grantAccess();
});
```

## Cadeias de Decisão

### chain - Múltiplas condições

```java
When.chain()
    .when(score >= 90)
    .then(() -> System.out.println("Nota A"))
    .when(score >= 80)
    .then(() -> System.out.println("Nota B"))
    .when(score >= 70)
    .then(() -> System.out.println("Nota C"))
    .otherwise(() -> System.out.println("Nota F"))
    .execute();
```

A primeira condição verdadeira executa sua ação, depois para.

## Matching de Valores

### match - Pattern matching

```java
String result = When.match(user.getRole())
    .when("admin", () -> "Acesso total")
    .when("editor", () -> "Pode editar")
    .when("viewer", () -> "Apenas visualizar")
    .otherwise(() -> "Sem acesso")
    .get();
```

Com predicados:

```java
When.match(user)
    .when(u -> u.isAdmin(), () -> grantAdminAccess())
    .when(u -> u.isPremium(), () -> grantPremiumAccess())
    .otherwise(() -> grantBasicAccess())
    .execute();
```

## Tratamento de Erros com Throw

### throwIf - Lançar exceção se condição verdadeira

```java
When.throwIf(
    user == null,
    () -> new UserNotFoundException("Usuário não encontrado")
);

// Equivalente a:
if (user == null) {
    throw new UserNotFoundException("Usuário não encontrado");
}
```

### throwUnless - Lançar exceção se condição falsa

```java
When.throwUnless(
    user.isActive(),
    () -> new UserInactiveException("Usuário está inativo")
);

// Lança se user não for ativo
```

## Validações com Precondições

### requireTrue - Validar condição verdadeira

```java
When.requireTrue(age > 0, "Idade deve ser positiva");
When.requireTrue(email.contains("@"), "Email inválido");
```

### requireFalse - Validar condição falsa

```java
When.requireFalse(
    userExists(email),
    "Email já registrado no sistema"
);
```

### requireNonNull - Validar não nulo

```java
User user = When.requireNonNull(userData, "Dados do usuário são obrigatórios");
```

## Casos de Uso Comuns

### Validação de entrada

```java
public void createUser(String email, String password) {
    When.requireNonNull(email, "Email é obrigatório");
    When.requireTrue(email.contains("@"), "Email inválido");
    When.requireTrue(password.length() >= 8, "Senha deve ter 8+ caracteres");
    When.requireFalse(userRepository.exists(email), "Email já registrado");
    
    // Prosseguir com criação
    userRepository.save(new User(email, password));
}
```

### Lógica de negócio complexa

```java
public OrderStatus processOrder(Order order) {
    return When.choose(order.getTotal())
        .when(t -> t <= 0, () -> {
            When.throwIf(true, () -> new InvalidOrderException("Total inválido"));
            return null;
        })
        .when(t -> t < 100, () -> {
            order.setShippingCost(10);
            return OrderStatus.PROCESSING;
        })
        .when(t -> t < 500, () -> {
            order.setShippingCost(5);
            return OrderStatus.PROCESSING;
        })
        .otherwise(() -> {
            order.setShippingCost(0);
            return OrderStatus.PRIORITY;
        })
        .get();
}
```

### Handler de eventos com padrões

```java
public void handleUserEvent(UserEvent event) {
    When.match(event.getType())
        .when(UserEventType.REGISTERED, () -> {
            sendWelcomeEmail(event.getUser());
            initializeProfile(event.getUser());
        })
        .when(UserEventType.UPDATED, () -> {
            updateCache(event.getUser());
            notifySubscribers(event.getUser());
        })
        .when(UserEventType.DELETED, () -> {
            deleteAssociatedData(event.getUser());
            notifySubscribers(event.getUser());
        })
        .otherwise(() -> logger.warn("Evento desconhecido: " + event.getType()))
        .execute();
}
```

### Determinação de nível de acesso

```java
Permission getPermission(User user, Resource resource) {
    return When.chain()
        .when(user.isAdmin())
        .then(() -> Permission.FULL_ACCESS)
        .when(user.isOwner(resource))
        .then(() -> Permission.OWNER_ACCESS)
        .when(user.hasExplicitGrant(resource))
        .then(() -> Permission.GRANTED_ACCESS)
        .otherwise(() -> Permission.DENIED)
        .get();
}
```

### Formatação condicional

```java
String formatPrice(BigDecimal price) {
    return When.choose(
        price.signum() < 0,
        () -> "- R$ " + price.abs().setScale(2, RoundingMode.HALF_UP),
        () -> "R$ " + price.setScale(2, RoundingMode.HALF_UP)
    );
}
```

### Seleção de estratégia

```java
DataLoader selectLoader(String source) {
    return When.match(source)
        .when("cache", () -> new CacheDataLoader())
        .when("database", () -> new DatabaseDataLoader())
        .when("api", () -> new ApiDataLoader())
        .otherwise(() -> {
            throw new UnsupportedOperationException("Source: " + source);
        })
        .get();
}
```

### Validação de estado antes de operação

```java
public void startJob(Job job) {
    When.requireNonNull(job, "Job é obrigatório");
    When.requireTrue(
        job.getStatus() == JobStatus.CREATED,
        "Job deve estar em status CREATED"
    );
    When.throwIf(
        job.getScheduledTime().isBefore(LocalDateTime.now()),
        () -> new InvalidJobException("Horário já passou")
    );
    
    // Job é válido, prosseguir
    jobScheduler.schedule(job);
}
```

## Diferença entre Choose e Match

### choose - Valores imediatos ou computações

```java
// Para decisões simples
String access = When.choose(
    isAdmin,
    "FULL_ACCESS",
    "LIMITED_ACCESS"
);
```

### match - Pattern matching com múltiplos casos

```java
// Para múltiplos casos que se excluem
String status = When.match(code)
    .when(200, () -> "OK")
    .when(404, () -> "Not Found")
    .when(500, () -> "Internal Error")
    .otherwise(() -> "Unknown")
    .get();
```

## Fluência vs Execução Imediata

### Chain com execute() - Ações

```java
// Executa a primeira condição verdadeira
When.chain()
    .when(condition1)
    .then(() -> action1())
    .when(condition2)
    .then(() -> action2())
    .execute();  // <-- Executa aqui
```

### Choose com get() - Valores

```java
// Retorna valor baseado na primeira condição verdadeira
T result = When.chain()
    .when(condition1)
    .then(() -> value1)
    .when(condition2)
    .then(() -> value2)
    .get();  // <-- Retorna aqui
```

## Melhores Práticas

1. **Use `When.when()` para ações** - Quando precisa executar código
2. **Use `When.choose()` para valores** - Quando precisa retornar um valor
3. **Prefira `match()` para múltiplos valores** - Mais legível que muitos `if-else`
4. **Use precondições no início de métodos** - Validar entrada cedo
5. **Combine com Optional** - `whenPresent()`, `whenEmpty()`
6. **Evite nesting profundo** - Use chains ao invés de ifs aninhados
7. **Lazy evaluation** - Use Suppliers para evitar computações desnecessárias
8. **Seja descritivo** - Use nomes claros nas ações

## Limitações

- `When.chain()` está limitado a ActionWhen (void)
- Para retornar valores use `When.choose()` ou `When.match().get()`
- Uma chamada a `execute()` ou `get()` pode ser feita apenas uma vez

## Licença

Este código é parte do projeto Obsidian e está sob a licença Apache 2.0.
