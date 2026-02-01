# 1️⃣4️⃣ Padrões Práticos

## 🎯 Best Practices para JSON API

Nesta seção reunimos os melhores padrões e práticas para usar a API JSON do Obsidian em aplicações reais.

## 🏗️ Padrão 1: Repository Pattern

```java
public abstract class JsonRepository<T, ID> {
    
    protected final JsonMapper mapper;
    protected final Path dataFile;
    protected final TypeRef<List<T>> listType;
    
    public JsonRepository(Path dataFile, TypeRef<List<T>> listType) {
        this.mapper = Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
        this.dataFile = dataFile;
        this.listType = listType;
    }
    
    // Operações CRUD básicas
    public void save(T entity) {
        List<T> entities = findAll();
        entities.add(entity);
        persistAll(entities);
    }
    
    public List<T> findAll() {
        try {
            if (!Files.exists(dataFile)) {
                return new ArrayList<>();
            }
            return JsonFiles.read(dataFile, listType, mapper);
        } catch (JsonException e) {
            logger.error(\"Erro ao carregar entidades\", e);
            return new ArrayList<>();
        }
    }
    
    public void deleteAll() {
        try {
            Files.deleteIfExists(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void persistAll(List<T> entities) {
        try {
            Files.createDirectories(dataFile.getParent());
            JsonFiles.write(dataFile, entities, mapper);
        } catch (IOException e) {
            throw new RuntimeException(\"Erro ao persistir\", e);
        }
    }
    
    // Métodos abstratos
    protected abstract ID getId(T entity);
}

// Implementação específica
public class UserRepository extends JsonRepository<User, String> {
    
    public UserRepository() {
        super(Paths.get(\"data/users.json\"), TypeRef.listOf(User.class));
    }
    
    @Override
    protected String getId(User entity) {
        return entity.id;
    }
    
    public Optional<User> findById(String id) {
        return findAll().stream()
            .filter(u -> u.id.equals(id))
            .findFirst();
    }
    
    public void update(User user) {
        List<User> users = findAll();
        users.removeIf(u -> u.id.equals(user.id));
        users.add(user);
        persistAll(users);
    }
}

// Usar
UserRepository repo = new UserRepository();
repo.save(new User(\"1\", \"João\"));
repo.findById(\"1\").ifPresent(System.out::println);
```

## 🔧 Padrão 2: Service Layer

```java
@Service
public class UserService {
    
    private final UserRepository repository;
    private final JsonMapper mapper;
    
    @Autowired
    public UserService(UserRepository repository, JsonMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    public User createUser(String name, String email) {
        User user = new User();
        user.id = UUID.randomUUID().toString();
        user.name = name;
        user.email = email;
        user.createdAt = LocalDateTime.now();
        
        repository.save(user);
        return user;
    }
    
    public Optional<User> getUser(String id) {
        return repository.findById(id);
    }
    
    public List<User> getAllUsers() {
        return repository.findAll();
    }
    
    public void updateUser(User user) {
        repository.update(user);
    }
    
    public boolean deleteUser(String id) {
        List<User> users = repository.findAll();
        boolean removed = users.removeIf(u -> u.id.equals(id));
        if (removed) {
            repository.persistAll(users);
        }
        return removed;
    }
}
```

## 📡 Padrão 3: API Controller

```java
@RestController
@RequestMapping(\"/api/users\")
public class UserController {
    
    private final UserService service;
    private final JsonMapper mapper;
    
    @Autowired
    public UserController(UserService service, JsonMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    
    @GetMapping(\"/{id}\")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return service.getUser(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = service.createUser(user.name, user.email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping(\"/{id}\")
    public ResponseEntity<User> updateUser(
        @PathVariable String id,
        @RequestBody User user) {
        
        user.id = id;
        service.updateUser(user);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping(\"/{id}\")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        return service.deleteUser(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}
```

## 🔄 Padrão 4: Cache com Invalidação

```java
public class CachedRepository<T> {
    
    private final JsonRepository<T, ?> repository;
    private final long cacheTTL;
    private List<T> cache;
    private long lastLoad = 0;
    
    public CachedRepository(JsonRepository<T, ?> repository, long cacheTTLMillis) {
        this.repository = repository;
        this.cacheTTL = cacheTTLMillis;
    }
    
    public List<T> findAll() {
        long now = System.currentTimeMillis();
        
        // Verificar se cache expirou
        if (cache == null || (now - lastLoad) > cacheTTL) {
            cache = repository.findAll();
            lastLoad = now;
        }
        
        return new ArrayList<>(cache);  // Cópia para segurança
    }
    
    public void invalidate() {
        cache = null;
        lastLoad = 0;
    }
}

// Usar
CachedRepository<User> cachedRepo = new CachedRepository<>(
    userRepository,
    5 * 60 * 1000  // 5 minutos
);

// Primeira chamada - carrega do arquivo
List<User> users1 = cachedRepo.findAll();

// Segunda chamada em 2 minutos - vem do cache
List<User> users2 = cachedRepo.findAll();

// Invalidar cache
cachedRepo.invalidate();
```

## 🧪 Padrão 5: Testing

