# 1️⃣5️⃣ Exemplos Completos

Nesta seção apresentamos exemplos práticos e completos de aplicações usando a API JSON do Obsidian.

## 📱 Exemplo 1: Sistema de Gestão de Contatos

```java
/**
 * Modelo de Contato
 */
@Data
public class Contact {
    @JsonRequired
    @JsonName(\"contact_id\")
    public String id;
    
    @JsonRequired
    public String name;
    
    @JsonRequired
    public String email;
    
    @JsonDefault(\"false\")
    public boolean starred;
    
    @JsonName(\"phone_numbers\")
    public List<String> phones = new ArrayList<>();
    
    @JsonAdapter(LocalDateCodec.class)
    public LocalDate createdAt;
    
    @JsonIgnore
    public transient boolean modified;
}

/**
 * Repository de Contatos
 */
public class ContactRepository {
    
    private final JsonMapper mapper;
    private final Path dataFile;
    
    public ContactRepository(Path dataFile) {
        this.dataFile = dataFile;
        this.mapper = Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    public void saveContact(Contact contact) {
        List<Contact> contacts = loadAll();
        contacts.removeIf(c -> c.id.equals(contact.id));
        contacts.add(contact);
        persistAll(contacts);
    }
    
    public Optional<Contact> getContact(String id) {
        return loadAll().stream()
            .filter(c -> c.id.equals(id))
            .findFirst();
    }
    
    public List<Contact> searchByName(String query) {
        return loadAll().stream()
            .filter(c -> c.name.toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    public List<Contact> getStarred() {
        return loadAll().stream()
            .filter(c -> c.starred)
            .collect(Collectors.toList());
    }
    
    public List<Contact> loadAll() {
        try {
            if (!Files.exists(dataFile)) {
                return new ArrayList<>();
            }
            return JsonFiles.read(dataFile, TypeRef.listOf(Contact.class), mapper);
        } catch (JsonException e) {
            System.err.println(\"Erro ao carregar contatos: \" + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private void persistAll(List<Contact> contacts) {
        try {
            Files.createDirectories(dataFile.getParent());
            JsonFiles.write(dataFile, contacts, mapper);
        } catch (IOException e) {
            throw new RuntimeException(\"Erro ao salvar contatos\", e);
        }
    }
}

/**
 * Aplicação de Contatos
 */
public class ContactManagementApp {
    
    public static void main(String[] args) {
        ContactRepository repo = new ContactRepository(
            Paths.get(\"data/contacts.json\")
        );
        
        // 1. Criar contatos
        Contact contact1 = new Contact();
        contact1.id = \"1\";
        contact1.name = \"João Silva\";
        contact1.email = \"joao@example.com\";
        contact1.phones.add(\"11999999999\");
        contact1.createdAt = LocalDate.now();
        
        Contact contact2 = new Contact();
        contact2.id = \"2\";
        contact2.name = \"Maria Santos\";
        contact2.email = \"maria@example.com\";
        contact2.starred = true;
        contact2.createdAt = LocalDate.now();
        
        // 2. Salvar
        repo.saveContact(contact1);
        repo.saveContact(contact2);
        System.out.println(\"Contatos salvos\");
        
        // 3. Listar todos
        List<Contact> all = repo.loadAll();
        System.out.println(\"\\nTodos os contatos: \" + all.size());
        
        // 4. Buscar um
        repo.getContact(\"1\").ifPresent(c -> 
            System.out.println(\"Contato: \" + c.name + \" (\" + c.email + \")\")
        );
        
        // 5. Favoritos
        List<Contact> starred = repo.getStarred();
        System.out.println(\"\\nContatos favoritos: \" + starred.size());
        
        // 6. Pesquisar
        List<Contact> search = repo.searchByName(\"Silva\");
        System.out.println(\"Resultados da busca por 'Silva': \" + search.size());
    }
}
```

## 🛒 Exemplo 2: Sistema de Carrinho de Compras

