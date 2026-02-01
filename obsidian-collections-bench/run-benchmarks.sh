#!/bin/bash
# Script para executar benchmarks da Obsidian Collections

set -e

echo "🚀 Obsidian Collections - JMH Benchmarks"
echo "========================================"
echo ""

# Cores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

usage() {
    echo "Uso: $0 [opção]"
    echo ""
    echo "Opções:"
    echo "  all              Executar todos os benchmarks (padrão)"
    echo "  vector           Executar VectorBenchmark"
    echo "  set              Executar SetBenchmark"
    echo "  stack            Executar StackBenchmark"
    echo "  queue            Executar QueueBenchmark"
    echo "  map              Executar MapBenchmark"
    echo "  comparison       Executar ComparisonBenchmark"
    echo "  help             Mostrar esta mensagem"
    echo ""
}

run_benchmark() {
    local benchmark=$1
    if [ -z "$benchmark" ]; then
        benchmark_arg=""
    else
        benchmark_arg="-Pjmh=$benchmark"
    fi

    echo -e "${BLUE}Executando JMH Benchmarks${NC}"
    if [ -n "$benchmark" ]; then
        echo "Benchmark: $benchmark"
    fi
    echo ""

    ./gradlew :obsidian-collections-bench:jmh $benchmark_arg
}

# Default is "all"
BENCHMARK=${1:-all}

case $BENCHMARK in
    all)
        echo -e "${GREEN}Executando TODOS os benchmarks...${NC}\n"
        run_benchmark
        ;;
    vector)
        echo -e "${GREEN}Executando VectorBenchmark...${NC}\n"
        run_benchmark "VectorBenchmark"
        ;;
    set)
        echo -e "${GREEN}Executando SetBenchmark...${NC}\n"
        run_benchmark "SetBenchmark"
        ;;
    stack)
        echo -e "${GREEN}Executando StackBenchmark...${NC}\n"
        run_benchmark "StackBenchmark"
        ;;
    queue)
        echo -e "${GREEN}Executando QueueBenchmark...${NC}\n"
        run_benchmark "QueueBenchmark"
        ;;
    map)
        echo -e "${GREEN}Executando MapBenchmark...${NC}\n"
        run_benchmark "MapBenchmark"
        ;;
    comparison)
        echo -e "${GREEN}Executando ComparisonBenchmark...${NC}\n"
        run_benchmark "ComparisonBenchmark"
        ;;
    help|--help|-h)
        usage
        ;;
    *)
        echo "❌ Benchmark desconhecido: $BENCHMARK"
        echo ""
        usage
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}✅ Benchmarks completados!${NC}"
echo ""
echo "📊 Resultados também foram salvos em:"
echo "   build/reports/jmh/"
