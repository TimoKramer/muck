# Java Code Style Guide for Muck

This project uses **modern Java syntax** (Java 21+) while maintaining professional structure with proper classes and imports.

## Core Principles

1. **Use modern Java features** - var, text blocks, pattern matching, records, etc.
2. **Keep proper structure** - Classes, imports, and organization matter
3. **Type inference with `var`** - Use where type is obvious from context
4. **Simplified syntax** - Leverage Java 21+ features for cleaner code

## Modern Java Features to Use

### 1. Type Inference with `var`

**Use `var` when the type is obvious:**

```java
// Good ✓
var server = WebServer.builder().build();
var pipelines = bobClient.listPipelines();
var config = Config.create();

// Avoid when type is not clear ✗
var result = process(); // What type is this?
```

**Keep explicit types when clarity matters:**

```java
// Good ✓
Configuration freemarkerConfig = createFreemarkerConfig();
Http1Client client = Http1Client.builder().build();
```

### 2. Text Blocks (Multi-line Strings)

**Use text blocks for HTML, JSON, SQL:**

```java
// Good ✓
var html = """
    <div class="card">
        <h1>%s</h1>
    </div>
    """.formatted(title);

// Old way ✗
String html = "<div class=\"card\">\n" +
              "    <h1>" + title + "</h1>\n" +
              "</div>";
```

### 3. Records (Immutable Data Classes)

**Use records for simple data carriers:**

```java
// Good ✓
public record Pipeline(String group, String name, String status) {
    public String fullName() {
        return group + "/" + name;
    }
}

// Instead of verbose class ✗
public class Pipeline {
    private final String group;
    private final String name;
    // ... constructor, getters, equals, hashCode, toString
}
```

### 4. Pattern Matching for instanceof

```java
// Good ✓
if (obj instanceof String s) {
    return s.toUpperCase();
}

// Old way ✗
if (obj instanceof String) {
    String s = (String) obj;
    return s.toUpperCase();
}
```

### 5. Switch Expressions

```java
// Good ✓
var badgeClass = switch (status) {
    case "running" -> "badge-success";
    case "passed" -> "badge-info";
    case "failed" -> "badge-error";
    default -> "badge-ghost";
};

// Old way ✗
String badgeClass;
switch (status) {
    case "running":
        badgeClass = "badge-success";
        break;
    // ...
}
```

### 6. Enhanced NPE Messages

Java 17+ gives you helpful NullPointerException messages automatically. Leverage this instead of excessive null checks.

### 7. Sealed Classes (When Needed)

```java
public sealed interface ApiResponse
    permits Success, Error, Loading {
}

public record Success(String data) implements ApiResponse {}
public record Error(String message) implements ApiResponse {}
public record Loading() implements ApiResponse {}
```

## Code Organization

### Imports

- Use explicit imports, not wildcards (except in rare cases)
- Group imports: Java standard library, third-party, project
- Remove unused imports

### Class Structure

```java
package muck.handlers;

import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.List;

public class MyHandler {
    // 1. Constants
    private static final String DEFAULT_THEME = "dark";

    // 2. Fields
    private final BobClient client;

    // 3. Constructor
    public MyHandler(BobClient client) {
        this.client = client;
    }

    // 4. Public methods
    public void handle(ServerRequest req, ServerResponse res) {
        // ...
    }

    // 5. Private methods
    private List<Pipeline> fetchPipelines() {
        // ...
    }
}
```

## Formatting

### Indentation
- **Tabs** for indentation (4-space width)
- See `.editorconfig` for details

### Braces
```java
// Good ✓
if (condition) {
    doSomething();
}

// Avoid (even for single lines)
if (condition)
    doSomething();
```

### Line Length
- Aim for 120 characters max
- Break long lines logically

## Error Handling

### Use Modern Try-Catch

```java
// Good ✓
try {
    var data = fetchData();
    process(data);
} catch (IOException e) {
    LOGGER.log(Level.SEVERE, "Failed to fetch data", e);
    throw new RuntimeException("Data fetch failed", e);
}
```

### Avoid Swallowing Exceptions

```java
// Bad ✗
try {
    doSomething();
} catch (Exception e) {
    // ignored
}

// Good ✓
try {
    doSomething();
} catch (Exception e) {
    LOGGER.log(Level.WARNING, "Operation failed, using default", e);
    return getDefault();
}
```

## Logging

Use `java.util.logging` with meaningful levels:

```java
private static final Logger LOGGER = Logger.getLogger(ClassName.class.getName());

LOGGER.info("Server started on port " + port);
LOGGER.log(Level.WARNING, "Connection timeout for {0}", url);
LOGGER.log(Level.SEVERE, "Critical error", exception);
```

## Comments

### Javadoc for Public APIs

```java
/**
 * Fetches all pipelines from the Bob CI/CD API.
 *
 * @return list of pipelines, never null (may be empty)
 */
public List<Pipeline> listPipelines() {
    // implementation
}
```

### Inline Comments

- Explain *why*, not *what*
- Keep comments up-to-date with code
- Use `// TODO:` for planned improvements
- Use `// FIXME:` for known issues

## Testing (When Added)

```java
@Test
void shouldFetchPipelinesSuccessfully() {
    var client = new BobClient("http://localhost:7777");
    var pipelines = client.listPipelines();

    assertThat(pipelines).isNotNull();
    assertThat(pipelines).isNotEmpty();
}
```

## Modern Java Checklist

When writing new code, ask yourself:

- [ ] Can I use `var` here for cleaner code?
- [ ] Would a record be better than a class?
- [ ] Can I use a switch expression instead of statement?
- [ ] Would a text block make this string more readable?
- [ ] Am I using pattern matching where applicable?
- [ ] Are my streams and optionals idiomatic?

## References

- Java 21+ features: https://openjdk.org/projects/jdk/21/
- Helidon Nima docs: https://helidon.io/docs/v4/
- Effective Java (3rd Edition) by Joshua Bloch

---

**Remember:** Modern syntax should enhance readability, not obscure intent. When in doubt, clarity wins over cleverness.