```java
@Data
public class Product {
    public String id;
    public String name;
    
    @JsonAdapter(BigDecimalCodec.class)
    public BigDecimal price;
    
    public String description;
}

@Data
public class CartItem {
    public String productId;
    public int quantity;
    
    @JsonAdapter(BigDecimalCodec.class)
    public BigDecimal subtotal;
}

@Data
public class ShoppingCart {
    public String cartId;
    public List<CartItem> items = new ArrayList<>();
    
    @JsonAdapter(LocalDateTimeCodec.class)
    public LocalDateTime createdAt;
    
    public void addItem(Product product, int quantity) {
        CartItem item = new CartItem();
        item.productId = product.id;
        item.quantity = quantity;
        item.subtotal = product.price.multiply(BigDecimal.valueOf(quantity));
        items.add(item);
    }
    
    public BigDecimal getTotal() {
        return items.stream()
            .map(i -> i.subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

public class CartService {
    
    private final JsonMapper mapper;
    private final Map<String, ShoppingCart> carts = new ConcurrentHashMap<>();
    
    public CartService() {
        this.mapper = Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    public ShoppingCart createCart() {
        ShoppingCart cart = new ShoppingCart();
        cart.cartId = UUID.randomUUID().toString();
        cart.createdAt = LocalDateTime.now();
        carts.put(cart.cartId, cart);
        return cart;
    }
    
    public ShoppingCart getCart(String cartId) {
        return carts.get(cartId);
    }
    
    public void saveCart(ShoppingCart cart) {
        carts.put(cart.cartId, cart);
    }
    
    public String exportToJson(ShoppingCart cart) {
        return mapper.stringify(mapper.encode(cart));
    }
    
    public ShoppingCart importFromJson(String json) {
        return mapper.decode(JsonSource.of(json), TypeRef.of(ShoppingCart.class));
    }
}

public class ShoppingExample {
    
    public static void main(String[] args) {
        CartService service = new CartService();
        
        // 1. Criar produtos
        Product laptop = new Product();
        laptop.id = \"p1\";
        laptop.name = \"Laptop\";
        laptop.price = new BigDecimal(\"2000.00\");
        
        Product mouse = new Product();
        mouse.id = \"p2\";
        mouse.name = \"Mouse\";
        mouse.price = new BigDecimal(\"50.00\");
        
        // 2. Criar carrinho
        ShoppingCart cart = service.createCart();
        System.out.println(\"Carrinho criado: \" + cart.cartId);
        
        // 3. Adicionar items
        cart.addItem(laptop, 1);
        cart.addItem(mouse, 2);
        System.out.println(\"Items adicionados\");
        
        // 4. Calcular total
        System.out.println(\"Total: R$ \" + cart.getTotal());
        
        // 5. Exportar para JSON
        String json = service.exportToJson(cart);
        System.out.println(\"\\nJSON do carrinho:\");
        System.out.println(json);
        
        // 6. Importar de volta
        ShoppingCart restored = service.importFromJson(json);
        System.out.println(\"\\nCarrinho restaurado: \" + restored.items.size() + \" items\");
        System.out.println(\"Total: R$ \" + restored.getTotal());
    }
}
```

## 📊 Exemplo 3: Sistema de Processamento de Dados

```java
@Data
public class DataRecord {
    @JsonRequired
    public String id;
    
    public String data;
    
    @JsonAdapter(LocalDateTimeCodec.class)
    public LocalDateTime timestamp;
    
    @JsonDefault(\"PENDING\")
    public String status;
}

public class DataProcessor {
    
    private final JsonMapper mapper;
    private final Path inputDir;
    private final Path outputDir;
    
    public DataProcessor(Path inputDir, Path outputDir) {
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.mapper = Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    public void processAllFiles() throws IOException {
        Files.createDirectories(outputDir);
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, \"*.json\")) {
            for (Path file : stream) {
                processFile(file);
            }
        }
    }
    
    private void processFile(Path inputFile) {
        try {
            // 1. Ler registros
            List<DataRecord> records = JsonFiles.read(
                inputFile,
                TypeRef.listOf(DataRecord.class),
                mapper
            );
            
            // 2. Processar
            List<DataRecord> processed = records.stream()
                .map(this::processRecord)
                .collect(Collectors.toList());
            
            // 3. Salvar resultados
            String filename = inputFile.getFileName().toString();
            Path outputFile = outputDir.resolve(\"processed_\" + filename);
            JsonFiles.write(outputFile, processed, mapper);
            
            System.out.println(\"Processado: \" + filename);
        } catch (JsonException e) {
            System.err.println(\"Erro ao processar: \" + inputFile);
            System.err.println(\"Detalhes: \" + e.getMessage());
        }
    }
    
    private DataRecord processRecord(DataRecord record) {
        // Simular processamento
        record.status = \"PROCESSED\";
        record.timestamp = LocalDateTime.now();
        return record;
    }
}

public class DataProcessingExample {
    
    public static void main(String[] args) throws IOException {
        // Preparar dados de teste
        Path inputDir = Files.createTempDirectory(\"input\");
        Path outputDir = Files.createTempDirectory(\"output\");
        
        // Criar arquivo de entrada
        List<DataRecord> records = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            DataRecord record = new DataRecord();
            record.id = \"rec_\" + i;
            record.data = \"Data \" + i;
            record.timestamp = LocalDateTime.now();
            record.status = \"PENDING\";
            records.add(record);
        }
        
        JsonFiles.write(inputDir.resolve(\"data.json\"), records);
        
        // Processar
        DataProcessor processor = new DataProcessor(inputDir, outputDir);
        processor.processAllFiles();
        
        // Verificar resultado
        List<DataRecord> processed = JsonFiles.read(
            outputDir.resolve(\"processed_data.json\"),
            TypeRef.listOf(DataRecord.class)
        );
        
        System.out.println(\"\\nResultado:\");
        processed.forEach(r -> System.out.println(\"  \" + r.id + \": \" + r.status));
    }
}
```

## 🔐 Exemplo 4: Sistema de Configuração Segura

