# 📚 Obsidian JSON API - Wiki Completa

Bem-vindo à documentação wiki da API JSON do Obsidian! Este guia cobre desde conceitos básicos até técnicas avançadas de manipulação de JSON em Java.

## 📑 Índice Completo

Acesse o **[Guia Completo da Wiki](./wiki/README.md)** para uma navegação melhor!

### Rápido Acesso aos Documentos

1. **[Introdução](./wiki/01-introducao.md)** - O que é a API JSON do Obsidian
2. **[Instalação e Configuração](./wiki/02-instalacao.md)** - Como começar
3. **[Conceitos Fundamentais](./wiki/03-conceitos-fundamentais.md)** - Entenda a base
4. **[JsonElement: Trabalhando com Elementos](./wiki/04-json-element.md)** - Core da API
5. **[JsonObject: Objetos JSON](./wiki/05-json-object.md)** - Trabalhando com chave-valor
6. **[JsonArray: Arrays JSON](./wiki/06-json-array.md)** - Listas e coleções
7. **[JsonMapper: Serialização e Desserialização](./wiki/07-json-mapper.md)** - Conversão de dados
8. **[TypeRef: Generics e Tipos Complexos](./wiki/08-type-ref.md)** - Segurança de tipos
9. **[Anotações: Controlando Serialização](./wiki/09-anotacoes.md)** - Customize o comportamento
10. **[Codecs Customizados](./wiki/10-codecs-customizados.md)** - Extensões avançadas
11. **[Configuração: JsonConfig Builder](./wiki/11-configuracao.md)** - Personalize tudo
12. **[Tratamento de Erros](./wiki/12-tratamento-erros.md)** - Lidar com exceções
13. **[I/O de Arquivos](./wiki/13-arquivo-io.md)** - Ler e escrever arquivos
14. **[Padrões Práticos](./wiki/14-padroes-praticos.md)** - Best practices
15. **[Exemplos Completos](./wiki/15-exemplos-completos.md)** - Casos de uso reais

---

## 🚀 Comece Rápido

Se você é novo na API JSON do Obsidian, comece aqui:

```java

import io.obsidian.json.codec.TypeRef;

// 1. Criar um mapper padrão
JsonMapper mapper = Json.defaultMapper();

        // 2. Parsear um JSON string
        JsonElement element = mapper.parse(JsonSource.of("{\"name\":\"João\"}"));

        // 3. Desserializar para um objeto
        User user = mapper.decode(
                JsonSource.of("{\"name\":\"João\",\"age\":30}"),
                TypeRef.of(User.class)
        );

        // 4. Serializar um objeto para JSON
        String json = mapper.stringify(mapper.encode(user));

        // 5. Trabalhar com arquivos
        User loaded = JsonFiles.read(Paths.get("user.json"), TypeRef.of(User.class));
JsonFiles.

        write(Paths.get("output.json"),user);
```

---

## 📖 Navegação

Escolha o tópico pelo qual tem interesse:

### Para Iniciantes
- Comece com [Introdução](./wiki/01-introducao.md)
- Depois [Instalação](./wiki/02-instalacao.md)
- Aprenda [Conceitos Fundamentais](./wiki/03-conceitos-fundamentais.md)

### Uso Básico
- [JsonElement](./wiki/04-json-element.md) - Entender a hierarquia
- [JsonObject](./wiki/05-json-object.md) - Trabalhar com objetos
- [JsonArray](./wiki/06-json-array.md) - Trabalhar com listas

### Conversão de Dados
- [JsonMapper](./wiki/07-json-mapper.md) - Serialização/Desserialização
- [TypeRef](./wiki/08-type-ref.md) - Tipos genéricos
- [I/O de Arquivos](./wiki/13-arquivo-io.md) - Persistência

### Personalização
- [Anotações](./wiki/09-anotacoes.md) - Controlar comportamento
- [Codecs Customizados](./wiki/10-codecs-customizados.md) - Lógica custom
- [Configuração](./wiki/11-configuracao.md) - Ajustar comportamento

