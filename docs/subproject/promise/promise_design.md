# Promise Design Patterns

Common patterns and best practices for using Obsidian Promise.

## Table of Contents

1. [Basic Patterns](#basic-patterns)
2. [Error Handling Patterns](#error-handling-patterns)
3. [Composition Patterns](#composition-patterns)
4. [Resource Management](#resource-management)
5. [Cancellation Patterns](#cancellation-patterns)
6. [Testing Patterns](#testing-patterns)

---

## Basic Patterns

### Pattern: Sequential Operations

When you need to perform operations one after another:

```java
Promise<Result> result = loadUser(userId)
    .flatMap(user -> loadPreferences(user.getId()))
    .flatMap(prefs -> applyPreferences(prefs))
    .map(Result::new);
```

**When to use**: Operations depend on previous results.

### Pattern: Parallel Operations

When operations are independent:

```java
Promise<List<Data>> results = Promises.all(
    loadUserData(),
    loadConfiguration(),
    loadAnalytics()
);
```

**When to use**: Operations can run concurrently for better performance.

### Pattern: Fallback Chain

Try multiple sources in order:

```java
Promise<Data> data = loadFromCache()
    .recoverWith(e -> loadFromPrimary())
    .recoverWith(e -> loadFromBackup())
    .recover(e -> Data.empty());
```

**When to use**: Multiple fallback options available.

---

## Error Handling Patterns

### Pattern: Specific Error Recovery

Handle different errors differently:

```java
Promise<Data> result = operation()
    .catchError(TimeoutException.class, e -> retryOperation())
    .catchError(IOException.class, e -> useCache())
    .catchError(ValidationException.class, e -> useDefaults());
```

**When to use**: Different errors require different handling strategies.

### Pattern: Error Transformation

Convert low-level errors to domain errors:

```java
Promise<User> user = repository.findUser(id)
    .mapError(SQLException.class, e -> 
        new UserNotFoundException("User not found: " + id, e)
    );
```

**When to use**: Abstracting infrastructure errors from domain logic.

### Pattern: Error Logging

Log errors without swallowing them:

```java
Promise<Data> result = operation()
    .tap(data -> log.info("Success: {}", data))
    .recoverWith(error -> {
        log.error("Operation failed", error);
        return Promises.error(error); // Re-throw
    });
```

**When to use**: Need visibility into failures while propagating errors.

### Pattern: Retry with Exponential Backoff

```java
RetryPolicy policy = RetryPolicy.builder()
    .maxAttempts(5)
    .exponentialBackoff(Duration.ofSeconds(1), 2.0)
    .maxDelay(Duration.ofSeconds(30))
    .withJitter()
    .retryOn(IOException.class, TimeoutException.class)
    .build();

Promise<Data> result = unreliableOperation()
    .retry(policy);
```

**When to use**: Transient failures that might succeed on retry.

---

## Composition Patterns

### Pattern: Conditional Execution

Execute based on condition:

```java
Promise<Data> result = checkCondition()
    .flatMap(condition -> 
        condition ? expensiveOperation() : Promises.value(Data.empty())
    );
```

### Pattern: Batch Processing

Process items in batches:

```java
List<Promise<Result>> promises = items.stream()
    .map(item -> processItem(item))
    .collect(Collectors.toList());

Promise<List<Result>> results = Promises.all(promises);
```

### Pattern: Race with Timeout

```java
Promise<Data> result = Promises.race(
    actualOperation(),
    Promises.delay(Duration.ofSeconds(5))
        .flatMap(v -> Promises.error(new TimeoutException()))
);
```

**When to use**: Need custom timeout behavior.

### Pattern: Result Aggregation

Combine multiple results:

```java
Promise<Dashboard> dashboard = Promises.all(
        loadUserInfo(),
        loadStats(),
        loadNotifications()
    )
    .map(results -> Dashboard.builder()
        .user(results.get(0))
        .stats(results.get(1))
        .notifications(results.get(2))
        .build());
```

---

## Resource Management

### Pattern: Resource Cleanup

Ensure resources are released:

```java
Promise<Data> result = acquireResource()
    .flatMap(resource -> 
        processResource(resource)
            .finallyDo(() -> resource.close())
    );
```

**Best Practice**: Always use `finallyDo` for cleanup.

### Pattern: Connection Pool

```java
class DatabaseService {
    private final ConnectionPool pool;
    
    Promise<Result> query(String sql) {
        return Promises.async(() -> pool.acquire())
            .flatMap(conn -> 
                executeQuery(conn, sql)
                    .finallyDo(() -> pool.release(conn))
            );
    }
}
```

### Pattern: Timeout with Cleanup

```java
Promise<Data> result = Promises.defer(deferred -> {
    Resource resource = acquire();
    
    operation(resource)
        .timeout(Duration.ofSeconds(10))
        .finallyDo(() -> resource.release())
        .onSuccess(deferred::resolve)
        .onError(deferred::reject);
});
```

---

## Cancellation Patterns

### Pattern: User Cancellation

Allow users to cancel operations:

```java
class SearchService {
    private CancellationSource currentSearch;
    
    Promise<Results> search(String query) {
        // Cancel previous search
        if (currentSearch != null) {
            currentSearch.cancel();
        }
        
        currentSearch = CancellationSource.create();
        
        return performSearch(query, currentSearch.token())
            .finallyDo(() -> currentSearch = null);
    }
}
```

### Pattern: Linked Cancellation

Cancel dependent operations:

```java
CancellationSource parent = CancellationSource.create();
CancellationSource child = CancellationSource.createLinked(parent.token());

Promise<Data> parentOp = operation1(parent.token());
Promise<Data> childOp = operation2(child.token());

// Cancelling parent also cancels child
parent.cancel();
```

### Pattern: Timeout as Cancellation

```java
CancellationSource source = CancellationSource.create();

Promise<Data> operation = performWork(source.token());

Promises.delay(Duration.ofSeconds(30))
    .onSuccess(v -> source.cancel("Timeout"));
```

---

## Testing Patterns

### Pattern: Testing Async Code

```java
@Test
void shouldProcessDataSuccessfully() throws Throwable {
    // Given
    Promise<Data> promise = service.loadData(123);
    
    // When
    Data result = promise.get(Duration.ofSeconds(5));
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(123);
}
```

### Pattern: Testing Error Handling

```java
@Test
void shouldHandleErrorGracefully() {
    // Given
    service.setFailureMode(true);
    
    // When
    Promise<Data> promise = service.loadData(123)
        .recover(e -> Data.empty());
    
    // Then
    assertThat(promise.get()).isEqualTo(Data.empty());
}
```

### Pattern: Testing Cancellation

```java
@Test
void shouldCancelOperation() {
    // Given
    CancellationSource source = CancellationSource.create();
    AtomicBoolean cancelled = new AtomicBoolean(false);
    
    // When
    Promise<Data> promise = longOperation(source.token())
        .onCancelled(() -> cancelled.set(true));
    
    source.cancel();
    
    // Then
    assertThatThrownBy(() -> promise.get())
        .hasCauseInstanceOf(CancellationException.class);
    assertThat(cancelled.get()).isTrue();
}
```

### Pattern: Testing Timeouts

```java
@Test
void shouldTimeoutSlowOperation() {
    // Given
    Promise<Data> promise = slowOperation()
        .timeout(Duration.ofMillis(100));
    
    // Then
    assertThatThrownBy(() -> promise.get())
        .hasCauseInstanceOf(TimeoutException.class);
}
```

---

## Advanced Patterns

### Pattern: Circuit Breaker

```java
class CircuitBreaker {
    private AtomicInteger failures = new AtomicInteger(0);
    private static final int THRESHOLD = 5;
    
    Promise<Data> execute(Supplier<Promise<Data>> operation) {
        if (failures.get() >= THRESHOLD) {
            return Promises.error(new CircuitOpenException());
        }
        
        return operation.get()
            .tap(data -> failures.set(0))
            .recoverWith(error -> {
                failures.incrementAndGet();
                return Promises.error(error);
            });
    }
}
```

### Pattern: Rate Limiting

```java
class RateLimiter {
    private final Semaphore semaphore;
    
    <T> Promise<T> execute(Supplier<Promise<T>> operation) {
        return Promises.async(() -> {
            semaphore.acquire();
            return null;
        })
        .flatMap(v -> operation.get())
        .finallyDo(semaphore::release);
    }
}
```

### Pattern: Caching

```java
class CachedService {
    private final Map<String, Promise<Data>> cache = new ConcurrentHashMap<>();
    
    Promise<Data> getData(String key) {
        return cache.computeIfAbsent(key, k -> 
            expensiveOperation(k)
                .tap(data -> scheduleEviction(k))
        );
    }
}
```

### Pattern: Request Deduplication

```java
class DeduplicatedService {
    private final Map<String, Promise<Data>> pending = new ConcurrentHashMap<>();
    
    Promise<Data> fetch(String key) {
        return pending.computeIfAbsent(key, k -> 
            actualFetch(k)
                .finallyDo(() -> pending.remove(k))
        );
    }
}
```

---

## Anti-Patterns

### ❌ Blocking on Every Promise

```java
// Bad: Defeats the purpose of async
Data d1 = promise1.get();
Data d2 = promise2.get();
Data d3 = promise3.get();
```

```java
// Good: Compose promises
Promise<Result> result = Promises.all(promise1, promise2, promise3)
    .map(results -> combine(results));
```

### ❌ Swallowing Errors

```java
// Bad: Silent failure
promise.catchError(e -> defaultValue());
```

```java
// Good: Log before recovering
promise
    .tap(data -> log.info("Success"))
    .recoverWith(e -> {
        log.error("Failed", e);
        return Promises.value(defaultValue());
    });
```

### ❌ Creating Promise Inside Promise

```java
// Bad: Nested promises
promise.map(data -> Promises.async(() -> process(data)));
```

```java
// Good: Use flatMap
promise.flatMap(data -> Promises.async(() -> process(data)));
```

---

## Summary

- Use **sequential composition** (`flatMap`) when operations depend on each other
- Use **parallel composition** (`all`, `race`) for independent operations
- Always handle errors explicitly
- Use `finallyDo` for resource cleanup
- Prefer composition over blocking
- Test both success and failure paths
- Use cancellation tokens for long-running operations

These patterns will help you write clean, maintainable, and robust asynchronous code with Obsidian Promise.