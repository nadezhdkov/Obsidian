# 🚀 Obsidian Collections Benchmarks

Esta suíte de benchmarks usa **JMH (Java Microbenchmark Harness)** para medir e comparar o desempenho das coleções imutáveis do Obsidian com as coleções padrão do Java.

## 📊 Estrutura dos Benchmarks

### VectorBenchmark
Compara **OVector<E>** (vetor imutável com acesso O(1)) com **ArrayList** padrão:
- `oVectorGet` / `arrayListGet` - Acesso por índice
- `oVectorPlus` / `arrayListAdd` - Adição de elementos
- `oVectorWith` / `arrayListSet` - Substituição de elementos
- `oVectorIteration` / `arrayListIteration` - Iteração sequencial

**Principais Insights:**
- OVector: O(1) para get, O(1) amortizado para plus
- ArrayList: O(1) para get, O(1) amortizado para add
- OVector com mais overhead, mas imutável

### SetBenchmark
Compara **OSet<E>** e **OSortedSet<E>** com **HashSet** e **TreeSet**:
- `oSetContains` / `hashSetContains` - Lookup de elementos
- `oSetPlus` / `hashSetAdd` - Adição de elementos
- `oSetMinus` / `hashSetRemove` - Remoção de elementos
- `oSetIteration` / `hashSetIteration` - Iteração sequencial

**Principais Insights:**
- OSet (HAMT): O(log n) para operações
- HashSet: O(1) médio para operações
- OSortedSet (TreeSet): O(log n) com ordem garantida

### StackBenchmark
Compara **OStack<E>** com **Stack** e **Deque** padrões:
- `oStackPush` / `javaStackPush` / `dequeAddFirst` - Push
- `oStackPop` / `javaStackPop` / `dequeRemoveFirst` - Pop
- `oStackPeek` / `javaStackPeek` / `dequeGetFirst` - Peek
- `oStackIndexedAccess` / `javaStackIndexedAccess` - Acesso indexado

**Principais Insights:**
- OStack (cons list): O(1) para push/pop no início
- Stack/Deque: O(1) amortizado, menos overhead
- OStack melhor para padrões recursivos

### QueueBenchmark
Compara **OQueue<E>** com **LinkedList** e **ArrayDeque**:
- `oQueueEnqueue` / `linkedListQueueOffer` / `arrayDequeOffer` - Enqueue
- `oQueueDequeue` / `linkedListQueuePoll` / `arrayDequePoll` - Dequeue
- `oQueuePeek` / `linkedListQueuePeek` / `arrayDequePeek` - Peek
- `oQueueRemoveElement` / `linkedListQueueRemove` - Remover elemento

**Principais Insights:**
- OQueue (two-stack): O(1) amortizado
- LinkedList: O(1) para poll, O(n) para random access
- ArrayDeque: O(1) amortizado, muito rápido

### MapBenchmark
Compara **PMap<K,V>** e **PSortedMap<K,V>** com **HashMap** e **TreeMap**:
- `pMapGet` / `hashMapGet` - Lookup por chave
- `pMapGetOpt` - Lookup seguro retornando Optional
- `pMapContainsKey` / `hashMapContainsKey` - Verificação de chave
- `pMapPlus` / `hashMapPut` - Adição/atualização
- `pMapMinus` / `hashMapRemove` - Remoção
- `pMapIteration` / `hashMapIteration` - Iteração

**Principais Insights:**
- PMap (HAMT): O(log n) para operações
- HashMap: O(1) médio, melhor para pure performance
- PSortedMap: O(log n) com ordem garantida

### ComparisonBenchmark
Benchmarks de cenários do mundo real:
- **Criação e Cópia** - Construção de collections
- **Stream Processing** - Operações com streams
- **Transformação** - Modificação de elementos
- **Busca e Lookup** - Pesquisa de elementos
- **Acesso Aleatório** - Acesso pseudo-aleatório
- **Operações Sequenciais** - Processamento FIFO/LIFO
- **Construção Incremental** - Build gradual de collections

## 🏃 Como Executar

### Executar Todos os Benchmarks
```bash
./gradlew :obsidian-collections-bench:jmh
```

