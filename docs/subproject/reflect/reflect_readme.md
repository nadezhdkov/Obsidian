# Reflect API

API fluente e poderosa para trabalhar com reflexão em Java de forma simples e intuitiva.

## 📦 Estrutura

```
io.reflect/
├── Reflect.java              - API principal
├── ReflectField.java         - Manipulação de campos
├── ReflectFields.java        - Coleção de campos
├── ReflectMethod.java        - Manipulação de métodos
├── ReflectMethods.java       - Coleção de métodos
├── ReflectAnnotations.java   - Manipulação de annotations
├── ReflectBuilder.java       - Builder pattern
├── util/
│   └── ReflectUtils.java     - Utilitários
└── exception/
    └── ReflectException.java
```

## 🚀 Características

✅ **API Fluente** - Encadeamento de métodos estilo builder

✅ **Filtros Poderosos** - Filtre campos e métodos facilmente

✅ **Type-Safe** - Tipagem forte onde possível

✅ **Simples ao Complexo** - Da operação básica à manipulação avançada

✅ **Zero Boilerplate** - Elimine código repetitivo

✅ **Builder Pattern** - Construa objetos de forma fluente

✅ **Utilitários** - Funções prontas para casos comuns

## 📖 Uso Básico

### Criar Instância

```java
// Construtor vazio
User user = Reflect.on(User.class).newInstance();

// Construtor com argumentos
User user = Reflect.on(User.class).newInstance("John", 30);
```

### Manipular Campos

```java
// Definir valor
Reflect.on(user)
    .field("name")
    .set("John Doe");

// Obter valor
String name = Reflect.on(user)
    .field("name")
    .get();

// Verificar propriedades
boolean isPrivate = Reflect.on(user)
    .field("name")
    .isPrivate();
```

### Invocar Métodos

```java
// Método sem parâmetros
String name = Reflect.on(user)
    .method("getName")
    .invoke();

// Método com parâmetros
Reflect.on(user)
    .method("setAge")
    .withParameterTypes(int.class)
    .invoke(30);

// Método privado
Reflect.on(user)
    .method("privateMethod")
    .invoke();
```

## 🔍 Filtros de Campos

```java
// Filtros básicos
List<Field> fields = Reflect.on(User.class)
    .fields()
    .privateOnly()        // Apenas privados
    .nonStatic()          // Não-estáticos
    .ofType(String.class) // Do tipo String
    .list();

// Filtros por annotation
List<Field> annotated = Reflect.on(User.class)
    .fields()
    .annotatedWith(Env.class)
    .nonFinal()
    .list();

// Filtros por nome
List<Field> matching = Reflect.on(User.class)
    .fields()
    .nameMatches("^name.*")
    .list();
```

### Filtros Disponíveis

- `filter(Predicate<Field>)` - Filtro customizado
- `named(String)` - Por nome exato
- `nameMatches(String)` - Por regex
- `ofType(Class<?>)` - Por tipo
- `assignableTo(Class<?>)` - Assignable a tipo
- `annotatedWith(Class<?>)` - Com annotation
- `notAnnotatedWith(Class<?>)` - Sem annotation
- `publicOnly()` / `privateOnly()` / `protectedOnly()`
- `staticOnly()` / `nonStatic()`
- `finalOnly()` / `nonFinal()`
- `transientOnly()` / `nonTransient()`

## 🔧 Filtros de Métodos

```java
// Getters e Setters
List<Method> getters = Reflect.on(User.class)
    .methods()
    .getters()
    .list();

List<Method> setters = Reflect.on(User.class)
    .methods()
    .setters()
    .list();

// Filtros combinados
List<Method> methods = Reflect.on(User.class)
    .methods()
    .publicOnly()
    .nonStatic()
    .nameStartsWith("get")
    .withoutParameters()
    .list();
```

### Filtros Disponíveis

- `filter(Predicate<Method>)` - Filtro customizado
- `named(String)` - Por nome exato
- `nameMatches(String)` - Por regex
- `nameStartsWith(String)` - Começa com
- `getters()` / `setters()` - Getters/Setters
- `returning(Class<?>)` - Por tipo de retorno
- `returningVoid()` - Retorna void
- `withParameterCount(int)` - Número de parâmetros
- `withoutParameters()` / `withParameters()`
- `annotatedWith(Class<?>)` - Com annotation
- `publicOnly()` / `privateOnly()` / `protectedOnly()`
- `staticOnly()` / `nonStatic()`
- `abstractOnly()` / `nonAbstract()`
- `finalOnly()` / `nonFinal()`

## 🏗️ Builder Pattern