```java
public class EncryptedString {
    private final String encrypted;
    
    private EncryptedString(String encrypted) {
        this.encrypted = encrypted;
    }
    
    public static EncryptedString encrypt(String plain) {
        String encrypted = Base64.getEncoder()
            .encodeToString(plain.getBytes());
        return new EncryptedString(encrypted);
    }
    
    public String decrypt() {
        byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
        return new String(decodedBytes);
    }
    
    @Override
    public String toString() {
        return encrypted;
    }
}

public class EncryptedStringCodec implements JsonCodec<EncryptedString> {
    
    @Override
    public JsonElement encode(EncryptedString value) {
        return new JsonPrimitive(value.toString());
    }
    
    @Override
    public EncryptedString decode(JsonElement element) {
        String encrypted = element.asJsonPrimitive().asString();
        return new EncryptedString(encrypted);
    }
}

@Data
public class AppConfig {
    @JsonRequired
    public String appName;
    
    @JsonRequired
    @JsonAdapter(EncryptedStringCodec.class)
    public EncryptedString databasePassword;
    
    @JsonRequired
    @JsonAdapter(EncryptedStringCodec.class)
    public EncryptedString apiKey;
    
    @JsonDefault(\"true\")
    public boolean debug;
}

public class ConfigurationExample {
    
    public static void main(String[] args) throws IOException {
        JsonMapper mapper = Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
        
        // 1. Criar configuração
        AppConfig config = new AppConfig();
        config.appName = \"MyApp\";
        config.databasePassword = EncryptedString.encrypt(\"secret_db_password\");
        config.apiKey = EncryptedString.encrypt(\"secret_api_key\");
        
        // 2. Salvar (criptografado em JSON)
        String json = mapper.stringify(mapper.encode(config));
        System.out.println(\"Configuração salva:\");
        System.out.println(json);
        
        // 3. Carregar (descriptografa automaticamente)
        AppConfig loaded = mapper.decode(
            JsonSource.of(json),
            TypeRef.of(AppConfig.class)
        );
        
        System.out.println(\"\\nConfiguração carregada:\");
        System.out.println(\"App: \" + loaded.appName);
        System.out.println(\"DB Password: \" + loaded.databasePassword.decrypt());
        System.out.println(\"API Key: \" + loaded.apiKey.decrypt());
    }
}
```

## 📚 Exemplo 5: API REST Completa

```java
@RestController
@RequestMapping(\"/api/posts\")
public class PostController {
    
    private final PostService service;
    private final JsonMapper mapper;
    
    @Autowired
    public PostController(PostService service, JsonMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    
    @GetMapping(\"/{id}\")
    public ResponseEntity<Post> getPost(@PathVariable String id) {
        return service.getPost(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Post>> listPosts() {
        return ResponseEntity.ok(service.listAll());
    }
    
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostCreateRequest req) {
        Post post = service.createPost(req.title, req.content);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(post);
    }
    
    @PutMapping(\"/{id}\")
    public ResponseEntity<Post> updatePost(
        @PathVariable String id,
        @RequestBody PostUpdateRequest req) {
        
        Optional<Post> updated = service.updatePost(id, req.title, req.content);
        return updated
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping(\"/{id}\")
    public ResponseEntity<Void> deletePost(@PathVariable String id) {
        return service.deletePost(id)
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }
}

@Data
public class Post {
    @JsonRequired
    public String id;
    
    @JsonRequired
    public String title;
    
    @JsonRequired
    public String content;
    
    @JsonAdapter(LocalDateTimeCodec.class)
    public LocalDateTime createdAt;
    
    @JsonAdapter(LocalDateTimeCodec.class)
    public LocalDateTime updatedAt;
}

public class PostService {
    
    private final PostRepository repository;
    
    @Autowired
    public PostService(PostRepository repository) {
        this.repository = repository;
    }
    
    public Post createPost(String title, String content) {
        Post post = new Post();
        post.id = UUID.randomUUID().toString();
        post.title = title;
        post.content = content;
        post.createdAt = LocalDateTime.now();
        post.updatedAt = LocalDateTime.now();
        
        repository.save(post);
        return post;
    }
    
    public Optional<Post> getPost(String id) {
        return repository.findById(id);
    }
    
    public List<Post> listAll() {
        return repository.findAll();
    }
    
    public Optional<Post> updatePost(String id, String title, String content) {
        Optional<Post> post = repository.findById(id);
        post.ifPresent(p -> {
            p.title = title;
            p.content = content;
            p.updatedAt = LocalDateTime.now();
            repository.save(p);
        });
        return post;
    }
    
    public boolean deletePost(String id) {
        return repository.delete(id);
    }
}
```

## 🎓 Conclusão

Estes exemplos cobrem casos de uso reais:
- ✅ **Exemplo 1** - CRUD simples com validação
- ✅ **Exemplo 2** - Lógica de negócio com cálculos
- ✅ **Exemplo 3** - Processamento em batch
- ✅ **Exemplo 4** - Segurança com criptografia
- ✅ **Exemplo 5** - API REST completa

Para mais informações, consulte os outros documentos da wiki!

---

**Anterior:** [14. Padrões Práticos](./14-padroes-praticos.md)  
**Retornar:** [Índice da Wiki](../WIKI.md)

**Última atualização:** Janeiro de 2026  
**Versão:** 0.1.0
