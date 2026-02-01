# 1️⃣3️⃣ I/O de Arquivos

## 🎯 O que é JsonFiles?

`JsonFiles` é uma classe utilitária que simplifica leitura e escrita de arquivos JSON. Oferece atalhos convenientes para operações comuns.

## 📖 Leitura de Arquivos

### Ler para Objeto

```java
import io.obsidian.json.io.JsonFiles;
import java.nio.file.Paths;

// Simples
User user = JsonFiles.read(
    Paths.get(\"user.json\"),
    TypeRef.of(User.class)
);

// Com mapper customizado
JsonMapper mapper = Json.builder()
    .prettyPrint(true)
    .build()
    .buildMapper();

User user = JsonFiles.read(
    Paths.get(\"user.json\"),
    TypeRef.of(User.class),
    mapper
);
```

### Ler para Lista

```java
// Lista de usuários
List<User> users = JsonFiles.read(
    Paths.get(\"users.json\"),
    TypeRef.listOf(User.class)
);

for (User user : users) {
    System.out.println(user.name);
}
```

### Ler para Mapa

```java
// Mapa de usuários por ID
Map<String, User> userMap = JsonFiles.read(
    Paths.get(\"users.json\"),
    new TypeRef<Map<String, User>>() {}
);

User user = userMap.get(\"user_123\");
```

### Ler para JsonElement

```java
// Ler como JsonElement sem type safety
JsonElement element = JsonFiles.read(
    Paths.get(\"data.json\"),
    JsonElement.class
);

// Depois processar manualmente
if (element.isJsonObject()) {
    JsonObject obj = element.asJsonObject();
    // ...
}
```

## 📝 Escrita de Arquivos

### Escrever Objeto

```java
User user = new User();
user.name = \"João\";
user.email = \"joao@example.com\";

// Escrever com mapper padrão
JsonFiles.write(
    Paths.get(\"user.json\"),
    user
);

// Escrever com mapper customizado
JsonMapper mapper = Json.builder()
    .prettyPrint(true)
    .enableAnnotations(true)
    .build()
    .buildMapper();

JsonFiles.write(
    Paths.get(\"user.json\"),
    user,
    mapper
);
```

### Escrever Lista

```java
List<User> users = Arrays.asList(
    new User(\"João\", \"joao@example.com\"),
    new User(\"Maria\", \"maria@example.com\")
);

JsonFiles.write(
    Paths.get(\"users.json\"),
    users
);
```

### Escrever JsonElement

```java
JsonObject obj = new JsonObject();
obj.addProperty(\"name\", \"João\");
obj.addProperty(\"age\", 30);

JsonFiles.write(
    Paths.get(\"data.json\"),
    obj
);
```

## 🔄 Padrões de Leitura/Escrita

### Padrão 1: Cache em Memória

```java
public class UserCache {
    
    private final Path cacheFile = Paths.get(\"cache/users.json\");
    private List<User> users;
    
    public void loadCache() throws IOException {
        if (Files.exists(cacheFile)) {
            users = JsonFiles.read(
                cacheFile,
                TypeRef.listOf(User.class)
            );
        } else {
            users = new ArrayList<>();
        }
    }
    
    public void saveCache() {
        JsonFiles.write(cacheFile, users);
    }
    
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }
    
    public void addUser(User user) {
        users.add(user);
        saveCache();
    }
}
```

### Padrão 2: Carregamento Lento

```java
public class LazyUserLoader {
    
    private final Path dataFile;
    private List<User> users;
    
    public LazyUserLoader(Path dataFile) {
        this.dataFile = dataFile;
    }
    
    public List<User> getUsers() {
        if (users == null) {
            users = JsonFiles.read(
                dataFile,
                TypeRef.listOf(User.class)
            );
        }
        return users;
    }
}

// Usar
LazyUserLoader loader = new LazyUserLoader(Paths.get(\"users.json\"));
// Arquivo não é lido ainda

List<User> users = loader.getUsers();
// Agora é lido sob demanda
```

### Padrão 3: Backup e Restore

```java
public class BackupManager {
    
    private final Path dataFile;
    private final Path backupDir = Paths.get(\"backups\");
    
    public BackupManager(Path dataFile) {
        this.dataFile = dataFile;
    }
    
    public void createBackup() throws IOException {
        Files.createDirectories(backupDir);
        
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern(\"yyyy-MM-dd_HH-mm-ss\"));
        Path backup = backupDir.resolve(\"backup_\" + timestamp + \".json\");
        
        Files.copy(dataFile, backup);
        System.out.println(\"Backup criado: \" + backup);
    }
    
    public <T> void save(T data, TypeRef<T> type) {
        try {
            // Criar backup da versão anterior
            if (Files.exists(dataFile)) {
                createBackup();
            }
            
            // Escrever novo dados
            JsonFiles.write(dataFile, data);
        } catch (IOException e) {
            throw new RuntimeException(\"Erro ao salvar\", e);
        }
    }
    
    public <T> T load(TypeRef<T> type) {
        if (!Files.exists(dataFile)) {
            throw new FileNotFoundException(\"Arquivo não existe: \" + dataFile);
        }
        return JsonFiles.read(dataFile, type);
    }
}
```

### Padrão 4: Processamento de Arquivo Grande

