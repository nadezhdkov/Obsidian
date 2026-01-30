# Box<T> - Thread-Safe Mutable Container

`Box<T>` é uma abstração elegante para um contêiner mutável thread-safe que encapsula um valor de tipo `T`. Fornece diferentes estratégias de armazenamento com garantias de visibilidade e atomicidade variáveis.

## Visão Geral

`Box<T>` é útil quando você precisa compartilhar um valor mutável entre threads com diferentes garantias de segurança:
- **AtomicBox** - Operações atômicas completas com CAS (Compare-And-Swap)
- **VolatileBox** - Visibilidade entre threads com `volatile`
- **PlainBox** - Sem garantias de sincronização (use em single-threaded)

## Criando Boxes

### AtomicBox - Operações atômicas

```java
// Criar um AtomicBox com valor inicial
Box<Integer> counter = Box.atomic(0);

// Operações atômicas
counter.set(10);
int value = counter.get();

// Compare-and-set
boolean success = counter.compareAndSet(10, 20);  // true se conseguiu trocar

// Get and set atomicamente
int oldValue = counter.getAndSet(30);
```

### VolatileBox - Visibilidade

```java
// Criar um VolatileBox
Box<String> flag = Box.volatileBox("initial");

// Garante visibilidade entre threads mas não atomicidade completa
flag.set("updated");
String value = flag.get();
```

### PlainBox - Sem sincronização

```java
// Usar apenas em contextos single-threaded
Box<Data> data = Box.plain(new Data());

// Sem overhead de sincronização
data.set(newData);
Data current = data.get();
```

## Operações Básicas

### get - Obter valor

```java
Box<Integer> box = Box.atomic(42);
int value = box.get();  // 42
```

### set - Definir valor

```java
box.set(100);
int newValue = box.get();  // 100
```

### getAndSet - Get + Set atomicamente

```java
Box<String> box = Box.atomic("old");
String previous = box.getAndSet("new");
// previous = "old"
// box.get() = "new"
```

### isNull - Verificar se é nulo

```java
Box<String> box = Box.atomic(null);
if (box.isNull()) {
    box.set("not null anymore");
}
```

### toOptional - Converter para Optional

```java
Box<String> box = Box.atomic("value");
Optional<String> opt = box.toOptional();  // Optional.of("value")

Box<String> empty = Box.atomic(null);
Optional<String> optEmpty = empty.toOptional();  // Optional.empty()
```

## Operações Avançadas

### compareAndSet - Atualização condicional

```java
Box<Integer> version = Box.atomic(1);

// Só atualiza se o valor atual for 1
if (version.compareAndSet(1, 2)) {
    System.out.println("Versão atualizada");
} else {
    System.out.println("Versão já foi modificada");
}
```

### getAndUpdate - Transform e retorna valor antigo

```java
Box<Integer> counter = Box.atomic(10);

// Incrementa e retorna o valor anterior
int previous = counter.getAndUpdate(n -> n + 1);
// previous = 10
// counter.get() = 11
```

### updateAndGet - Transform e retorna novo valor

```java
Box<Integer> counter = Box.atomic(10);

// Incrementa e retorna o novo valor
int newValue = counter.updateAndGet(n -> n + 1);
// newValue = 11
// counter.get() = 11
```

### update - Transform sem retornar valor

```java
Box<List<String>> items = Box.atomic(new ArrayList<>());

items.update(list -> {
    list.add("item1");
    list.add("item2");
    return list;
});
```

### getOrElse - Get com valor padrão

```java
Box<String> box = Box.atomic(null);
String value = box.getOrElse("default");  // "default"

Box<String> notNull = Box.atomic("hello");
String value2 = notNull.getOrElse("default");  // "hello"
```

### ifPresent - Callback se não nulo

```java
Box<User> user = Box.atomic(getUserData());

user.ifPresent(u -> {
    System.out.println("Usuário: " + u.getName());
    saveUserPreferences(u);
});
```

## Box Views

### view - Criar visão transformada (read-only)

```java
Box<User> user = Box.atomic(new User("John", 30));

// Criar uma visão que transforma o valor
Box<String> userName = user.view(User::getName);

String name = userName.get();  // "John"

// A visão é read-only
userName.set("Jane");  // Throws UnsupportedOperationException
```

Útil para expor apenas partes de um objeto:

```java
class UserProfile {
    private Box<User> user = Box.atomic(new User(...));
    
    // Expor apenas o nome, não permitindo modificação
    public Box<String> getUsername() {
        return user.view(User::getName);
    }
}
```

