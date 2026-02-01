package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import obsidian.collections.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks para PMap<K,V> e PSortedMap<K,V> - mapas imutáveis.
 * Compara com HashMap e TreeMap padrões do Java.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class MapBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    private OMap<Integer, String> oMap;
    private OSortedMap<Integer, String> oSortedMap;
    private Map<Integer, String> hashMap;
    private Map<Integer, String> treeMap;

    @Setup(Level.Trial)
    public void setup() {
        oMap = createPMap(size);
        oSortedMap = createPSortedMap(size);
        hashMap = createHashMap(size);
        treeMap = createTreeMap(size);
    }

    private OMap<Integer, String> createPMap(int n) {
        OMap<Integer, String> m = Empty.map();
        for (int i = 0; i < n; i++) {
            m = m.plus(i, "value_" + i);
        }
        return m;
    }

    private OSortedMap<Integer, String> createPSortedMap(int n) {
        OSortedMap<Integer, String> m = Empty.sortedMap();
        for (int i = 0; i < n; i++) {
            m = m.plus(i, "value_" + i);
        }
        return m;
    }

    private Map<Integer, String> createHashMap(int n) {
        Map<Integer, String> m = new HashMap<>();
        for (int i = 0; i < n; i++) {
            m.put(i, "value_" + i);
        }
        return m;
    }

    private Map<Integer, String> createTreeMap(int n) {
        Map<Integer, String> m = new TreeMap<>();
        for (int i = 0; i < n; i++) {
            m.put(i, "value_" + i);
        }
        return m;
    }

    @Benchmark
    public void pMapGet(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oMap.get(i));
        }
    }

    @Benchmark
    public void hashMapGet(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(hashMap.get(i));
        }
    }

    @Benchmark
    public void pSortedMapGet(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oSortedMap.get(i));
        }
    }

    @Benchmark
    public void treeMapGet(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(treeMap.get(i));
        }
    }

    @Benchmark
    public void pMapGetOpt(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oMap.getOpt(i));
        }
    }

    @Benchmark
    public void pMapContainsKey(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oMap.containsKey(i));
        }
    }

    @Benchmark
    public void hashMapContainsKey(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(hashMap.containsKey(i));
        }
    }

    @Benchmark
    public void pMapPlus(Blackhole bh) {
        OMap<Integer, String> m = oMap;
        for (int i = size; i < size + 100; i++) {
            m = m.plus(i, "value_" + i);
        }
        bh.consume(m);
    }

    @Benchmark
    public void hashMapPut(Blackhole bh) {
        Map<Integer, String> m = new HashMap<>(hashMap);
        for (int i = size; i < size + 100; i++) {
            m.put(i, "value_" + i);
        }
        bh.consume(m);
    }

    @Benchmark
    public void pSortedMapPlus(Blackhole bh) {
        OSortedMap<Integer, String> m = oSortedMap;
        for (int i = size; i < size + 100; i++) {
            m = m.plus(i, "value_" + i);
        }
        bh.consume(m);
    }

    @Benchmark
    public void treeMapPut(Blackhole bh) {
        Map<Integer, String> m = new TreeMap<>(treeMap);
        for (int i = size; i < size + 100; i++) {
            m.put(i, "value_" + i);
        }
        bh.consume(m);
    }

    @Benchmark
    public void pMapMinus(Blackhole bh) {
        OMap<Integer, String> m = oMap;
        for (int i = 0; i < Math.min(50, size); i++) {
            m = m.minus(i);
        }
        bh.consume(m);
    }

    @Benchmark
    public void hashMapRemove(Blackhole bh) {
        Map<Integer, String> m = new HashMap<>(hashMap);
        for (int i = 0; i < Math.min(50, size); i++) {
            m.remove(i);
        }
        bh.consume(m);
    }

    @Benchmark
    public void pMapIteration(Blackhole bh) {
        for (Map.Entry<Integer, String> entry : oMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void hashMapIteration(Blackhole bh) {
        for (Map.Entry<Integer, String> entry : hashMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void pSortedMapIteration(Blackhole bh) {
        for (Map.Entry<Integer, String> entry : oSortedMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }

    @Benchmark
    public void treeMapIteration(Blackhole bh) {
        for (Map.Entry<Integer, String> entry : treeMap.entrySet()) {
            bh.consume(entry.getKey());
            bh.consume(entry.getValue());
        }
    }
}
