# 🔮 Obsidian

Un framework Java moderno y minimalista con bibliotecas de utilidades poderosas para simplificar tareas comunes de desarrollo.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](../../../LICENSE.md)
[![Java Version](https://img.shields.io/badge/java-11%2B-orange.svg)](https://www.java.com/)
[![Maven Central](https://img.shields.io/badge/maven--central-1.0.0-brightgreen.svg)]()

**📖 Idiomas Disponibles:** [English](../../../README.md) | [Português Brasileiro](../pt-BR/README.md) | [Deutsch](../de-DE/README.md) | [Español](./README.md)

## 📚 Acerca de

Obsidian es una colección de módulos independientes y altamente especializados que facilitan el desarrollo en Java. Cada módulo está diseñado para resolver problemas específicos con una API clara, fluida y fácil de usar.

### Módulos Disponibles

#### 🔧 [Obsidian Configuration](../../../docs/subproject/dotenv/dotenv_readme.md)

Sistema minimalista de **anotaciones** para mapear variables de entorno (`.env`) a campos Java.

**Características:**
- ✅ Mapeo automático de variables de entorno
- ✅ Soporte para valores por defecto
- ✅ Validación de variables obligatorias
- ✅ Prefijos automáticos
- ✅ Conversión de tipos automática
- ✅ Ignorar campos específicos

**Ejemplo Rápido:**
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

Una API fluida y poderosa para trabajar con reflexión en Java de forma simple e intuitiva.

**Características:**
- ✅ API fluida con encadenamiento de métodos
- ✅ Filtros poderosos para campos y métodos
- ✅ Type-safe siempre que sea posible
- ✅ Patrón Constructor integrado
- ✅ Manipulación fácil de anotaciones
- ✅ Sin código boilerplate

**Ejemplo Rápido:**
```java
// Acceder a campos
Reflect.on(User.class)
    .fields()
    .named("name")
    .set(user, "John");

// Invocar métodos
Reflect.on(user)
    .methods()
    .getters()
    .each(System.out::println);

// Crear instancias
User user = Reflect.on(User.class).newInstance("John", 30);
```

## 🚀 Quick Start

### Instalación

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

### Ejemplo Completo

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
        // Cargar configuración de .env
        Application app = new Application();
        Dotenv.load(app);
        
        // Usar reflexión para inspeccionar la aplicación
        Reflect.on(app)
            .fields()
            .each(f -> System.out.println(f.getName() + " = " + f.get(app)));
    }
}
```

## 📖 Documentación

- [Obsidian Configuration - Documentación Completa](../../../docs/subproject/dotenv/dotenv_readme.md)
- [Obsidian Reflection - Documentación Completa](../../../docs/subproject/reflect/reflect_readme.md)
- [Arquitectura JSON](../../subproject/json/json_architecture_and_design.md)

## 🛠️ Desarrollo

### Requisitos

- **Java 11** o superior
- **Gradle 7.0** o superior
- **Make** (opcional, para usar comandos de Makefile)

### Construir el Proyecto

```bash
# Build completo
make build

# Ejecutar pruebas
make test

# Limpiar build
make clean

# Generar documentación
make javadoc

# Publicar localmente
make local
```

O usando Gradle directamente:

```bash
# Build
./gradlew build

# Pruebas
./gradlew test

# Verificar
./gradlew check
```

### Estructura del Proyecto

```
Obsidian/
├── obsidian-configuration/   # Módulo de configuración y .env
│   ├── src/main/java/
│   │   └── io/dotenv/
│   │       ├── annotations/   # Anotaciones @Env, @Default, etc
│   │       ├── core/          # Implementación core
│   │       ├── processor/     # Procesadores
│   │       └── util/          # Utilidades
│   └── build.gradle.kts
│
├── obsidian-reflection/      # Módulo de reflexión
│   ├── src/main/java/
│   │   └── lang/reflect/
│   │       ├── Reflect*.java  # Clases principales
│   │       └── exception/     # Excepciones
│   └── build.gradle.kts
│
├── obsidian-promise/         # Módulo de promises
│   ├── src/main/java/
│   │   └── io/obsidian/promise/
│   │       ├── api/           # API Promise
│   │       ├── combinators/   # Combinadores de promises
│   │       ├── error/         # Manejo de errores
│   │       └── internal/      # Implementación interna
│   └── build.gradle.kts
│
├── src/main/java/obsidian/  # Utilidades core
│   ├── control/              # When y control de flujo
│   ├── functional/           # Try<T> y utilidades funcionales
│   ├── util/                 # Box<T>, Sequence<T>, Range
│   │   ├── concurrent/       # Contenedores thread-safe
│   │   └── stream/           # Utilidades de stream
│   └── api/                  # APIs core
│
├── docs/                     # Documentación
├── gradle/                   # Gradle wrapper
├── build.gradle.kts          # Configuración de build raíz
├── settings.gradle.kts       # Configuración de subproyectos
├── Makefile                  # Targets de desarrollo
└── README.md                 # Este archivo
```

## 📦 Dependencias

- **JetBrains Annotations** - Anotaciones para mejor análisis de código
- **Gson** - Serialización JSON
- **YAML** - Soporte YAML
- **Lombok** - Reducción de boilerplate
- **JUnit 5** - Framework de pruebas

## 🤝 Contribuyendo

¡Las contribuciones son bienvenidas! Para contribuir:

1. Haz Fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Confirma tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Empuja a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está licenciado bajo la [Apache License 2.0](../../../LICENSE.md) - ver el archivo de licencia para más detalles.

## 👨‍💻 Autor

Desarrollado con ❤️ por Nadezhda

## 🎯 Roadmap

- [ ] Soporte YAML avanzado
- [ ] Caché inteligente de reflexión
- [ ] Validación automática de campos
- [ ] Integración con Spring Framework
- [ ] Plugin Gradle dedicado
- [ ] Documentación multiidioma

## 💡 Consejos y Trucos

### Usando Dotenv con Reflection

```java
@Env("DB_HOST")
String host;

// Acceder a todos los campos anotados con @Env
Reflect.on(this)
    .fields()
    .annotated(Env.class)
    .each(f -> System.out.println(f.getName()));
```

### Filtrando Métodos

```java
// Encontrar todos los getters
List<Method> getters = Reflect.on(User.class)
    .methods()
    .getters()
    .list();

// Filtros personalizados
Reflect.on(service)
    .methods()
    .isPublic()
    .notStatic()
    .startWith("handle")
    .each(method -> /* procesar */);
```

## 🐛 Reportar Problemas

¿Encontraste un bug? Por favor abre un [issue](https://github.com/nadezhdkov/obsidian/issues) con:

- Descripción clara del problema
- Pasos para reproducir
- Comportamiento esperado vs actual
- Versión de Java y Obsidian

## 📞 Soporte

Para soporte adicional:

- 📧 Email: seu-email@exemplo.com
- 💬 GitHub Issues
- 📚 Consulta la documentación completa en las carpetas `/docs`

---

**Desarrollado con ❤️ usando Java y Gradle**
