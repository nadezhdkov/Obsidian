package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import obsidian.collections.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks para OVector<E> - vetor imutável com acesso O(1).
 * Compara com ArrayList padrão do Java.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class VectorBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    private OVector<Integer> oVector;
    private List<Integer> arrayList;

    @Setup(Level.Trial)
    public void setup() {
        oVector = createOVector(size);
        arrayList = createArrayList(size);
    }

    private OVector<Integer> createOVector(int n) {
        OVector<Integer> v = Empty.vector();
        for (int i = 0; i < n; i++) {
            v = (OVector<Integer>) v.plus(i);
        }
        return v;
    }

    private List<Integer> createArrayList(int n) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        return list;
    }

    @Benchmark
    public void oVectorGet(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oVector.get(i));
        }
    }

    @Benchmark
    public void arrayListGet(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(arrayList.get(i));
        }
    }

    @Benchmark
    public void oVectorPlus(Blackhole bh) {
        OVector<Integer> v = oVector;
        for (int i = 0; i < 100; i++) {
            v = (OVector<Integer>) v.plus(size + i);
        }
        bh.consume(v);
    }

    @Benchmark
    public void arrayListAdd(Blackhole bh) {
        List<Integer> list = new ArrayList<>(arrayList);
        for (int i = 0; i < 100; i++) {
            list.add(size + i);
        }
        bh.consume(list);
    }

    @Benchmark
    public void oVectorWith(Blackhole bh) {
        OVector<Integer> v = oVector;
        for (int i = 0; i < Math.min(100, size); i++) {
            v = (OVector<Integer>) v.with(i, -1);
        }
        bh.consume(v);
    }

    @Benchmark
    public void arrayListSet(Blackhole bh) {
        List<Integer> list = new ArrayList<>(arrayList);
        for (int i = 0; i < Math.min(100, size); i++) {
            list.set(i, -1);
        }
        bh.consume(list);
    }

    @Benchmark
    public void oVectorIteration(Blackhole bh) {
        for (Integer i : oVector) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void arrayListIteration(Blackhole bh) {
        for (Integer i : arrayList) {
            bh.consume(i);
        }
    }
}
