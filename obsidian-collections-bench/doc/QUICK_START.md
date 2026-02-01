# 🚀 QUICK START GUIDE

## ⚡ 30 Segundos para Começar

```bash
cd /home/nadezh/Projects/Obsidian

# Compilar (5s)
./gradlew :obsidian-collections-bench:compileJmhJava

# Rodar benchmark (10 min)
./obsidian-collections-bench/run-benchmarks.sh vector

# Ver resultados em:
# build/results/jmh/results.json
```

## 📊 Benchmarks Disponíveis

```bash
./obsidian-collections-bench/run-benchmarks.sh [opção]

vector       → OVector vs ArrayList
set          → OSet/OSortedSet vs HashSet/TreeSet
stack        → OStack vs Stack/Deque
queue        → OQueue vs LinkedList/ArrayDeque
map          → PMap/PSortedMap vs HashMap/TreeMap
comparison   → Cenários reais (melhor overview)
all          → Todos (padrão, ~60-80 min)
help         → Mostra opções
```

## 📖 Documentação

- **README.md** - Visão geral
- **RESULTS_GUIDE.md** - Como ler resultados
- **TROUBLESHOOTING.md** - FAQ e soluções
- **collections/collections.md** - API completa

## ✅ O Que Você Tem

✓ 6 benchmarks compilados  
✓ ~79 métodos de teste  
✓ ~240+ combinações  
✓ 3 documentos de suporte  
✓ Script executável  
✓ Pronto para rodar

## 🎯 Próximos Passos

1. Rodar: `./obsidian-collections-bench/run-benchmarks.sh vector`
2. Ler: RESULTS_GUIDE.md
3. Analisar resultados
4. Comparar implementações

## 💡 Dicas

- Primeiro run: teste rápido com um benchmark
- Ambiente: feche outros programas
- Tempo: complete rodando overnight
- Análise: use JSON output para gráficos

**Tempo estimado**: 10 min (vector) a 80 min (all)

---

Status: ✅ BUILD SUCCESSFUL | Data: 2025-02-01 | Version: 1.0