### Executar um Benchmark Específico
```bash
# VectorBenchmark
./gradlew :obsidian-collections-bench:jmh -Pjmh=VectorBenchmark

# SetBenchmark
./gradlew :obsidian-collections-bench:jmh -Pjmh=SetBenchmark

# StackBenchmark
./gradlew :obsidian-collections-bench:jmh -Pjmh=StackBenchmark

# QueueBenchmark
./gradlew :obsidian-collections-bench:jmh -Pjmh=QueueBenchmark

# MapBenchmark
./gradlew :obsidian-collections-bench:jmh -Pjmh=MapBenchmark

# ComparisonBenchmark
./gradlew :obsidian-collections-bench:jmh -Pjmh=ComparisonBenchmark
```

### Executar um Método de Benchmark Específico
```bash
./gradlew :obsidian-collections-bench:jmh -Pjmh=VectorBenchmark.oVectorGet
```

### Opções Avançadas

#### Aumentar o Tempo de Execução
```bash
./gradlew :obsidian-collections-bench:jmh -Pjmh.timeOnIteration=10
```

#### Mais Forks para Precisão
```bash
./gradlew :obsidian-collections-bench:jmh -Pjmh.forks=4
```

#### Incluir Relatório Detalhado
```bash
./gradlew :obsidian-collections-bench:jmh -Pjmh.resultFormat=json
```

## 📈 Entendendo os Resultados

JMH produz um relatório com as seguintes informações para cada benchmark:

```
Benchmark                                   Mode  Cnt      Score      Error  Units
ComparisonBenchmark.arrayDequeSequential    avgt   10     123.456 ±    4.567  ns/op
ComparisonBenchmark.oQueueSequential        avgt   10     145.678 ±    5.678  ns/op
```

- **Score**: Tempo médio em nanosegundos por operação
- **Error**: Margem de erro em nanosgundos
- **Mode**: `avgt` = Average Time (tempo médio)
- **Cnt**: Número de iterações

### Interpretação

1. **Score Menor = Melhor Desempenho**
   - 100 ns/op é muito mais rápido que 1000 ns/op

2. **Erro Relativo Importante**
   - ±10% é bom
   - ±5% é excelente
   - ±20% pode indicar JVM instável

3. **Tamanho (Param) Importante**
   - Performance pode ser linear, logarítmica, etc.
   - Comparar o padrão entre implementações

## 💡 Boas Práticas para Usar Benchmarks

### ✅ Faça
- Rodar pelo menos 3 vezes para ter consistência
- Permitir warm-up adequado (JIT compilation)
- Usar Blackhole para consumir resultados
- Aumentar o tamanho para dados mais realistas
- Rodar em máquina dedicada sem outros programas

### ❌ Não Faça
- Não confiar em uma única execução
- Não rodar com muitos programas abertos
- Não usar resultados de benchmark para decisões críticas sem análise
- Não comparar diferentes JVM versions sem ajuste

## 📊 Análise de Resultados

### Exemplo: VectorBenchmark com size=1000

```
Observação: Para acesso sequencial, ArrayList é mais rápido
Score OVector:    150 ns/op
Score ArrayList:  120 ns/op
Razão:            1.25x mais lento

Motivo: ArrayList é contíguo em memória, melhor cache locality
```

### Quando Usar Cada Estrutura

| Estrutura | Use quando | Evite quando |
|-----------|-----------|-------------|
| **OVector** | Imutabilidade é crítica | Performance pura é crítica |
| **ArrayList** | Mutabilidade frequente, acesso rápido | Concorrência, compartilhamento |
| **OSet** | Unicidade + imutabilidade | Performance pura |
| **HashSet** | Performance máxima com mutação | Concorrência |
| **OQueue** | Processamento FIFO imutável | Acesso aleatório |
| **ArrayDeque** | Fila rápida mútavel | Imutabilidade |
| **PMap** | Mapas imutáveis e seguros | Hotspots de performance |
| **HashMap** | Performance máxima em lookups | Multithreading simples |

## 🎯 Próximos Passos

1. **Rodar os benchmarks** na sua máquina
2. **Analisar os resultados** e identificar padrões
3. **Comparar com suas necessidades** (imutabilidade vs performance)
4. **Escolher a estrutura** mais apropriada
5. **Profile seu código** antes de otimizar prematuramente

## 📚 Referências

- [JMH Documentation](https://github.com/openjdk/jmh)
- [JMH User Guide](https://github.com/openjdk/jmh/wiki/FAQ)
- [Perfomance Testing Java Code](https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html)

## 🔗 Relacionado

- [Documentação de Collections](../collections/collections.md)
- [Fonte do Obsidian](https://github.com/nadezhdkov/Obsidian)
