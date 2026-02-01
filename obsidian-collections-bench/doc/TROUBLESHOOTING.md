# 🔧 Troubleshooting & FAQ

## Problemas Comuns

### ❌ "BUILD FAILED: Cannot find symbol"

**Problema**: Erro de compilação ao rodar benchmarks

```
error: cannot find symbol
symbol: class OVector
```

**Solução**:
```bash
# 1. Rebuild obsidian-collections primeiro
./gradlew :obsidian-collections:build

# 2. Clean e rebuild benchmarks
./gradlew :obsidian-collections-bench:clean
./gradlew :obsidian-collections-bench:compileJmhJava

# 3. Depois execute
./gradlew :obsidian-collections-bench:jmh
```

---

### ❌ "Module not found: obsidian-collections"

**Problema**: Projeto local não encontrado

```
Module 'obsidian-collections' not found in settings.gradle
```

**Solução**:
Verifique se `settings.gradle.kts` (raiz) contém:
```kotlin
include(":obsidian-collections")
include(":obsidian-collections-bench")
```

---

### ❌ Erros de Memory/OutOfMemory

**Problema**: "Exception in thread "main" java.lang.OutOfMemoryError"

**Solução**:
Aumente heap no build.gradle.kts:
```kotlin
@Fork(value = 2, jvmArgs = {"-Xms4G", "-Xmx4G"})  // Aumentar de 2G
```

Ou via linha de comando:
```bash
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh.jvmArgs="-Xms4G -Xmx4G"
```

---

### ❌ Erros de Tipo: "OVector cannot be converted to OSequence"

**Problema**: Type inference do compilador confuso

```
incompatible types: OSequence<Integer> cannot be converted to OVector<Integer>
```

**Solução**: Use cast explícito
```java
OVector<Integer> v = oVector;
v = (OVector<Integer>) v.plus(i);  // Add cast
```

---

### ❌ "Stack<Integer> s = new Stack<>(javaStack)"

**Problema**: Stack não tem constructor que aceita outro Stack

```
cannot infer type arguments for Stack<>
```

**Solução**: Use addAll
```java
Stack<Integer> s = new Stack<>();
s.addAll(javaStack);  // Correto
```

---

### ⚠️ Resultados Inconsistentes (Erro > 20%)

**Problema**: Variação muito grande nos resultados

```
VectorBenchmark.oVectorGet:size=10   avgt 10  150.000 ±  50.000  ns/op  (33% erro!)
```

**Causas possíveis**:
1. Outro programa usando CPU
2. GC happening durante teste
3. Ambiente instável

**Solução**:

```bash
# 1. Feche outros programas
# 2. Aumente iterations
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh.warmupIterations=10 \
    -Pjmh.measurementIterations=10

# 3. Aumente forks para isolamento
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh.forks=4

# 4. Mais tempo por iteração
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh.timeOnIteration=5
```

---

### ❌ "Cannot execute task ':obsidian-collections-bench:jmh'"

**Problema**: Plugin JMH não encontrado ou configurado incorretamente

```
Could not find method jmh() for arguments
```

**Solução**:
Verifique `build.gradle.kts`:

```kotlin
plugins {
    id("me.champeau.jmh") version "0.7.2"  // ✅ Necessário
}

dependencies {
    jmh("org.openjdk.jmh:jmh-core:1.37")  // ✅ Necessário
}
```

---

### ⚠️ Benchmarks Muito Lentos

**Problema**: Testes demorando mais que o esperado

Exemplo: 1 teste demora > 5 minutos

**Causas**:
1. Muitos forks
2. Muitas iterações
3. Tamanhos muito grandes
4. Máquina lenta

**Solução - Opção 1 (Rápido)**: Reduzir escopo
```bash
# Rodar apenas um método
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh=VectorBenchmark.oVectorGet
```

**Solução - Opção 2 (Padrão)**: Reduzir iterações
```bash
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh.warmupIterations=2 \
    -Pjmh.measurementIterations=2 \
    -Pjmh.forks=1
```

**Solução - Opção 3 (Completo)**: Rodar durante a noite
```bash
# Deixar rodando overnight com configuração padrão
./gradlew :obsidian-collections-bench:jmh &
```

---

### ⚠️ "WARNING: Unable to get Unsafe"

**Problema**: JMH aviso sobre Unsafe class

```
WARNING: Unable to get Unsafe. Some operations may be slow.
```

**Não é erro!** É apenas aviso. Performance ainda válida.

Para eliminar (Java 9+):
```bash
./gradlew :obsidian-collections-bench:jmh \
    --add-modules jdk.unsupported
```

---

## Performance Tips

### 🚀 Para Máquinas Rápidas (Desktop/Workstation)

```bash
# Máxima precisão
./run-benchmarks.sh all
# Ou específico
./gradlew :obsidian-collections-bench:jmh -Pjmh=ComparisonBenchmark
```

