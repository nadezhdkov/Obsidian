# Dotenv Annotations

Este documento descreve o conjunto mínimo e ideal de annotations para um sistema de **dotenv baseado em Java**, focado exclusivamente em:

> Mapear variáveis de ambiente (.env / System.getenv) para campos Java.

Sem incluir responsabilidades externas como:

* criptografia
* profiles complexos
* hot reload
* dependency injection pesado

---

## Package

Todas as annotations devem viver em:

```
io.dotenv.annotations
```

Isso mantém:

* separação clara entre API core e integração
* possibilidade futura de módulos independentes

---

## 1. @Env

### Descrição

Define explicitamente o nome da variável de ambiente que será lida.

### Exemplo

```java
@Env("DB_HOST")
String host;
```

### Responsabilidade

* Mapeamento direto entre campo Java e variável de ambiente.

---

## 2. @Default

### Descrição

Define um valor padrão caso a variável de ambiente não exista.

### Exemplo

```java
@Env("DB_PORT")
@Default("3306")
int port;
```

### Responsabilidade

* Evitar null
* Evitar exceções por ausência

---

## 3. @RequiredEnv

### Descrição

Indica que a variável é obrigatória.
Se não existir, o sistema deve lançar erro.

### Exemplo

```java
@Env("API_KEY")
@RequiredEnv
String apiKey;
```

### Responsabilidade

* Validação de presença

---

## 4. @EnvPrefix

### Descrição

Define um prefixo automático para todos os campos da classe.

### Exemplo

```java
@EnvPrefix("REDIS_")
class RedisConfig {

    @Env("HOST")
    String host;   // REDIS_HOST

    @Env("PORT")
    int port;     // REDIS_PORT
}
```

### Responsabilidade

* Evitar repetição de prefixos
* Organização por domínio

---

## 5. @EnvIgnore

### Descrição

Marca um campo para ser ignorado pelo scanner.

### Exemplo

```java
@EnvIgnore
String temp;
```

### Responsabilidade

* Exclusão explícita de injeção

---

## Exemplo Completo

```java
@EnvPrefix("DB_")
class DatabaseConfig {

    @Env("HOST")
    String host;

    @Env("PORT")
    @Default("3306")
    int port;

    @Env("PASSWORD")
    @RequiredEnv
    String password;

    @EnvIgnore
    String debug;
}
```

### .env

```
DB_HOST=localhost
DB_PASSWORD=123
```

### Resultado da Injeção

| Campo    | Valor     |
| -------- | --------- |
| host     | localhost |
| port     | 3306      |
| password | 123       |
| debug    | null      |

---

## O que NÃO faz parte do Dotenv

Estas responsabilidades devem ficar fora do módulo dotenv:

| Feature     | Motivo                  |
| ----------- | ----------------------- |
| @Decrypt    | é segurança, não config |
| @Profile    | é ambiente de runtime   |
| @Reloadable | é hot reload            |
| @EnvGroup   | redundante              |

---

## Princípio Arquitetural

Este design segue a regra:

> 1 annotation = 1 responsabilidade

Isso garante:

* API previsível
* composição fluente
* fácil extensão
* manutenção a longo prazo

---

## Resumo Final

O sistema dotenv deve conter apenas:

* @Env
* @Default
* @RequiredEnv
* @EnvPrefix
* @EnvIgnore

Qualquer funcionalidade além disso pertence a outros módulos (security, runtime, framework, etc).
Essas annotations fornecem uma base sólida para mapear variáveis de ambiente para campos Java, mantendo o sistema simples, modular e fácil de usar.