```java
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository repository;
    
    @InjectMocks
    private UserService service;
    
    @Test
    public void testCreateUser() {
        // Arrange
        User expected = new User(\"1\", \"João\", \"joao@example.com\");
        when(repository.findById(\"1\")).thenReturn(Optional.of(expected));
        
        // Act
        Optional<User> result = repository.findById(\"1\");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(\"João\", result.get().name);
    }
}

// Test de integração
@SpringBootTest
public class UserRepositoryIntegrationTest {
    
    @Autowired
    private UserRepository repository;
    
    @TempDir
    private Path tempDir;
    
    @Test
    public void testSaveAndLoad() throws IOException {
        // Arrange
        User user = new User(\"1\", \"João\", \"joao@example.com\");
        
        // Act
        repository.save(user);
        
        // Assert
        Optional<User> loaded = repository.findById(\"1\");
        assertTrue(loaded.isPresent());
        assertEquals(\"João\", loaded.get().name);
    }
}
```

## 📊 Padrão 6: Validação em Cascata

```java
public class ValidationService {
    
    private final JsonMapper mapper;
    
    public ValidationService() {
        this.mapper = Json.builder()
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    public <T> ValidationResult validate(String json, TypeRef<T> type) {
        List<String> errors = new ArrayList<>();
        
        // 1. Validação sintática
        try {
            mapper.parse(JsonSource.of(json));
        } catch (JsonParseException e) {
            errors.add(\"JSON malformado: \" + e.getMessage());
            return new ValidationResult(false, errors);
        }
        
        // 2. Validação de tipo
        try {
            T obj = mapper.decode(JsonSource.of(json), type);
        } catch (JsonMappingException e) {
            errors.add(\"Tipo incompatível: \" + e.getMessage());
        } catch (JsonValidationException e) {
            errors.add(\"Validação falhou: \" + e.getMessage());
        }
        
        // 3. Validação customizada
        if (type.equals(TypeRef.of(User.class))) {
            validateUser(json, errors);
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    private void validateUser(String json, List<String> errors) {
        try {
            JsonElement elem = Json.defaultMapper().parse(JsonSource.of(json));
            JsonObject obj = elem.asJsonObject();
            
            if (obj.has(\"email\")) {
                String email = obj.getAsString(\"email\");
                if (!email.contains(\"@\")) {
                    errors.add(\"Email inválido\");
                }
            }
        } catch (Exception e) {
            // Ignorar erros de validação customizada
        }
    }
}
```

## 🌐 Padrão 7: API Client

```java
public class ApiClient {
    
    private final JsonMapper mapper;
    private final RestTemplate restTemplate;
    
    public ApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.mapper = Json.builder()
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    public <T> T getJson(String url, TypeRef<T> type) {
        try {
            String response = restTemplate.getForObject(url, String.class);
            return mapper.decode(JsonSource.of(response), type);
        } catch (JsonException e) {
            throw new ApiException(\"Erro ao desserializar resposta\", e);
        }
    }
    
    public <T> String postJson(String url, T data) {
        try {
            String json = mapper.stringify(mapper.encode(data));
            return restTemplate.postForObject(url, json, String.class);
        } catch (JsonException e) {
            throw new ApiException(\"Erro ao serializar dados\", e);
        }
    }
}

// Usar
ApiClient client = new ApiClient(restTemplate);
User user = client.getJson(\"https://api.example.com/users/1\", TypeRef.of(User.class));
```

## 📈 Padrão 8: Monitoring e Logging

```java
@Component
public class JsonMetrics {
    
    private final MeterRegistry meterRegistry;
    private final AtomicInteger parseCount = new AtomicInteger(0);
    private final AtomicInteger encodeCount = new AtomicInteger(0);
    private final AtomicInteger decodeCount = new AtomicInteger(0);
    
    public JsonMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Registrar métricas
        Gauge.builder(\"json.parse.count\", parseCount::get).register(meterRegistry);
        Gauge.builder(\"json.encode.count\", encodeCount::get).register(meterRegistry);
        Gauge.builder(\"json.decode.count\", decodeCount::get).register(meterRegistry);
    }
    
    public <T> T trackDecode(String json, TypeRef<T> type, JsonMapper mapper) {
        try {
            T result = mapper.decode(JsonSource.of(json), type);
            decodeCount.incrementAndGet();
            return result;
        } catch (JsonException e) {
            logger.error(\"Erro na desserialização\", e);
            throw e;
        }
    }
    
    public <T> String trackEncode(T obj, JsonMapper mapper) {
        try {
            String result = mapper.stringify(mapper.encode(obj));
            encodeCount.incrementAndGet();
            return result;
        } catch (JsonException e) {
            logger.error(\"Erro na serialização\", e);
            throw e;
        }
    }
}
```

## 📚 Próximos Passos

1. **[Exemplos Completos](./15-exemplos-completos.md)** - Aplicações reais
2. Retorne ao **[Índice](../WIKI.md)** para explorar outros tópicos

---

**Anterior:** [13. I/O de Arquivos](./13-arquivo-io.md)  
**Próximo:** [15. Exemplos Completos](./15-exemplos-completos.md)