### 🐢 Para Máquinas Lentas (Laptop/VM)

```bash
# Rápido e ainda válido
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh=VectorBenchmark \
    -Pjmh.warmupIterations=2 \
    -Pjmh.measurementIterations=2 \
    -Pjmh.forks=1 \
    -Pjmh.timeOnIteration=100ms
```

### 🎯 Para CI/CD

```bash
# Rápido, reproduzível, confiável
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh.warmupIterations=3 \
    -Pjmh.measurementIterations=3 \
    -Pjmh.forks=2 \
    -Pjmh.resultFormat=json
```

---

## Limpeza

### Remover Resultados Anteriores

```bash
# Remove build directory
./gradlew :obsidian-collections-bench:clean

# Remove apenas resultados JMH
rm -rf obsidian-collections-bench/build/results
```

### Remover Tudo e Rebuild

```bash
# Nuclear option
./gradlew clean build
./gradlew :obsidian-collections-bench:compileJmhJava
```

---

## Validação

### Verificar Compilação

```bash
# Apenas compile
./gradlew :obsidian-collections-bench:compileJmhJava

# Se sucesso:
# BUILD SUCCESSFUL in Xs
```

### Verificar Benchmarks Registrados

```bash
# List benchmarks
./gradlew :obsidian-collections-bench:jmh -Pjmh=list

# Output esperado:
# VectorBenchmark.oVectorGet
# VectorBenchmark.arrayListGet
# ... (muitos mais)
```

### Teste Rápido

```bash
# Rodar apenas 1 iteração para validar
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh=VectorBenchmark.oVectorGet \
    -Pjmh.warmupIterations=1 \
    -Pjmh.measurementIterations=1 \
    -Pjmh.forks=1
```

---

## Debugging

### Ver Logs Detalhados

```bash
# Com informações de GC
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh.jvmArgs="-XX:+PrintGCDetails" \
    -Pjmh=VectorBenchmark \
    --info
```

### Salvar Logs em Arquivo

```bash
./gradlew :obsidian-collections-bench:jmh > benchmark.log 2>&1
tail -f benchmark.log  # Ver em tempo real
```

### Analisar Heap

```bash
# Dump heap durante execução
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh.jvmArgs="-Xmx2G -XX:+HeapDumpOnOutOfMemoryError"
```

---

## FAQ

### P: Quanto tempo levam os benchmarks?

**R**: Depende da máquina e configuração:
- VectorBenchmark: ~5-10 minutos
- SetBenchmark: ~5-10 minutos
- StackBenchmark: ~5-10 minutos
- QueueBenchmark: ~5-10 minutos
- MapBenchmark: ~7-15 minutos
- ComparisonBenchmark: ~10-20 minutos
- **Total**: ~40-80 minutos

Para acelerar: usar `-Pjmh=SpecificBenchmark`

---

### P: Posso parar no meio?

**R**: Sim, com `Ctrl+C`. Mas os resultados até agora serão perdidos.

---

### P: Onde ficam os resultados?

**R**: Em `obsidian-collections-bench/build/results/jmh/`

```
build/results/jmh/
├── results.json          ← Dados brutos
└── results.txt           ← Resumo
```

---

### P: Como comparar dois runs?

**R**: Use `-Pjmh.resultFormat=json` e salve com nome diferente:

```bash
# Run 1
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh=VectorBenchmark \
    -Pjmh.resultFormat=json \
    > results_run1.json

# Run 2 (depois de mudanças)
./gradlew :obsidian-collections-bench:jmh \
    -Pjmh=VectorBenchmark \
    -Pjmh.resultFormat=json \
    > results_run2.json

# Compare com ferramenta JSON diff
jdiff results_run1.json results_run2.json
```

---

### P: Posso modificar os benchmarks?

**R**: Sim! Mas:

1. Mantenha os nomes para rastreabilidade
2. Atualize documentação
3. Recompile com `./gradlew :obsidian-collections-bench:compileJmhJava`
4. Teste com `-Pjmh=ModifiedBenchmark` primeiro

---

### P: E se quiser adicionar novo benchmark?

**R**: 
1. Crie nova classe em `src/jmh/java/benchmark/MyNewBenchmark.java`
2. Siga o padrão existente (anotações, setup, etc.)
3. Compile: `./gradlew :obsidian-collections-bench:compileJmhJava`
4. Execute: `./gradlew :obsidian-collections-bench:jmh -Pjmh=MyNewBenchmark`

---

## Suporte

### Documentação Oficial

- [JMH GitHub](https://github.com/openjdk/jmh)
- [JMH FAQ](https://github.com/openjdk/jmh/wiki/FAQ)
- [JMH Samples](https://github.com/openjdk/jmh/tree/master/jmh-samples)

### Obsidian Collections

- [Projeto GitHub](https://github.com/nadezhdkov/Obsidian)
- [Issues](https://github.com/nadezhdkov/Obsidian/issues)
