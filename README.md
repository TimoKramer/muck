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
http://localhost:8080
```

## Configuration

Edit `src/main/resources/application.yaml` to configure:

```yaml
server:
  port: 8080        # Web server port
  host: "0.0.0.0"   # Bind address

bob:
  url: "http://localhost:7777"  # Bob API URL
```

## Project Structure

```
src/main/java/muck/
├── Main.java                    # Application entry point
├── client/
│   └── BobClient.java           # REST client for Bob API
├── handlers/
│   ├── HomeHandler.java         # Main page handler
│   └── PipelineHandler.java    # Pipeline list handler (HTMX)
└── model/
    └── Pipeline.java            # Pipeline data model

src/main/resources/
├── api.yaml                     # Bob OpenAPI specification
├── application.yaml             # Application configuration
└── templates/
    ├── index.ftl                # Main page template
    └── pipelines.ftl            # Pipeline list fragment
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

### Building

```bash
mvn clean package
```

### Running Tests

```bash
mvn test
```

### Code Style

- Java 21+ features encouraged
- Use virtual threads for I/O operations
- Keep handlers simple and focused
- Templates contain only presentation logic

## HTMX + DaisyUI Features

The application uses HTMX for dynamic updates and DaisyUI for beautiful components:

- **Auto-refresh**: Pipeline list updates every 5 seconds
- **Loading indicators**: DaisyUI loading spinners provide visual feedback
- **Smooth transitions**: Tailwind CSS animations
- **Server-side rendering**: All logic runs on the server
- **Dark theme**: Uses DaisyUI's dark theme by default
- **Responsive cards**: Pipeline cards with hover effects
- **Status badges**: Color-coded badges for pipeline status

Example HTMX usage in `index.ftl`:

```html
<div id="pipeline-list"
     hx-get="/pipelines"
     hx-trigger="load, every 5s"
     hx-indicator="#loading-indicator">
    <div class="flex flex-col items-center justify-center py-16">
        <span class="loading loading-dots loading-lg"></span>
        <p class="mt-4 text-lg opacity-60">Loading pipelines...</p>
    </div>
</div>
```

## API Integration

The application integrates with Bob's REST API using:

1. **OpenAPI Spec**: Loaded from `resources/api.yaml`
2. **Helidon WebClient**: Type-safe HTTP client
3. **Swagger Parser**: Runtime validation

Key endpoints used:

- `GET /api/pipelines` - List all pipelines
- `GET /api/pipelines/{group}/{name}/status` - Get pipeline status
- `POST /api/pipelines/{group}/{name}/start` - Trigger pipeline

## Troubleshooting

### Bob not responding

Ensure Bob is running:

```bash
docker-compose ps
curl http://localhost:7777/api/pipelines
```

### Port already in use

Change the port in `application.yaml`:

```yaml
server:
  port: 8081  # Use different port
```

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