### Avançado
- [Tratamento de Erros](./wiki/12-tratamento-erros.md) - Robustez
- [Padrões Práticos](./wiki/14-padroes-praticos.md) - Best practices
- [Exemplos Completos](./wiki/15-exemplos-completos.md) - Aplicações reais

---

## ⭐ Recursos Principais

### 🎯 API-First Design
A API JSON do Obsidian foi projetada com o usuário em mente, oferecendo uma interface limpa e intuitiva que nunca expõe detalhes de implementação.

### 🔄 Independência de Engine
Usa Google Gson internamente, mas pode ser trocado sem quebrar seu código, graças à abstração.

### 🔒 Imutabilidade
Configuração thread-safe e imutável para ambientes multi-threaded sem preocupações.

### 💥 Fail-Fast
Mensagens de erro claras com rastreamento de caminho JSON para debug rápido.

### 🧩 Extensibilidade
Codecs customizados, anotações poderosas e configuração flexível para todos os casos de uso.

---

## 📚 Documentação Adicional

- **[JSON README](./json_readme.md)** - Visão geral do módulo
- **[Arquitetura e Design](./json_architecture_and_design.md)** - Detalhes técnicos
- **[JavaDoc](../../../build/docs/javadoc/obsidian/index.html)** - API reference

---

## 🤔 Perguntas Frequentes

**P: Qual é a diferença entre JsonElement e JsonObject?**
R: `JsonElement` é a classe abstrata base que representa qualquer elemento JSON. `JsonObject` é uma implementação concreta que representa um objeto JSON (chave-valor).

**P: Como trabalhar com tipos genéricos?**
R: Use `TypeRef` para representar tipos genéricos de forma type-safe. Veja [TypeRef](./wiki/08-type-ref.md).

**P: Posso usar meu próprio codec de serialização?**
R: Sim! Implemente `JsonCodec<T>` e use a anotação `@JsonAdapter`. Veja [Codecs Customizados](./wiki/10-codecs-customizados.md).

**P: Como tratar erros de parsing?**
R: Capture `JsonParseException` ou suas subclasses. Veja [Tratamento de Erros](./wiki/12-tratamento-erros.md).

---

## 🎓 Tabela de Referência Rápida

| Tarefa | Classe Principal | Referência |
|--------|-----------------|-----------|
| Parsear JSON | `JsonMapper.parse()` | [JsonMapper](./wiki/07-json-mapper.md) |
| Criar objeto JSON | `JsonObject` | [JsonObject](./wiki/05-json-object.md) |
| Criar array JSON | `JsonArray` | [JsonArray](./wiki/06-json-array.md) |
| Desserializar | `JsonMapper.decode()` | [JsonMapper](./wiki/07-json-mapper.md) |
| Serializar | `JsonMapper.encode()` | [JsonMapper](./wiki/07-json-mapper.md) |
| Ler arquivo | `JsonFiles.read()` | [I/O](./wiki/13-arquivo-io.md) |
| Escrever arquivo | `JsonFiles.write()` | [I/O](./wiki/13-arquivo-io.md) |
| Tipos genéricos | `TypeRef<T>` | [TypeRef](./wiki/08-type-ref.md) |
| Customizar campo | Anotações `@Json*` | [Anotações](./wiki/09-anotacoes.md) |
| Serialização custom | `JsonCodec<T>` | [Codecs](./wiki/10-codecs-customizados.md) |

---

## 💡 Dicas Importantes

- ✅ Sempre use `TypeRef` para tipos genéricos
- ✅ Use anotações `@JsonRequired` para validação
- ✅ Implemente `JsonCodec` para tipos não suportados
- ✅ Use `JsonMapper` builder para configurações customizadas
- ✅ Sempre trate exceções `JsonException`
- ✅ Use `JsonFiles` para operações de arquivo
- ✅ Aproveite as facilidades de pretty-print para debug

---

## 🔗 Links Úteis

- [Obsidian GitHub](https://github.com/nadezhdkov/Obsidian)
- [Google Gson](https://github.com/google/gson)
- [JSON Specification](https://www.json.org)

---

**Última atualização:** Janeiro de 2026  
**Versão:** 0.1.0
