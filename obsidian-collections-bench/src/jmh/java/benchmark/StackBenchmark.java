package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import obsidian.collections.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks para OStack<E> - pilha imutável baseada em cons list.
 * Compara com Collections.asStack (baseado em Vector) do Java.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class StackBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    private OStack<Integer> oStack;
    private Stack<Integer> javaStack;
    private Deque<Integer> deque;

    @Setup(Level.Trial)
    public void setup() {
        oStack = createOStack(size);
        javaStack = createJavaStack(size);
        deque = createDeque(size);
    }

    private OStack<Integer> createOStack(int n) {
        OStack<Integer> s = Empty.stack();
        for (int i = 0; i < n; i++) {
            s = s.plus(i);
        }
        return s;
    }

    private Stack<Integer> createJavaStack(int n) {
        Stack<Integer> s = new Stack<>();
        for (int i = 0; i < n; i++) {
            s.push(i);
        }
        return s;
    }

    private Deque<Integer> createDeque(int n) {
        Deque<Integer> d = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            d.push(i);
        }
        return d;
    }

    @Benchmark
    public void oStackPush(Blackhole bh) {
        OStack<Integer> s = oStack;
        for (int i = size; i < size + 100; i++) {
            s = s.plus(i);
        }
        bh.consume(s);
    }

    @Benchmark
    public void javaStackPush(Blackhole bh) {
        Stack<Integer> s = new Stack<>();
        for (int i = size; i < size + 100; i++) {
            s.push(i);
        }
        bh.consume(s);
    }

    @Benchmark
    public void dequeAddFirst(Blackhole bh) {
        Deque<Integer> d = new LinkedList<>(deque);
        for (int i = size; i < size + 100; i++) {
            d.addFirst(i);
        }
        bh.consume(d);
    }

    @Benchmark
    public void oStackPop(Blackhole bh) {
        OStack<Integer> s = oStack;
        for (int i = 0; i < Math.min(50, size); i++) {
            s = s.minus(0);
        }
        bh.consume(s);
    }

    @Benchmark
    public void javaStackPop(Blackhole bh) {
        Stack<Integer> s = new Stack<>();
        for (int i = 0; i < Math.min(50, size); i++) {
            s.pop();
        }
        bh.consume(s);
    }

    @Benchmark
    public void dequeRemoveFirst(Blackhole bh) {
        Deque<Integer> d = new LinkedList<>(deque);
        for (int i = 0; i < Math.min(50, size); i++) {
            d.removeFirst();
        }
        bh.consume(d);
    }

    @Benchmark
    public void oStackPeek(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oStack.get(0));
        }
    }

    @Benchmark
    public void javaStackPeek(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(javaStack.peek());
        }
    }

    @Benchmark
    public void dequeGetFirst(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(deque.getFirst());
        }
    }

    @Benchmark
    public void oStackIteration(Blackhole bh) {
        for (Integer i : oStack) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void javaStackIteration(Blackhole bh) {
        for (Integer i : javaStack) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void dequeIteration(Blackhole bh) {
        for (Integer i : deque) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void oStackIndexedAccess(Blackhole bh) {
        for (int i = 0; i < Math.min(100, size); i++) {
            bh.consume(oStack.get(i));
        }
    }

    @Benchmark
    public void javaStackIndexedAccess(Blackhole bh) {
        Stack<Integer> s = new Stack<>();
        s.addAll(javaStack);
        for (int i = 0; i < Math.min(100, size); i++) {
            bh.consume(s.get(i));
        }
    }
}
