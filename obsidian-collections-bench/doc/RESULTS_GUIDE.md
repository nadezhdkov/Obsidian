# 📊 Guia de Interpretação de Resultados

## Output do JMH

Um benchmark típico produz um resultado assim:

```
Benchmark                                   Mode  Cnt      Score       Error  Units
ComparisonBenchmark.oVectorGet              avgt   10    123.456 ±     4.567  ns/op
ComparisonBenchmark.arrayListGet            avgt   10     98.765 ±     3.456  ns/op
```

### Explicação

| Campo | Significado | Exemplo |
|-------|-----------|---------|
| **Benchmark** | Nome do teste | `ComparisonBenchmark.oVectorGet` |
| **Mode** | Modo de medição | `avgt` = Average Time |
| **Cnt** | Número de iterações | `10` iterações |
| **Score** | Tempo médio por operação | `123.456` ns |
| **Error** | Margem de erro | `±4.567` ns (3.7% de erro) |
| **Units** | Unidade de tempo | `ns/op` = nanosegundos por operação |

## Interpretação Prática

### 1. Score (Tempo)

**Quanto MENOR, MELHOR**

```
Score: 100 ns/op   → Muito rápido ✅
Score: 500 ns/op   → Rápido ✅
Score: 2000 ns/op  → Normal ⚠️
Score: 10000 ns/op → Lento ❌
```

### 2. Error (Margem de Erro)

**Quanto MENOR, MAIS CONFIÁVEL**

```
Error: ±2%   → Excelente (muito consistente) ✅✅✅
Error: ±5%   → Bom (consistente) ✅✅
Error: ±10%  → Aceitável (alguma variação) ✅
Error: ±20%  → Fraco (muita variação) ⚠️
Error: ±50%  → Não confiável ❌
```

**Cálculo**: `Error Relativo = (Error / Score) * 100`

```
Score: 100, Error: ±5
Erro Relativo = (5 / 100) * 100 = 5% ✅
```

## Comparação Entre Benchmarks

### OVector vs ArrayList

Exemplo de resultado real (fictício):
```
VectorBenchmark.oVectorGet              avgt   10    150.000 ±     3.000  ns/op
VectorBenchmark.arrayListGet            avgt   10    120.000 ±     2.400  ns/op
```

**Análise**:
- ArrayList é ~25% mais rápido
- `(150 - 120) / 120 * 100 = 25%`
- **Conclusão**: ArrayList tem menos overhead, mas OVector é imutável

### OSet vs HashSet

Exemplo:
```
SetBenchmark.oSetContains               avgt   10    250.000 ±     5.000  ns/op
SetBenchmark.hashSetContains            avgt   10    180.000 ±     3.600  ns/op
```

**Análise**:
- HashSet é ~39% mais rápido
- `(250 - 180) / 180 * 100 = 38.9%`
- **Motivo**: HAMT tem mais overhead que hash puro

### Padrão por Tamanho

Ao rodar com `@Param({"10", "100", "1000"})`:

```
VectorBenchmark.oVectorGet:size=10      avgt   10     50.000  ns/op
VectorBenchmark.oVectorGet:size=100     avgt   10     51.000  ns/op
VectorBenchmark.oVectorGet:size=1000    avgt   10     52.000  ns/op
```

**Análise**:
- Tempo praticamente igual
- **Conclusão**: OVector.get() é O(1) ✅

Vs ArrayList (que também é O(1)):
```
VectorBenchmark.arrayListGet:size=10    avgt   10     40.000  ns/op
VectorBenchmark.arrayListGet:size=100   avgt   10     40.500  ns/op
VectorBenchmark.arrayListGet:size=1000  avgt   10     41.000  ns/op
```

## Padrões Esperados

### OVector (Chunked)
```
Operação   | Complexidade | Score | Padrão
-----------|--------------|-------|--------
get(i)     | O(1)         | ~150  | Constante ✅
plus(e)    | O(1)*        | ~200  | Constante ✅
with(i,v)  | O(1)*        | ~300  | Constante ✅
iteration  | O(n)         | ~10/e | Linear
```

### OSet (HAMT)
```
Operação   | Complexidade | Score | Padrão
-----------|--------------|-------|--------
contains   | O(log n)     | ~250  | Logarítmico
plus       | O(log n)     | ~400  | Logarítmico
minus      | O(log n)     | ~500  | Logarítmico
iteration  | O(n)         | ~15/e | Linear
```

