# 🔮 Obsidian

Um framework Java moderno e minimalista com bibliotecas utilitárias poderosas para simplificar tarefas comuns de desenvolvimento.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../../../LICENSE.md)
[![Java Version](https://img.shields.io/badge/java-11%2B-orange.svg)](https://www.java.com/)
[![Maven Central](https://img.shields.io/badge/maven--central-1.0.0-brightgreen.svg)]()

**📖 Idiomas Disponíveis:** [English](../../../README.md) | [Português Brasileiro](./README.md) | [Deutsch](../de-DE/README.md) | [Español](../es-ES/README.md)

## 📚 Sobre

Obsidian é uma coleção de módulos independentes e altamente especializados que facilitam desenvolvimento em Java. Cada módulo é projetado para resolver problemas específicos com uma API clara, fluente e fácil de usar.

### Módulos Disponíveis

#### 🔧 [Obsidian Configuration](../../../docs/subproject/dotenv/dotenv_readme.md)

Sistema minimalista de **annotations** para mapear variáveis de ambiente (`.env`) para campos Java.

**Características:**
- ✅ Mapeamento automático de variáveis de ambiente
- ✅ Suporte a valores padrão
- ✅ Validação de variáveis obrigatórias
- ✅ Prefixos automáticos
- ✅ Conversão automática de tipos
- ✅ Ignorar campos específicos

**Exemplo Rápido:**
```java
@Env("DATABASE_URL")
String dbUrl;

@Env("PORT")
@Default("8080")
int port;

@RequiredEnv
@Env("API_KEY")
String apiKey;
```

#### 🔬 [Obsidian Reflection](../../../docs/subproject/reflect/reflect_readme.md)

API fluente e poderosa para trabalhar com reflexão em Java de forma simples e intuitiva.

**Características:**
- ✅ API fluente com encadeamento de métodos
- ✅ Filtros poderosos para campos e métodos
- ✅ Type-safe sempre que possível
- ✅ Builder pattern integrado
- ✅ Manipulação fácil de anotações
- ✅ Zero boilerplate code

**Exemplo Rápido:**
```java
// Acessar campos
Reflect.on(User.class)
    .fields()
    .named("name")
    .set(user, "John");

// Invocar métodos
Reflect.on(user)
    .methods()
    .getters()
    .each(System.out::println);

// Criar instâncias
User user = Reflect.on(User.class).newInstance("John", 30);
```

## 🚀 Quick Start

### Instalação

#### Maven
```xml
<dependency>
    <groupId>obsidian.lib</groupId>
    <artifactId>obsidian-configuration</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>obsidian.lib</groupId>
    <artifactId>obsidian-reflection</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle
```gradle
implementation 'obsidian.lib:obsidian-configuration:1.0.0'
implementation 'obsidian.lib:obsidian-reflection:1.0.0'
```

### Exemplo Completo

```java
import io.dotenv.annotations.Env;
import io.dotenv.annotations.Default;
import io.dotenv.core.Dotenv;
import lang.reflect.Reflect;

public class Application {
    
    @Env("APP_NAME")
    @Default("MyApp")
    String appName;
    
    @Env("PORT")
    @Default("8080")
    int port;
    
    public static void main(String[] args) {
        // Carregar configuração do .env
        Application app = new Application();
        Dotenv.load(app);
        
        // Usar reflexão para inspecionar a aplicação
        Reflect.on(app)
            .fields()
            .each(f -> System.out.println(f.getName() + " = " + f.get(app)));
    }
}
```

## 📖 Documentação

- [Obsidian Configuration - Documentação Completa](../../../docs/subproject/dotenv/dotenv_readme.md)
- [Obsidian Reflection - Documentação Completa](../../../docs/subproject/reflect/reflect_readme.md)
- [Arquitetura JSON](../../subproject/json/json_architecture_and_design.md)

## 🛠️ Desenvolvimento

### Requisitos

- **Java 11** ou superior
- **Gradle 7.0** ou superior
- **Make** (opcional, para usar os comandos do Makefile)

### Build do Projeto

```bash
# Build completo
make build

# Rodar testes
make test

# Limpar build
make clean

# Gerar documentação
make javadoc

# Publicar localmente
make local
```

Ou usando Gradle diretamente:

```bash
# Build
./gradlew build

# Testes
./gradlew test

# Verificar
./gradlew check
```

### Estrutura do Projeto

```
Obsidian/
├── obsidian-configuration/   # Módulo de configuração e .env
│   ├── src/main/java/
│   │   └── io/dotenv/
│   │       ├── annotations/   # Annotations @Env, @Default, etc
│   │       ├── core/          # Implementação core
│   │       ├── processor/     # Processadores
│   │       └── util/          # Utilitários
│   └── build.gradle.kts
│
├── obsidian-reflection/      # Módulo de reflexão
│   ├── src/main/java/
│   │   └── lang/reflect/
│   │       ├── Reflect*.java  # Classes principais
│   │       └── exception/     # Exceções
│   └── build.gradle.kts
│
├── obsidian-promise/         # Módulo de promises
│   ├── src/main/java/
│   │   └── io/obsidian/promise/
│   │       ├── api/           # API Promise
│   │       ├── combinators/   # Combinadores de promises
│   │       ├── error/         # Tratamento de erros
│   │       └── internal/      # Implementação interna
│   └── build.gradle.kts
│
├── src/main/java/obsidian/  # Utilidades core
│   ├── control/              # When e controle de fluxo
│   ├── functional/           # Try<T> e utilidades funcionais
│   ├── util/                 # Box<T>, Sequence<T>, Range
│   │   ├── concurrent/       # Contêineres thread-safe
│   │   └── stream/           # Utilidades de stream
│   └── api/                  # APIs core
│
├── docs/                     # Documentação
├── gradle/                   # Gradle wrapper
├── build.gradle.kts          # Build config raiz
├── settings.gradle.kts       # Configuração de subprojetos
├── Makefile                  # Targets para desenvolvimento
└── README.md                 # Este arquivo
```

## 📦 Dependências

- **JetBrains Annotations** - Anotações para melhor análise de código
- **Gson** - Serialização JSON
- **YAML** - Suporte a YAML
- **Lombok** - Redução de boilerplate
- **JUnit 5** - Framework de testes

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está licenciado sob a [Apache License 2.0](../../../LICENSE.md) - veja o arquivo de licença para detalhes.

## 👨‍💻 Autor

Desenvolvido com ❤️ por Nadezhda

## 🎯 Roadmap

- [ ] Suporte a YAML avançado
- [ ] Cache inteligente de reflexão
- [ ] Validação automática de campos
- [ ] Integração com Spring Framework
- [ ] Plugin Gradle dedicado
- [ ] Documentação em múltiplos idiomas

## 💡 Dicas & Truques

### Usando Dotenv com Reflection

```java
@Env("DB_HOST")
String host;

// Acessar todos os campos anotados com @Env
Reflect.on(this)
    .fields()
    .annotated(Env.class)
    .each(f -> System.out.println(f.getName()));
```

### Filtrando Métodos

```java
// Encontrar todos os getters
List<Method> getters = Reflect.on(User.class)
    .methods()
    .getters()
    .list();

// Filtros customizados
Reflect.on(service)
    .methods()
    .isPublic()
    .notStatic()
    .startWith("handle")
    .each(method -> /* processar */);
```

## 🐛 Reportar Problemas

Encontrou um bug? Por favor, abra uma [issue](https://github.com/nadezhdkov/obsidian/issues) com:

- Descrição clara do problema
- Steps para reproduzir
- Comportamento esperado vs atual
- Versão do Java e Obsidian

## 📞 Suporte

Para suporte adicional:

- 📧 Email: seu-email@exemplo.com
- 💬 Issues no GitHub
- 📚 Consulte a documentação completa nas pastas `/docs`

---

**Desenvolvido com ❤️ usando Java e Gradle**
