# Muck - Bob CI/CD Monitor

A modern web-based monitoring application for [Bob CI/CD](https://github.com/bob-cd/bob) built with Helidon Nima and HTMX.

## Features

- **Real-time Pipeline Monitoring**: Auto-refreshing pipeline list using HTMX
- **Server-Side Rendering**: No complex JavaScript frameworks, just clean HTML
- **OpenAPI Integration**: Client validates against Bob's OpenAPI specification
- **Virtual Threads**: Built on Helidon Nima with Java 21's Project Loom for efficient concurrency
- **Modern UI**: Beautiful, responsive interface built with DaisyUI and Tailwind CSS

## Requirements

- **Java 21+** (required for virtual threads)
- **Maven 3.8+**
- **Bob CI/CD server** running (default: http://localhost:7777)

## Quick Start

### 1. Start Bob CI/CD

Using the included docker-compose:

```bash
docker-compose up -d
```

This will start:
- Bob API server (port 7777)
- PostgreSQL database
- RabbitMQ message queue
- etcd distributed storage
- Supporting services (runner, artifact store, etc.)

### 2. Build the Application

Use JDK Version 21 or higher

```bash
mvn clean package
```

### 3. Run the Application

```bash
java -jar target/muck-1.0.0.jar
```

Or use Maven directly:

```bash
mvn exec:java -Dexec.mainClass=muck.Main
```

### 4. Access the Web UI

Open your browser to:

```
http://localhost:7999
```

## Configuration

Edit `src/main/resources/application.yaml` to configure:

```yaml
server:
  port: 7999        # Web server port
  host: "0.0.0.0"   # Bind address

bob:
  url: "http://localhost:7777"  # Bob API URL
```

## Technology Stack

- **[Helidon Nima](https://helidon.io/)**: Modern Java web framework with virtual threads
- **[HTMX](https://htmx.org/)**: High-power tools for HTML
- **[DaisyUI](https://daisyui.com/)**: Tailwind CSS component library
- **[Tailwind CSS](https://tailwindcss.com/)**: Utility-first CSS framework
- **[FreeMarker](https://freemarker.apache.org/)**: Template engine
- **[OpenAPI](https://swagger.io/specification/)**: API specification and validation
- **Java 21**: Virtual threads (Project Loom)

## Development

### Running Tests

```bash
mvn test
```

### Code Style

This project uses modern Java 21+ syntax. See **[JAVA_STYLE.md](JAVA_STYLE.md)** for detailed guidelines including:

- Type inference with `var`
- Records for data classes
- Switch expressions
- Text blocks
- Streams and functional programming
- Code organization and formatting

Quick summary:
- Use `var` where type is obvious
- Prefer records over traditional classes for data
- Use switch expressions over statements
- Leverage Java 21+ features
- See `.editorconfig` for formatting rules

### Java version issues

Verify Java 21+:

```bash
java -version
```

Should show version 21 or higher.

## License

This project is open source.

## Contributing

Contributions welcome! Please ensure:

- Code compiles with Java 21
- Templates are valid FreeMarker
- HTMX attributes are properly used
- OpenAPI spec validation passes
