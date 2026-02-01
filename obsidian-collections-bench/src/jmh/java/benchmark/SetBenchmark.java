package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import obsidian.collections.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks para OSet<E> e OSortedSet<E> - conjuntos imutáveis.
 * Compara com HashSet e TreeSet padrões do Java.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class SetBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    private OSet<Integer> oSet;
    private OSortedSet<Integer> oSortedSet;
    private Set<Integer> hashSet;
    private Set<Integer> treeSet;
    private List<Integer> elements;

    @Setup(Level.Trial)
    public void setup() {
        elements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            elements.add(i);
        }

        oSet = P.setCopyOf(elements);
        oSortedSet = P.sortedSetCopyOf(elements);
        hashSet = new HashSet<>(elements);
        treeSet = new TreeSet<>(elements);
    }

    @Benchmark
    public void oSetContains(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oSet.contains(i));
        }
    }

    @Benchmark
    public void hashSetContains(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(hashSet.contains(i));
        }
    }

    @Benchmark
    public void oSortedSetContains(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oSortedSet.contains(i));
        }
    }

    @Benchmark
    public void treeSetContains(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(treeSet.contains(i));
        }
    }

    @Benchmark
    public void oSetPlus(Blackhole bh) {
        OSet<Integer> s = oSet;
        for (int i = size; i < size + 100; i++) {
            s = s.plus(i);
        }
        bh.consume(s);
    }

    @Benchmark
    public void hashSetAdd(Blackhole bh) {
        Set<Integer> s = new HashSet<>(hashSet);
        for (int i = size; i < size + 100; i++) {
            s.add(i);
        }
        bh.consume(s);
    }

    @Benchmark
    public void oSortedSetPlus(Blackhole bh) {
        OSortedSet<Integer> s = oSortedSet;
        for (int i = size; i < size + 100; i++) {
            s = s.plus(i);
        }
        bh.consume(s);
    }

    @Benchmark
    public void treeSetAdd(Blackhole bh) {
        Set<Integer> s = new TreeSet<>(treeSet);
        for (int i = size; i < size + 100; i++) {
            s.add(i);
        }
        bh.consume(s);
    }

    @Benchmark
    public void oSetMinus(Blackhole bh) {
        OSet<Integer> s = oSet;
        for (int i = 0; i < Math.min(50, size); i++) {
            s = s.minus(i);
        }
        bh.consume(s);
    }

    @Benchmark
    public void hashSetRemove(Blackhole bh) {
        Set<Integer> s = new HashSet<>(hashSet);
        for (int i = 0; i < Math.min(50, size); i++) {
            s.remove(i);
        }
        bh.consume(s);
    }

    @Benchmark
    public void oSetIteration(Blackhole bh) {
        for (Integer i : oSet) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void hashSetIteration(Blackhole bh) {
        for (Integer i : hashSet) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void oSortedSetIteration(Blackhole bh) {
        for (Integer i : oSortedSet) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void treeSetIteration(Blackhole bh) {
        for (Integer i : treeSet) {
            bh.consume(i);
        }
    }
}