```java
public class StreamProcessor {
    
    public void processLargeFile(Path jsonFile, Consumer<User> processor) throws IOException {
        // Ler uma linha por vez (se JSON Lines format)
        Files.lines(jsonFile)
            .forEach(line -> {
                try {
                    User user = JsonFiles.read(
                        Paths.get(line),
                        TypeRef.of(User.class)
                    );
                    processor.accept(user);
                } catch (JsonException e) {
                    System.err.println(\"Erro ao processar linha: \" + line);
                }
            });
    }
    
    // Uso
    StreamProcessor processor = new StreamProcessor();
    processor.processLargeFile(
        Paths.get(\"users.jsonl\"),
        user -> System.out.println(\"Processando: \" + user.name)
    );
}
```

## 🛡️ Tratamento de Erros em I/O

```java
public class SafeFileIO {
    
    public <T> Optional<T> readSafe(Path file, TypeRef<T> type) {
        try {
            if (!Files.exists(file)) {
                System.err.println(\"Arquivo não existe: \" + file);
                return Optional.empty();
            }
            
            return Optional.of(JsonFiles.read(file, type));
        } catch (JsonParseException e) {
            System.err.println(\"JSON malformado em: \" + file);
            return Optional.empty();
        } catch (JsonIoException e) {
            System.err.println(\"Erro de I/O ao ler: \" + file);
            return Optional.empty();
        }
    }
    
    public <T> boolean writeSafe(Path file, T data) {
        try {
            Files.createDirectories(file.getParent());
            JsonFiles.write(file, data);
            return true;
        } catch (IOException e) {
            System.err.println(\"Erro ao escrever: \" + file);
            return false;
        }
    }
}

// Usar
SafeFileIO io = new SafeFileIO();

Optional<User> user = io.readSafe(
    Paths.get(\"user.json\"),
    TypeRef.of(User.class)
);

user.ifPresent(u -> System.out.println(\"Carregado: \" + u.name));

boolean success = io.writeSafe(
    Paths.get(\"output.json\"),
    myObject
);
```

## 🎯 Exemplo Completo

```java
public class UserRepository {
    
    private final Path dataFile;
    private final JsonMapper mapper;
    
    public UserRepository(Path dataFile) {
        this.dataFile = dataFile;
        this.mapper = Json.builder()
            .prettyPrint(true)
            .enableAnnotations(true)
            .build()
            .buildMapper();
    }
    
    // Carregar todos os usuários
    public List<User> loadAll() {
        try {
            if (!Files.exists(dataFile)) {
                return new ArrayList<>();
            }
            
            return JsonFiles.read(
                dataFile,
                TypeRef.listOf(User.class),
                mapper
            );
        } catch (JsonException e) {
            System.err.println(\"Erro ao carregar usuários: \" + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Salvar todos os usuários
    public void saveAll(List<User> users) {
        try {
            Files.createDirectories(dataFile.getParent());
            JsonFiles.write(dataFile, users, mapper);
        } catch (IOException e) {
            throw new RuntimeException(\"Erro ao salvar usuários\", e);
        }
    }
    
    // Adicionar novo usuário
    public void add(User user) {
        List<User> users = loadAll();
        users.add(user);
        saveAll(users);
    }
    
    // Encontrar por ID
    public Optional<User> findById(String id) {
        return loadAll().stream()
            .filter(u -> u.id.equals(id))
            .findFirst();
    }
    
    // Contar usuários
    public int count() {
        return loadAll().size();
    }
    
    // Limpar todos
    public void deleteAll() {
        try {
            Files.deleteIfExists(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(\"Erro ao deletar arquivo\", e);
        }
    }
}

// Usar
UserRepository repo = new UserRepository(Paths.get(\"data/users.json\"));

// Adicionar
repo.add(new User(\"João\", \"joao@example.com\"));
repo.add(new User(\"Maria\", \"maria@example.com\"));

// Listar
List<User> users = repo.loadAll();
System.out.println(\"Total: \" + repo.count());

// Encontrar
repo.findById(\"1\").ifPresent(u -> System.out.println(u.name));

// Limpar
repo.deleteAll();
```

## 💡 Melhores Práticas

✅ **Faça:**
```java
// Sempre crie diretório se não existir
Files.createDirectories(file.getParent());

// Use try-catch para I/O
try {
    JsonFiles.write(file, data);
} catch (JsonIoException e) {
    logger.error(\"Erro ao escrever\", e);
}

// Verifique existência antes de ler
if (Files.exists(file)) {
    data = JsonFiles.read(file, type);
}
```

❌ **Evite:**
```java
// Não assuma que arquivo existe
data = JsonFiles.read(file, type);  // Pode falhar!

// Não ignore exceções
try {
    JsonFiles.write(file, data);
} catch (Exception e) {
    // Ignorado!
}

// Não sobrescreva sem backup
JsonFiles.write(file, newData);  // Dados antigos perdidos!
```

## 📚 Próximos Passos

1. **[Padrões Práticos](./14-padroes-praticos.md)** - Best practices
2. **[Exemplos Completos](./15-exemplos-completos.md)** - Aplicações reais

---

**Anterior:** [12. Tratamento de Erros](./12-tratamento-erros.md)  
**Próximo:** [14. Padrões Práticos](./14-padroes-praticos.md)
