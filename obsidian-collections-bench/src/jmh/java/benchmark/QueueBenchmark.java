package benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import obsidian.collections.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks para OQueue<E> - fila imutável com amortização.
 * Compara com LinkedList (que implementa Queue).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class QueueBenchmark {

    @Param({"10", "100", "1000"})
    public int size;

    private OQueue<Integer> oQueue;
    private Queue<Integer> linkedListQueue;
    private Queue<Integer> arrayDeque;

    @Setup(Level.Trial)
    public void setup() {
        oQueue = createOQueue(size);
        linkedListQueue = createLinkedListQueue(size);
        arrayDeque = createArrayDeque(size);
    }

    private OQueue<Integer> createOQueue(int n) {
        OQueue<Integer> q = Empty.queue();
        for (int i = 0; i < n; i++) {
            q = q.plus(i);
        }
        return q;
    }

    private Queue<Integer> createLinkedListQueue(int n) {
        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            q.offer(i);
        }
        return q;
    }

    private Queue<Integer> createArrayDeque(int n) {
        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            q.offer(i);
        }
        return q;
    }

    @Benchmark
    public void oQueueEnqueue(Blackhole bh) {
        OQueue<Integer> q = oQueue;
        for (int i = size; i < size + 100; i++) {
            q = q.plus(i);
        }
        bh.consume(q);
    }

    @Benchmark
    public void linkedListQueueOffer(Blackhole bh) {
        Queue<Integer> q = new LinkedList<>(linkedListQueue);
        for (int i = size; i < size + 100; i++) {
            q.offer(i);
        }
        bh.consume(q);
    }

    @Benchmark
    public void arrayDequeOffer(Blackhole bh) {
        Queue<Integer> q = new ArrayDeque<>(arrayDeque);
        for (int i = size; i < size + 100; i++) {
            q.offer(i);
        }
        bh.consume(q);
    }

    @Benchmark
    public void oQueueDequeue(Blackhole bh) {
        OQueue<Integer> q = oQueue;
        for (int i = 0; i < Math.min(50, size); i++) {
            q = q.minus();
        }
        bh.consume(q);
    }

    @Benchmark
    public void linkedListQueuePoll(Blackhole bh) {
        Queue<Integer> q = new LinkedList<>(linkedListQueue);
        for (int i = 0; i < Math.min(50, size); i++) {
            q.poll();
        }
        bh.consume(q);
    }

    @Benchmark
    public void arrayDequePoll(Blackhole bh) {
        Queue<Integer> q = new ArrayDeque<>(arrayDeque);
        for (int i = 0; i < Math.min(50, size); i++) {
            q.poll();
        }
        bh.consume(q);
    }

    @Benchmark
    public void oQueuePeek(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(oQueue.peek());
        }
    }

    @Benchmark
    public void linkedListQueuePeek(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(linkedListQueue.peek());
        }
    }

    @Benchmark
    public void arrayDequePeek(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(arrayDeque.peek());
        }
    }

    @Benchmark
    public void oQueueIteration(Blackhole bh) {
        for (Integer i : oQueue) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void linkedListQueueIteration(Blackhole bh) {
        for (Integer i : linkedListQueue) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void arrayDequeIteration(Blackhole bh) {
        for (Integer i : arrayDeque) {
            bh.consume(i);
        }
    }

    @Benchmark
    public void oQueueRemoveElement(Blackhole bh) {
        OQueue<Integer> q = oQueue;
        for (int i = 0; i < Math.min(50, size); i++) {
            q = q.minus(i);
        }
        bh.consume(q);
    }

    @Benchmark
    public void linkedListQueueRemove(Blackhole bh) {
        Queue<Integer> q = new LinkedList<>(linkedListQueue);
        for (int i = 0; i < Math.min(50, size); i++) {
            q.remove((Integer) i);
        }
        bh.consume(q);
    }
}
