# 📦 Obsidian Collections - Documentação Completa

## Índice

1. [Visão Geral](#visão-geral)
2. [Princípios Fundamentais](#princípios-fundamentais)
3. [Estruturas de Dados](#estruturas-de-dados)
4. [API Core](#api-core)
5. [Collections Persistentes](#collections-persistentes)
6. [Collections Sequenciais](#collections-sequenciais)
7. [Maps Persistentes](#maps-persistentes)
8. [Construtores e Factory Methods](#construtores-e-factory-methods)
9. [Exemplos Práticos](#exemplos-práticos)
10. [Performance](#performance)
11. [Boas Práticas](#boas-práticas)

---

## Visão Geral

**Obsidian Collections** é uma biblioteca de estruturas de dados imutáveis e funcionais para Java, inspirada em linguagens de programação funcional como Scala e Clojure. Diferentemente das collections padrão do Java que são mutáveis por natureza, Obsidian Collections oferece:

- ✅ **Imutabilidade Total**: Uma vez criada, uma collection não pode ser modificada
- ✅ **Operações Seguras**: Alterações retornam novas instâncias, mantendo a original intacta
- ✅ **Thread-Safe**: Sem necessidade de sincronização
- ✅ **Análise Estática**: Melhor suporte para análise de código e refatoração
- ✅ **Composição Funcional**: API fluente e funcional
- ✅ **Performance Otimizada**: Uso de estruturas avançadas como Hash Array Mapped Trie (HAMT)

### Quando Usar

- Programação funcional e reativa
- Aplicações multi-thread sem sincronização manual
- Análise e refatoração de código
- Sistemas que requerem auditoria de mudanças
- Processamento de dados imutável

---

## Princípios Fundamentais

### Imutabilidade

Todas as operações que modificam uma collection retornam uma **nova instância**:

```java
OVector<String> v1 = P.vectorOf("a", "b");
OVector<String> v2 = v1.plus("c");  // Retorna novo vetor

System.out.println(v1);  // [a, b]
System.out.println(v2);  // [a, b, c]
```

### Métodos Descontinuados

Os métodos mutáveis das interfaces padrão do Java são marcados como `@Deprecated`:

```java
OVector<String> v = P.vectorOf("a", "b");
// v.add("c");        // ❌ Não compila (deprecated)
// v.set(0, "x");     // ❌ Não compila (deprecated)
// v.remove(0);       // ❌ Não compila (deprecated)
v = v.plus("c");       // ✅ Forma correta
```

### Sem Null

Nenhuma estrutura de dados aceita elementos `null`:

```java
OVector<String> v = P.vectorOf("a", "b");
// v.plus(null);  // ❌ NullPointerException

// Use Optional para valores opcionais
Optional<String> opt = Optional.of("valor");
```

---

## Estruturas de Dados

### Hierarquia de Classes

```
OCollection<E> (extends Collection<E>)
├── OSequence<E> (extends List<E>)
│   ├── OStack<E>
│   └── OVector<E>
├── OQueue<E> (extends Queue<E>)
└── OSet<E> (extends Set<E>)
    └── OSortedSet<E> (extends NavigableSet<E>)

Map<K,V>
├── PMap<K,V>
└── PSortedMap<K,V> (extends NavigableMap<K,V>)
```

---

## API Core

### OCollection<E>

Interface base para todas as collections. Estende `Collection<E>` do Java padrão.

**Métodos Principais:**

| Método | Descrição | Retorno |
|--------|-----------|---------|
| `plus(E e)` | Adiciona um elemento | `OCollection<E>` |
| `plusAll(Collection<? extends E> list)` | Adiciona múltiplos elementos | `OCollection<E>` |
| `minus(Object e)` | Remove a primeira ocorrência | `OCollection<E>` |
| `minusAll(Collection<?> list)` | Remove múltiplos elementos | `OCollection<E>` |

**Exemplo:**

```java
OSet<Integer> nums = Empty.set();
nums = nums.plus(1);
nums = nums.plus(2);
nums = nums.plus(3);
// nums = {1, 2, 3}

OSet<Integer> updated = nums.minus(2);
// updated = {1, 3}
// nums ainda = {1, 2, 3}
```

---

## Collections Persistentes

### OSet<E> - Conjuntos Únicos

Uma collection não ordenada sem duplicatas.

**Implementação**: `HashTrieOSet` (baseada em HAMT)

**Características:**
- ✅ Sem duplicatas
- ✅ Sem ordem garantida
- ✅ O(log n) para add/remove/lookup
- ✅ Compartilhamento de estrutura entre versões

**Exemplo:**

```java
// Criação
OSet<String> frutas = P.setOf("maçã", "banana", "laranja");

// Operações
OSet<String> frutas2 = frutas.plus("morango");      // Adiciona
OSet<String> frutas3 = frutas2.minus("banana");     // Remove
OSet<String> frutas4 = frutas3.minusAll(
    Arrays.asList("maçã", "laranja")
);

// Queries
System.out.println(frutas.size());                   // 3
System.out.println(frutas.contains("maçã"));         // true
System.out.println(frutas.isEmpty());                // false

// Iteração
frutas.forEach(System.out::println);
```

**Métodos Estáticos:**

```java
OSet<Integer> empty = OSet.empty();
OSet<Integer> set = P.setOf(1, 2, 3);
OSet<Integer> copia = P.setCopyOf(Arrays.asList(1, 2, 3));
```

### OSortedSet<E> - Conjuntos Ordenados

Um conjunto que mantém elementos em ordem segundo um comparador.

**Implementação**: `TreeOSet` (baseada em Árvore Vermelha-Preta)

**Características:**
- ✅ Elementos mantidos em ordem
- ✅ O(log n) para operações
- ✅ Suporta NavigableSet
- ✅ Comparador customizável

**Exemplo:**

```java
// Com ordem natural
OSortedSet<Integer> nums = P.sortedSetOf(3, 1, 4, 1, 5);
System.out.println(nums);  // [1, 3, 4, 5]

// Com comparador customizado
OSortedSet<String> palavras = P.sortedSetOf(
    Comparator.comparingInt(String::length),
    "java", "python", "c", "javascript"
);
// Ordenado por tamanho: [c, java, python, javascript]

// Operações NavigableSet
OSortedSet<Integer> nums = P.sortedSetOf(1, 3, 5, 7, 9);
OSortedSet<Integer> subSet = nums.subSet(3, 7);     // [3, 5]
Integer menor = nums.first();                        // 1
Integer maior = nums.last();                         // 9
OSortedSet<Integer> desc = nums.descendingSet();    // [9, 7, 5, 3, 1]
```

---

## Collections Sequenciais

Collections que mantêm ordem e permitem acesso por índice.

### OSequence<E> - Lista Imutável Base

Interface que estende `List<E>` com operações imutáveis.

**Métodos Principais (além de OCollection):**

| Método | Descrição |
|--------|-----------|
| `with(int index, E value)` | Substitui elemento no índice |
| `plus(int index, E value)` | Insere elemento no índice |
| `plusAll(int index, Collection<? extends E> list)` | Insere múltiplos no índice |
| `minus(int index)` | Remove elemento no índice |
| `subList(int from, int to)` | Retorna subsequência |

**Exemplo:**

```java
OSequence<String> nomes = P.vectorOf("Alice", "Bob", "Carlos");

// Substituir
OSequence<String> atualizado = nomes.with(1, "Roberto");
// nomes   = [Alice, Bob, Carlos]
// atualizado = [Alice, Roberto, Carlos]

// Inserir em posição
OSequence<String> com_novo = nomes.plus(1, "Bruno");
// com_novo = [Alice, Bruno, Bob, Carlos]

// Remover por índice
OSequence<String> sem_um = nomes.minus(0);
// sem_um = [Bob, Carlos]

// Subsequência
OSequence<String> sub = nomes.subList(1, 3);
// sub = [Bob, Carlos]
```

### OVector<E> - Vetor de Acesso Rápido

Implementação altamente otimizada com acesso O(1) a qualquer elemento.

**Implementação**: `ChunkedOVector` (vetores chunked com 32 elementos por chunk)

**Características:**
- ✅ Acesso O(1) a qualquer índice
- ✅ Operações estruturais O(1) amortizado
- ✅ Estrutura compacta e eficiente em memória
- ✅ Perfeito para acesso aleatório frequente

**Exemplo:**

```java
// Criação
OVector<Integer> numeros = P.vectorOf(10, 20, 30, 40, 50);

// Acesso rápido
System.out.println(numeros.get(2));        // 30 (O(1))

// Operações
OVector<Integer> com_novo = numeros.plus(60);
OVector<Integer> atualizado = numeros.with(1, 25);

// Iteração
numeros.stream()
    .filter(n -> n > 25)
    .forEach(System.out::println);          // 30, 40, 50

// Transformação
OVector<String> textos = numeros.stream()
    .map(String::valueOf)
    .collect(Collectors.toCollection(
        () -> Empty.vector(),
        OVector::plus
    ));
```

**Factory Methods:**

```java
OVector<String> empty = Empty.vector();
OVector<String> vec = P.vectorOf("a", "b", "c");
OVector<String> copia = P.vectorCopyOf(Arrays.asList("a", "b", "c"));
```

### OStack<E> - Pilha Imutável

Estrutura baseada em lista encadeada (cons list) otimizada para operações no início.

**Implementação**: `ConsOStack`

**Características:**
- ✅ O(1) para plus/minus no início
- ✅ O(n) para acesso aleatório
- ✅ Excelente para processamento recursivo
- ✅ Compartilhamento máximo de estrutura

**Exemplo:**

```java
// Criação
OStack<String> pilha = P.stackOf("base");

// Push (adiciona no topo)
OStack<String> p1 = pilha.plus("meio");
OStack<String> p2 = p1.plus("topo");
// p2 = [topo, meio, base]

// Pop (remove do topo)
OStack<String> p3 = p2.minus(0);
// p3 = [meio, base]

// Acesso
System.out.println(p2.get(0));             // topo
System.out.println(p2.get(p2.size()-1));   // base

// Útil para processamento recursivo
OStack<Integer> fib = calcularSequencia();
while (!fib.isEmpty()) {
    System.out.println(fib.get(0));
    fib = fib.minus(0);
}
```

### OQueue<E> - Fila Imutável

Estrutura otimizada para operações FIFO (First In, First Out).

**Implementação**: `AmortizedOQueue` (duas stacks com amortização)

**Características:**
- ✅ O(1) amortizado para enqueue/dequeue
- ✅ Excelente para processamento sequencial
- ✅ Baixo overhead de memória

**Exemplo:**

```java
// Criação
OQueue<String> fila = P.queueOf("primeiro");

// Enqueue (adiciona no final)
OQueue<String> f1 = fila.plus("segundo");
OQueue<String> f2 = f1.plus("terceiro");

// Peek (vê primeiro elemento)
System.out.println(f2.peek());             // primeiro

// Dequeue (remove primeiro)
OQueue<String> f3 = f2.minus();
// f3 não tem "primeiro"

// Remover elemento específico
OQueue<String> f4 = f2.minus("segundo");

// Iteração em ordem FIFO
f2.forEach(System.out::println);
// primeiro, segundo, terceiro
```

**Factory Methods:**

```java
OQueue<Integer> empty = Empty.queue();
OQueue<Integer> q = P.queueOf(1, 2, 3);
OQueue<Integer> copia = P.queueCopyOf(Arrays.asList(1, 2, 3));
```

---

## Maps Persistentes

### PMap<K, V> - Mapa Imutável

Mapa chave-valor imutável baseado em Hash Array Mapped Trie.

**Implementação**: `HashTriePMap`

**Características:**
- ✅ O(log n) para operações
- ✅ Sem chaves duplicadas
- ✅ Sem valores null
- ✅ Compartilhamento de estrutura

**Exemplo:**

```java
// Criação vazia
PMap<String, Integer> mapa = Empty.map();

// Adição de pares
PMap<String, Integer> m1 = mapa.plus("Alice", 30);
PMap<String, Integer> m2 = m1.plus("Bob", 25);
PMap<String, Integer> m3 = m2.plus("Carlos", 35);

// Lookup seguro com Optional
Optional<Integer> idade_alice = m3.getOpt("Alice");   // Optional[30]
Optional<Integer> idade_dave = m3.getOpt("Dave");     // Optional.empty

// Lookup direto
Integer idade = m3.get("Bob");                        // 25
Integer nao_existe = m3.get("Eve");                   // null

// Atualização
PMap<String, Integer> m4 = m3.plus("Alice", 31);    // Sobrescreve

// Remoção
PMap<String, Integer> m5 = m3.minus("Bob");

// Remoção múltipla
PMap<String, Integer> m6 = m3.minusAll(
    Arrays.asList("Bob", "Carlos")
);

// Iteração
m3.forEach((nome, idade) -> {
    System.out.println(nome + ": " + idade);
});

// Transformação
PMap<String, String> textos = m3.entrySet().stream()
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> e.getValue() + " anos",
        (a, b) -> a,
        () -> Empty.map()
    ));
```

**Factory Methods:**

```java
PMap<String, Integer> empty = Empty.map();
PMap<String, Integer> m1 = P.mapOf();
PMap<String, Integer> m2 = P.mapOf("a", 1);
PMap<String, Integer> m3 = P.mapOf("a", 1, "b", 2, "c", 3);
PMap<String, Integer> copia = P.mapCopyOf(
    Map.of("x", 10, "y", 20)
);
```

### PSortedMap<K, V> - Mapa Ordenado

Mapa que mantém chaves em ordem.

**Implementação**: `TreePMap` (Árvore Vermelha-Preta)

**Características:**
- ✅ Chaves mantidas em ordem
- ✅ O(log n) para operações
- ✅ Suporta NavigableMap
- ✅ Comparador customizável

**Exemplo:**

```java
// Com ordem natural
PSortedMap<Integer, String> mapa = P.sortedMapOf(
    3, "três", 1, "um", 4, "quatro", 1, "um"
);
// Chaves em ordem: [1, 3, 4]

// Com comparador customizado
PSortedMap<String, Integer> ranking = P.sortedMapOf(
    Comparator.reverseOrder(),
    "Python", 1,
    "Java", 2,
    "Go", 3
);
// Chaves em ordem reversa

// Operações NavigableMap
PSortedMap<Integer, String> m = P.sortedMapOf(
    1, "um", 3, "três", 5, "cinco", 7, "sete"
);

Map<Integer, String> subMapa = m.subMap(2, 6);
// {3=três, 5=cinco}

Integer primeira = m.firstKey();                    // 1
Integer ultima = m.lastKey();                       // 7
PSortedMap<Integer, String> desc = m.descendingMap();
```

---

## Construtores e Factory Methods

### Classe P - Builders Convenientes

A classe `P` fornece factory methods para criar collections facilmente.

#### Stacks

```java
// Variadic
OStack<String> s1 = P.stackOf("a", "b", "c");

// A partir de Collection
OStack<String> s2 = P.stackCopyOf(Arrays.asList("x", "y", "z"));

// Vazio
OStack<String> s3 = Empty.stack();
```

#### Queues

```java
// Variadic
OQueue<Integer> q1 = P.queueOf(1, 2, 3);

// A partir de Collection
OQueue<Integer> q2 = P.queueCopyOf(Arrays.asList(1, 2, 3));

// Vazio
OQueue<Integer> q3 = Empty.queue();
```

#### Vectors

```java
// Variadic
OVector<Double> v1 = P.vectorOf(1.0, 2.0, 3.0);

// A partir de Collection
OVector<Double> v2 = P.vectorCopyOf(Arrays.asList(1.0, 2.0));

// Vazio
OVector<Double> v3 = Empty.vector();
```

#### Sets

```java
// Variadic
OSet<String> set1 = P.setOf("a", "b", "c");

// A partir de Collection
OSet<String> set2 = P.setCopyOf(Arrays.asList("x", "y", "z"));

// Vazio
OSet<String> set3 = Empty.set();
```

#### Sets Ordenados

```java
// Com ordem natural
OSortedSet<Integer> ss1 = P.sortedSetOf(3, 1, 4, 1, 5);

// Com comparador
OSortedSet<Integer> ss2 = P.sortedSetOf(
    Comparator.reverseOrder(),
    3, 1, 4, 1, 5
);

// A partir de Collection com ordem natural
OSortedSet<String> ss3 = P.sortedSetCopyOf(
    Arrays.asList("zebra", "apple")
);

// A partir de Collection com comparador
OSortedSet<String> ss4 = P.sortedSetCopyOf(
    Comparator.comparingInt(String::length),
    Arrays.asList("java", "python", "c")
);
```

#### Maps

```java
// Vazio
PMap<String, Integer> m1 = P.mapOf();

// Um par
PMap<String, Integer> m2 = P.mapOf("a", 1);

// Múltiplos pares
PMap<String, Integer> m3 = P.mapOf("a", 1, "b", 2, "c", 3);

// A partir de Map
PMap<String, Integer> m4 = P.mapCopyOf(
    Map.of("x", 10, "y", 20)
);
```

#### Sorted Maps

```java
// Vazio com ordem natural
PSortedMap<Integer, String> sm1 = P.sortedMapOf();

// Vazio com comparador
PSortedMap<Integer, String> sm2 = P.sortedMapOf(
    Comparator.reverseOrder()
);

// Com pares e ordem natural
PSortedMap<Integer, String> sm3 = P.sortedMapOf(
    3, "três", 1, "um", 2, "dois"
);

// A partir de Map com ordem natural
PSortedMap<String, Integer> sm4 = P.sortedMapCopyOf(
    Map.of("a", 1, "b", 2)
);

// A partir de Map com comparador
PSortedMap<String, Integer> sm5 = P.sortedMapCopyOf(
    Comparator.reverseOrder(),
    Map.of("a", 1, "b", 2)
);
```

### Classe Empty - Constructores Vazios

Fornece construtores para collections vazias:

```java
OStack<E> stack = Empty.stack();
OQueue<E> queue = Empty.queue();
OVector<E> vector = Empty.vector();
OSet<E> set = Empty.set();
OSortedSet<E> sortedSet = Empty.sortedSet();
OSortedSet<E> sortedSet = Empty.sortedSet(comparator);
PMap<K, V> map = Empty.map();
PSortedMap<K, V> sortedMap = Empty.sortedMap();
PSortedMap<K, V> sortedMap = Empty.sortedMap(comparator);
```

### Helpers para Resolução de Conflitos em Maps

```java
// Falha ao encontrar chave duplicada
BinaryOperator<String> failOnDuplicate = P.failOnDuplicateKeys();

// Mantém o primeiro valor
BinaryOperator<String> first = P.keepFirst();

// Mantém o último valor
BinaryOperator<String> last = P.keepLast();
```

---

## Exemplos Práticos

### Exemplo 1: Processamento de Dados de Usuários

```java
record User(int id, String name, int age) {}

// Dados iniciais
OVector<User> users = P.vectorOf(
    new User(1, "Alice", 28),
    new User(2, "Bob", 34),
    new User(3, "Carlos", 25)
);

// Adicionar novo usuário
OVector<User> updated = users.plus(new User(4, "Diana", 31));

// Filtrar maiores de idade
OVector<User> maiores = users.stream()
    .filter(u -> u.age() >= 30)
    .collect(Collectors.toCollection(
        () -> Empty.vector(),
        OVector::plus
    ));

// Criar mapa nome -> idade
PMap<String, Integer> idades = users.stream()
    .collect(Collectors.toMap(
        User::name,
        User::age,
        (a, b) -> a,
        () -> Empty.map()
    ));
```

### Exemplo 2: Sistema de Configuração

```java
// Configurações imutáveis
PMap<String, String> config = P.mapOf(
    "app.name", "MeuApp",
    "app.version", "1.0.0",
    "db.host", "localhost",
    "db.port", "5432"
);

// Função que acessa config
String getNomeApp() {
    return config.get("app.name");
}

// Criar nova config com override
PMap<String, String> prodConfig = config
    .minus("db.host")
    .plus("db.host", "prod.db.com");

// Iterar configurações
config.forEach((key, value) -> {
    System.out.println(key + " = " + value);
});
```

### Exemplo 3: Processamento de Eventos

```java
record Event(String type, long timestamp, String data) {}

// Fila de eventos a processar
OQueue<Event> eventQueue = P.queueOf(
    new Event("LOGIN", System.currentTimeMillis(), "user1"),
    new Event("CLICK", System.currentTimeMillis(), "button1")
);

// Processar evento
OQueue<Event> processQueue(OQueue<Event> queue) {
    if (queue.size() == 0) return queue;
    
    Event current = queue.peek();
    handleEvent(current);
    
    return queue.minus();  // Remove primeiro
}

// Usar recursivamente
OQueue<Event> resultado = processQueue(eventQueue);
```

### Exemplo 4: Histórico de Mudanças

```java
// Manter histórico de mudanças de documento
class DocumentHistory {
    private OStack<String> versions = Empty.stack();
    
    public void addVersion(String content) {
        versions = versions.plus(content);
    }
    
    public String getLatest() {
        return versions.isEmpty() ? "" : versions.get(0);
    }
    
    public String getVersion(int index) {
        return versions.get(index);
    }
    
    public void undo() {
        if (!versions.isEmpty()) {
            versions = versions.minus(0);
        }
    }
}
```

### Exemplo 5: Índices e Pesquisa

```java
record Product(int id, String name, double price, String category) {}

// Criar índices para busca rápida
PMap<String, OSortedSet<Product>> byCategory = P.mapOf();

// Adicionar produto
Product p = new Product(1, "Laptop", 1500.0, "Eletrônicos");

OSortedSet<Product> products = byCategory.getOpt("Eletrônicos")
    .orElse(Empty.sortedSet())
    .plus(p);

byCategory = byCategory.plus("Eletrônicos", products);

// Pesquisar por categoria
OSortedSet<Product> eletronicos = byCategory.get("Eletrônicos");
```

### Exemplo 6: Graph Traversal

```java
// Grafo representado como mapa de adjacências
PMap<String, OSet<String>> graph = P.mapOf(
    "A", P.setOf("B", "C"),
    "B", P.setOf("C", "D"),
    "C", P.setOf("D"),
    "D", P.setOf()
);

// BFS
void bfs(String start) {
    OSet<String> visited = Empty.set();
    OQueue<String> queue = P.queueOf(start);
    
    while (queue.size() > 0) {
        String node = queue.peek();
        queue = queue.minus();
        
        if (visited.contains(node)) continue;
        visited = visited.plus(node);
        
        System.out.println(node);
        
        OSet<String> neighbors = graph.get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    queue = queue.plus(neighbor);
                }
            }
        }
    }
}
```

---

## Performance

### Complexidade de Tempo

| Operação | OStack | OQueue | OVector | OSet | PMap | PSortedMap |
|----------|--------|--------|---------|------|------|-----------|
| get(i) | O(n) | O(n) | O(1) | N/A | N/A | N/A |
| plus(e) | O(1) | O(1)* | O(1)* | O(log n) | O(log n) | O(log n) |
| minus(e) | O(1) | O(1)* | O(n) | O(log n) | O(log n) | O(log n) |
| contains | O(n) | O(n) | O(n) | O(log n) | O(log n) | O(log n) |

*Amortizado

### Overhead de Memória

- **OStack**: Muito alto (ponteiros)
- **OQueue**: Médio (duas stacks)
- **OVector**: Muito baixo (arrays)
- **OSet/PMap**: Médio (HAMT compartilhado)

### Quando Usar Cada Uma

| Estrutura | Use quando... |
|-----------|---------------|
| **OStack** | Acesso/modificação frequente no início |
| **OQueue** | Processamento FIFO sequencial |
| **OVector** | Acesso aleatório frequente |
| **OSet** | Uniqueness importante, ordem não importa |
| **OSortedSet** | Ordem é importante |
| **PMap** | Lookups rápidos de chave-valor |
| **PSortedMap** | Ordem de chaves é importante |

---

## Boas Práticas

### ✅ Faça

```java
// Encadear operações
OVector<Integer> v = Empty.vector()
    .plus(1)
    .plus(2)
    .plus(3);

// Usar Optional para lookups
Optional<String> valor = config.getOpt("chave");

// Reusar variáveis
OSet<String> set = P.setOf("a");
set = set.plus("b");
set = set.plus("c");

// Usar P.* para criar collections
OVector<String> v = P.vectorOf("a", "b", "c");

// Documentar imutabilidade em tipos
PMap<String, OVector<String>> dados = Empty.map();
```

### ❌ Não Faça

```java
// Não confiar em mutabilidade
OVector<Integer> v = P.vectorOf(1, 2);
v.plus(3);  // Não afeta v!

// Não usar métodos deprecated
OVector<String> v = P.vectorOf("a", "b");
// v.add("c");  // ❌ Deprecated

// Não passar null
OVector<String> v = P.vectorOf("a");
// v.plus(null);  // ❌ NullPointerException

// Não assumir mutabilidade compartilhada
OVector<Integer> v1 = P.vectorOf(1, 2);
OVector<Integer> v2 = v1.plus(3);
// v1 não muda!

// Não ignorar retorno de operações
OSet<String> set = P.setOf("a", "b");
set.plus("c");  // ❌ Resultado ignorado
set = set.plus("c");  // ✅ Correto
```

### Threading

```java
// Thread-safe por padrão
PMap<String, String> cache = Empty.map();

// Múltiplas threads podem ler simultaneamente
String valor = cache.get("chave");

// Atualizações criam novas instâncias
PMap<String, String> novoCache = cache.plus("chave", "valor");
// Necessário sincronizar o compartilhamento de referências
// se vários threads atualizam a mesma variável
```

### Conversão com Java Collections

```java
// De Java Collection para Obsidian
java.util.List<String> list = Arrays.asList("a", "b");
OVector<String> vec = P.vectorCopyOf(list);

// De Obsidian para Java Collection
OVector<String> vec = P.vectorOf("a", "b");
java.util.List<String> list = new ArrayList<>(vec);

// Usar com Streams
OVector<Integer> vec = P.vectorOf(1, 2, 3);
vec.stream()
    .filter(x -> x > 1)
    .forEach(System.out::println);
```

---

## Referência Rápida

### Imports

```java
import obsidian.collections.*;
```

### Factory Methods Básicos

```java
// Vazio
OStack<T> = Empty.stack();
OQueue<T> = Empty.queue();
OVector<T> = Empty.vector();
OSet<T> = Empty.set();
PMap<K,V> = Empty.map();

// Com valores
OStack<T> = P.stackOf(e1, e2, ...);
OQueue<T> = P.queueOf(e1, e2, ...);
OVector<T> = P.vectorOf(e1, e2, ...);
OSet<T> = P.setOf(e1, e2, ...);
PMap<K,V> = P.mapOf(k1, v1, k2, v2, ...);

// De Collection
OStack<T> = P.stackCopyOf(collection);
OQueue<T> = P.queueCopyOf(collection);
OVector<T> = P.vectorCopyOf(collection);
OSet<T> = P.setCopyOf(collection);
PMap<K,V> = P.mapCopyOf(map);
```

### Operações Comuns

```java
// Adicionar
collection.plus(elemento);
collection.plusAll(elements);

// Remover
collection.minus(elemento);
collection.minusAll(elements);

// Sequências específicas
sequence.with(index, value);
sequence.plus(index, value);
sequence.minus(index);

// Maps
map.plus(key, value);
map.minus(key);
map.getOpt(key);  // Retorna Optional
```

---

## Conclusão

Obsidian Collections oferece uma forma moderna e funcional de trabalhar com dados em Java, eliminando classes inteiras de bugs relacionados a compartilhamento mutable. Com uma API clara, performance otimizada e imutabilidade garantida em tempo de compilação, é a escolha ideal para programação funcional, sistemas concorrentes e código robusto.

Para mais informações, visite a [documentação oficial do Obsidian](https://github.com/nadezhdkov/Obsidian).