## Casos de Uso Comuns

### Contador compartilhado entre threads

```java
Box<Integer> pageViews = Box.atomic(0);

// Múltiplas threads incrementam
Runnable increment = () -> {
    for (int i = 0; i < 1000; i++) {
        pageViews.updateAndGet(n -> n + 1);
    }
};

Thread t1 = new Thread(increment);
Thread t2 = new Thread(increment);
t1.start();
t2.start();
t1.join();
t2.join();

System.out.println("Total: " + pageViews.get());  // 2000
```

### Flag compartilhada

```java
Box<Boolean> running = Box.volatileBox(true);

// Thread de trabalho
new Thread(() -> {
    while (running.get()) {
        doWork();
    }
}).start();

// Parar operação
running.set(false);
```

### Cache com update seguro

```java
Box<Cache> cache = Box.atomic(new Cache());

void refreshCache() {
    cache.update(c -> {
        c.load();
        return c;
    });
}

Data getData(String key) {
    return cache.get().find(key);
}
```

### Padrão de Lazy Initialization

```java
Box<DataLoader> loader = Box.atomic(null);

DataLoader getLoader() {
    return loader.getOrElse(() -> {
        DataLoader newLoader = new DataLoader();
        loader.set(newLoader);
        return newLoader;
    });
}
```

### State machine

```java
Box<State> state = Box.atomic(State.IDLE);

void transition(Event event) {
    state.update(current -> {
        State next = current.transitionOn(event);
        return next;
    });
}
```

### Configuração dinâmica

```java
Box<AppConfig> config = Box.atomic(loadConfig());

// Recarregar com segurança
void reloadConfig() {
    config.update(old -> {
        AppConfig newConfig = loadConfig();
        System.out.println("Configuração atualizada de " + old.version() + " para " + newConfig.version());
        return newConfig;
    });
}

// Consumir configuração
void doSomething() {
    AppConfig current = config.get();
    // usar current
}
```

## Escolhendo o tipo de Box

### Use AtomicBox quando:
- Precisa de operações atômicas (CAS)
- Múltiplas threads acessam
- Precisa de `compareAndSet()` ou `getAndUpdate()`
- Segurança é crítica

```java
Box<Integer> counter = Box.atomic(0);  // Seguro para múltiplas threads
```

### Use VolatileBox quando:
- Múltiplas threads acessam
- Operações simples de get/set
- Quer visibilidade sem overhead de locks

```java
Box<Boolean> flag = Box.volatileBox(false);  // Visibilidade garantida
```

### Use PlainBox quando:
- Apenas uma thread acessa
- Performance é crítica
- Sem preocupações de concorrência

```java
Box<Data> data = Box.plain(initialData);  // Sem sincronização
```

## Padrões de Design

### Observer Pattern com Box

```java
class ObservableBox<T> {
    private Box<T> value;
    private List<Consumer<T>> listeners = new ArrayList<>();
    
    void setValue(T newValue) {
        value.set(newValue);
        listeners.forEach(l -> l.accept(newValue));
    }
    
    void subscribe(Consumer<T> listener) {
        listeners.add(listener);
    }
}
```

### Builder Pattern com Box

```java
class MutableBuilder<T> {
    private Box<T> obj = Box.atomic(createDefault());
    
    MutableBuilder<T> with(Consumer<T> modifier) {
        modifier.accept(obj.get());
        return this;
    }
    
    T build() {
        return obj.get();
    }
}
```

## Limitações e Considerações

1. **Box não é imutável** - Usa-se para estado mutável compartilhado
2. **PlainBox não é thread-safe** - Apenas para single-threaded
3. **Views são read-only** - Não podem ser modificadas
4. **Sem bloqueio de leitura** - Leitura durante escrita pode retornar valor inconsistente (PlainBox)
5. **Performance** - AtomicBox tem overhead, use PlainBox se possível

## Melhores Práticas

1. **Minimize conteúdo do Box** - Guarde apenas o dado mutável necessário
2. **Prefira imutabilidade** - Use Box apenas quando necessário
3. **Use o tipo apropriado** - AtomicBox para multi-thread, PlainBox para single-thread
4. **Evite operações longas dentro de update** - Pode causar contenção
5. **Use views para API pública** - Limita modificações indevidas
6. **Teste race conditions** - Use stress tests com múltiplas threads
7. **Documente thread-safety** - Deixe claro qual tipo de Box está sendo usado

## Licença

Este código é parte do projeto Obsidian e está sob a licença Apache 2.0.