```java
// Criar com builder
User user = ReflectBuilder.of(User.class)
    .set("name", "John")
    .set("age", 30)
    .set("email", "john@example.com")
    .build();

// Builder a partir de instância
User modified = ReflectBuilder.from(original)
    .set("age", 35)
    .build();

// Definir múltiplos campos
Map<String, Object> values = Map.of(
    "name", "Jane",
    "age", 25
);

User user = ReflectBuilder.of(User.class)
    .setAll(values)
    .build();
```

## 🛠️ Utilitários

### Copiar Campos

```java
User source = new User("John", 30);
User target = new User();

ReflectUtils.copy(source, target);
```

### Converter para Map

```java
Map<String, Object> map = ReflectUtils.toMap(user);

User user = ReflectUtils.fromMap(map, User.class);
```

### Clone Profundo

```java
User clone = ReflectUtils.deepClone(original);
```

### Comparar Objetos

```java
boolean equals = ReflectUtils.equals(obj1, obj2);
```

### ToString Automático

```java
String str = ReflectUtils.toString(user);
// Output: User{name=John, age=30, email=john@example.com}
```

## 📋 Operações em Massa

### Definir Valor em Múltiplos Campos

```java
// Definir todos os campos String como "N/A"
Reflect.on(user)
    .fields()
    .ofType(String.class)
    .setAll("N/A");
```

### Executar Ação em Campos

```java
Reflect.on(user)
    .fields()
    .nonStatic()
    .forEach(field -> {
        System.out.println(field.getName() + " = " + field.get(user));
    });
```

### Invocar Múltiplos Métodos

```java
// Invocar todos os métodos que começam com "init"
Reflect.on(service)
    .methods()
    .nameStartsWith("init")
    .withoutParameters()
    .invokeAll();
```

## 🎯 Annotations

```java
// Verificar annotation
boolean hasEnv = Reflect.on(User.class)
    .field("name")
    .hasAnnotation(Env.class);

// Obter annotation
Env env = Reflect.on(User.class)
    .field("name")
    .getAnnotation(Env.class);

// Trabalhar com annotations da classe
ReflectAnnotations annotations = Reflect.on(User.class)
    .annotations();

boolean has = annotations.has(EnvPrefix.class);
EnvPrefix prefix = annotations.get(EnvPrefix.class);
```

## 📊 Informações e Verificações

```java
Reflect reflect = Reflect.on(User.class);

// Verificações de classe
boolean isInterface = reflect.isInterface();
boolean isAbstract = reflect.isAbstract();
boolean isEnum = reflect.isEnum();

// Hierarquia
Reflect superclass = reflect.superclass();
List<Class<?>> interfaces = reflect.interfaces();

// Contagem
int fieldCount = reflect.fields().count();
int methodCount = reflect.methods().count();

// Existência
boolean exists = reflect.fields()
    .named("name")
    .exists();
```

## 🔄 Encadeamento Fluente

```java
// Tudo em uma cadeia
String result = Reflect.on(User.class)
    .newInstance()
    .field("name").set("John").end()
    .field("age").set(30).end()
    .method("getName")
    .invoke();
```

## 🎨 Exemplos Avançados

### Copiar Campos Específicos

```java
Reflect.on(source)
    .fields()
    .annotatedWith(CopyField.class)
    .copyTo(target);
```

### Obter Map de Campos Filtrados

```java
Map<String, Object> map = Reflect.on(user)
    .fields()
    .nonStatic()
    .nonTransient()
    .toMap();
```

### Encontrar Primeiro Campo

```java
Optional<Field> firstString = Reflect.on(User.class)
    .fields()
    .ofType(String.class)
    .first();
```

### Criar Proxy Dinâmico

```java
MyInterface proxy = Reflect.on(MyInterface.class)
    .proxy(MyInterface.class, (proxy, method, args) -> {
        // Handler implementation
        return "result";
    });
```

## ⚙️ Configuração de Acesso

Todos os campos e métodos são automaticamente tornados acessíveis (`setAccessible(true)`), permitindo acesso a membros privados.

## 🎯 Casos de Uso

- **Injeção de Dependências** - Injete valores em campos privados
- **Serialização/Deserialização** - Converta objetos para/de Map
- **Testing** - Acesse/modifique estado interno
- **Frameworks** - Construa ferramentas baseadas em annotations
- **Clonagem** - Clone objetos profundamente
- **Comparação** - Compare objetos por reflexão
- **Builder Pattern** - Construa objetos complexos

## 🚨 Tratamento de Erros

Todas as operações lançam `ReflectException` em caso de erro:

```java
try {
    Reflect.on(user).field("invalid").get();
} catch (ReflectException e) {
    System.err.println("Erro: " + e.getMessage());
}
```

## 📝 Licença

Mesmo modelo de licença da biblioteca base.