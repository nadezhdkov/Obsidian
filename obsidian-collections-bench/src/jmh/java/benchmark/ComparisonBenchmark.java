package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import obsidian.collections.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks de comparação entre diferentes estruturas de dados.
 * Testa cenários de uso comum em programas Java.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class ComparisonBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    private OVector<Integer> oVector;
    private OStack<Integer> oStack;
    private OQueue<Integer> oQueue;
    private OSet<Integer> oSet;
    private OMap<Integer, Integer> oMap;

    private List<Integer> arrayList;
    private Stack<Integer> javaStack;
    private Queue<Integer> arrayDeque;
    private Set<Integer> hashSet;
    private Map<Integer, Integer> hashMap;

    @Setup(Level.Trial)
    public void setup() {
        List<Integer> elements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            elements.add(i);
        }

        // Obsidian Collections
        oVector = P.vectorCopyOf(elements);
        oStack = P.stackCopyOf(elements);
        oQueue = P.queueCopyOf(elements);
        oSet = P.setCopyOf(elements);
        oMap = createPMap(elements);

        // Java Collections
        arrayList = new ArrayList<>(elements);
        javaStack = new Stack<>();
        javaStack.addAll(elements);
        arrayDeque = new ArrayDeque<>(elements);
        hashSet = new HashSet<>(elements);
        hashMap = createHashMap(elements);
    }

    private OMap<Integer, Integer> createPMap(List<Integer> elements) {
        OMap<Integer, Integer> m = Empty.map();
        for (int i = 0; i < elements.size(); i++) {
            m = m.plus(i, elements.get(i));
        }
        return m;
    }

    private Map<Integer, Integer> createHashMap(List<Integer> elements) {
        Map<Integer, Integer> m = new HashMap<>();
        for (int i = 0; i < elements.size(); i++) {
            m.put(i, elements.get(i));
        }
        return m;
    }

    // === CRÉATION ET COPIE ===

    @Benchmark
    public void oVectorCreationFromList(Blackhole bh) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        OVector<Integer> vec = P.vectorCopyOf(list);
        bh.consume(vec);
    }

    @Benchmark
    public void arrayListCreation(Blackhole bh) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        bh.consume(list);
    }

    // === STREAM PROCESSING ===

    @Benchmark
    public void oVectorStream(Blackhole bh) {
        long sum = oVector.stream()
                .filter(x -> x % 2 == 0)
                .mapToLong(Integer::longValue)
                .sum();
        bh.consume(sum);
    }

    @Benchmark
    public void arrayListStream(Blackhole bh) {
        long sum = arrayList.stream()
                .filter(x -> x % 2 == 0)
                .mapToLong(Integer::longValue)
                .sum();
        bh.consume(sum);
    }

    // === TRANSFORMAÇÃO E CÓPIA ===

    @Benchmark
    public void oVectorTransformation(Blackhole bh) {
        OVector<Integer> transformed = oVector;
        for (int i = 0; i < Math.min(20, size); i++) {
            transformed = (OVector<Integer>) transformed.with(i, -1);
        }
        bh.consume(transformed);
    }

    @Benchmark
    public void arrayListTransformation(Blackhole bh) {
        List<Integer> copy = new ArrayList<>(arrayList);
        for (int i = 0; i < Math.min(20, size); i++) {
            copy.set(i, -1);
        }
        bh.consume(copy);
    }

    // === BUSCA E LOOKUP ===

    @Benchmark
    public void pMapLookups(Blackhole bh) {
        long count = 0;
        for (int i = 0; i < size; i++) {
            if (oMap.containsKey(i)) {
                count += oMap.get(i);
            }
        }
        bh.consume(count);
    }

    @Benchmark
    public void hashMapLookups(Blackhole bh) {
        long count = 0;
        for (int i = 0; i < size; i++) {
            if (hashMap.containsKey(i)) {
                count += hashMap.get(i);
            }
        }
        bh.consume(count);
    }

    @Benchmark
    public void oSetContainsAll(Blackhole bh) {
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (oSet.contains(i)) {
                count++;
            }
        }
        bh.consume(count);
    }

    @Benchmark
    public void hashSetContainsAll(Blackhole bh) {
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (hashSet.contains(i)) {
                count++;
            }
        }
        bh.consume(count);
    }

    // === ACESSO ALEATÓRIO ===

    @Benchmark
    public void oVectorRandomAccess(Blackhole bh) {
        Random rand = new Random(42);
        long sum = 0;
        for (int i = 0; i < 100; i++) {
            int idx = rand.nextInt(size);
            sum += oVector.get(idx);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void arrayListRandomAccess(Blackhole bh) {
        Random rand = new Random(42);
        long sum = 0;
        for (int i = 0; i < 100; i++) {
            int idx = rand.nextInt(size);
            sum += arrayList.get(idx);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void oStackRandomAccess(Blackhole bh) {
        Random rand = new Random(42);
        long sum = 0;
        for (int i = 0; i < 100; i++) {
            int idx = rand.nextInt(size);
            sum += oStack.get(idx);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void javaStackRandomAccess(Blackhole bh) {
        Random rand = new Random(42);
        long sum = 0;
        for (int i = 0; i < 100; i++) {
            int idx = rand.nextInt(size);
            sum += javaStack.get(idx);
        }
        bh.consume(sum);
    }

    // === OPERAÇÕES SEQUENCIAIS ===

    @Benchmark
    public void oQueueSequentialProcessing(Blackhole bh) {
        OQueue<Integer> q = oQueue;
        long sum = 0;
        while (q.size() > 0) {
            Integer val = q.peek();
            if (val != null) {
                sum += val;
            }
            q = q.minus();
        }
        bh.consume(sum);
    }

    @Benchmark
    public void arrayDequeSequentialProcessing(Blackhole bh) {
        Queue<Integer> q = new ArrayDeque<>(arrayDeque);
        long sum = 0;
        while (!q.isEmpty()) {
            Integer val = q.peek();
            if (val != null) {
                sum += val;
            }
            q.poll();
        }
        bh.consume(sum);
    }

    // === CONSTRUÇÃO INCREMENTAL ===

    @Benchmark
    public void oVectorIncrementalBuild(Blackhole bh) {
        OVector<Integer> vec = Empty.vector();
        for (int i = 0; i < 50; i++) {
            vec = (OVector<Integer>) vec.plus(i);
        }
        bh.consume(vec);
    }

    @Benchmark
    public void arrayListIncrementalBuild(Blackhole bh) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add(i);
        }
        bh.consume(list);
    }

    @Benchmark
    public void pMapIncrementalBuild(Blackhole bh) {
        OMap<Integer, Integer> map = Empty.map();
        for (int i = 0; i < 50; i++) {
            map = map.plus(i, i * 2);
        }
        bh.consume(map);
    }

    @Benchmark
    public void hashMapIncrementalBuild(Blackhole bh) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            map.put(i, i * 2);
        }
        bh.consume(map);
    }
}