### OStack (Cons List)
```
Operação   | Complexidade | Score | Padrão
-----------|--------------|-------|--------
plus       | O(1)         | ~100  | Constante ✅
minus      | O(1)         | ~100  | Constante ✅
get(0)     | O(1)         | ~50   | Constante ✅
get(i)     | O(i)         | ~Var  | Linear
iteration  | O(n)         | ~10/e | Linear
```

### OQueue (Two-Stack)
```
Operação   | Complexidade | Score | Padrão
-----------|--------------|-------|--------
plus       | O(1)*        | ~150  | Constante ✅
minus      | O(1)*        | ~150  | Constante ✅
peek       | O(1)         | ~50   | Constante ✅
iteration  | O(n)         | ~12/e | Linear
```

## Red Flags 🚩

Se você ver:

### ❌ Score muito alto para O(1)
```
VectorBenchmark.oVectorGet:size=1000: 5000 ns/op
```
→ Possível problema de memory access ou GC

### ❌ Aumento linear com tamanho para O(1)
```
size=10:   100 ns/op
size=100:  500 ns/op
size=1000: 5000 ns/op
```
→ Algoritmo não é O(1), pode ser O(n)

### ❌ Erro muito alto
```
Score: 100 ±20 ns/op (20% de erro)
```
→ Ambiente instável, rerun em máquina quieta

### ❌ Variação estranha
```
Iteração 1: 100 ns/op
Iteração 2: 1000 ns/op
Iteração 3: 100 ns/op
```
→ GC happening, aumente heap ou iterations

## Dicas para Testes Válidos

### ✅ Ambiente Ideal

1. **Máquina dedicada**
   - Sem navegador, IDE, música, etc.
   - CPU não compartilhada
   - Memória não sob pressão

2. **JVM Settings**
   ```bash
   -Xms2G -Xmx2G  # Heap fixo
   -XX:+UseG1GC   # GC estável
   ```

3. **Iterações**
   ```
   Warmup: 5 iterações de 1s
   Measurement: 5 iterações de 1s
   Forks: 2+
   ```

4. **Validação**
   ```
   Erro < 10% → Resultado confiável ✅
   Erro 10-20% → Resultado aceitável
   Erro > 20% → Rerun ou revise setup
   ```

### ❌ Não Faça

```java
// ❌ ERRADO - Otimização demais
int total = 0;
for (int i = 0; i < 1000000; i++) {
    total += collection.get(i);
}
return total;
// Compiler pode otimizar tudo!

// ✅ CORRETO - Use Blackhole
for (int i = 0; i < 1000000; i++) {
    bh.consume(collection.get(i));
}
// JMH previne otimizações espúrias
```

## Exemplo Completo de Análise

### Código do Benchmark
```java
@Benchmark
public void oVectorPlusLoop(Blackhole bh) {
    OVector<Integer> v = oVector;
    for (int i = 0; i < 100; i++) {
        v = v.plus(size + i);
    }
    bh.consume(v);
}
```

### Resultados
```
VectorBenchmark.oVectorPlusLoop:size=10   avgt 10  2000.000 ± 40.000  ns/op
VectorBenchmark.oVectorPlusLoop:size=100  avgt 10  2050.000 ± 50.000  ns/op
VectorBenchmark.oVectorPlusLoop:size=1000 avgt 10  2100.000 ± 60.000  ns/op
```

### Interpretação

1. **Score relativo**
   - ~2000-2100 ns/op para 100 operações de plus
   - ~20-21 ns/op por operação
   - Muito bom! ✅

2. **Complexidade**
   - Score aumenta ligeiramente com size
   - Efeito: ~1% por 10x aumento
   - Provavelmente cache effects, não algoritmo
   - **Conclusão**: O(1) amortizado ✅

3. **Erro**
   - Máximo ±60 ns em 2100 ns
   - ~2.9% de erro
   - Excelente! ✅

4. **Decisão**
   - OVector.plus() é muito eficiente
   - Trade-off imutabilidade vale a pena
   - Recomendado para construção incremental ✅

## Salvando Resultados

### JSON Output
```bash
./gradlew :obsidian-collections-bench:jmh -Pjmh.resultFormat=json
```

Resultados salvos em:
```
build/results/jmh/results.json
```

### CSV para Análise
```bash
# Exportar JSON e converter para CSV
jq '.results[] | [.benchmark, .primaryMetric.score]' \
    results.json > results.csv
```

### Gráficos
```bash
# Usar ferramentas como:
# - JMH Visualizer: http://jmh.morethan.io/
# - CustomJMHVisualizer: https://github.com/melix/jmh-gradle-plugin
```

## Documentação Oficial

- [JMH FAQ](https://github.com/openjdk/jmh/wiki/FAQ)
- [JMH Samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [Perfomance Testing](https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html)
