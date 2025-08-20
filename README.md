# FreightFox Chat Application

## Features

- Create and manage chat rooms
- Real-time messaging with WebSocket
- Redis pub/sub for scalability
- Participant management
- Message history persistence
- RESTful API endpoints
- Health check endpoints
- Comprehensive error handling
- Input validation

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Redis server (local or remote)
- Docker (optional)

### Redis Requirement

You need Redis running before starting the application. You can use:

1. **Local Redis installation**
2. **Docker Redis container**
3. **Cloud Redis service**

Default connection: `localhost:6379`

## Quick Start (Recommended)

### One-Command Setup
```bash
# Download and run the quick-start script
curl -sSL https://raw.githubusercontent.com/your-repo/freight-fox-chat-app/main/quick-start.sh | bash

# Or if you have the repository:
./quick-start.sh
```

### Docker Only (Super Quick)
```bash
# Just Docker with Redis
./docker-run.sh
```

## Local Development Setup

### 1. Clone and Setup

```bash
git clone <repository-url>
cd freight-fox-chat-app
```

### 2. Start Redis

#### Option A: Docker (Recommended)
```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

#### Option B: Docker Compose
```bash
docker-compose -f redis-docker-compose.yml up -d
```

#### Option C: Local Redis Installation
```bash
# Install Redis locally and start it
redis-server
```

### 3. Configure Redis Connection

#### Option A: Environment Variables
```bash
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
export SERVER_PORT=8080
```

#### Option B: Update application.properties
```properties
# Edit src/main/resources/application.properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0
```

### 4. Build and Run

```bash
# Build the application
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### 5. API Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## API Endpoints

### Chat Room Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chatapp/chatrooms/` | Create chat room |
| POST | `/api/chatapp/chatrooms/{roomName}/join` | Join room |
| POST | `/api/chatapp/chatrooms/{roomName}/leave` | Leave room |
| GET | `/api/chatapp/chatrooms/{roomName}/participants` | Get participants |
| DELETE | `/api/chatapp/chatrooms/{roomName}` | Delete room |

### Message Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/chatapp/messages/` | Send message |
| GET | `/api/chatapp/messages/{roomName}` | Get messages |

### WebSocket

| Protocol | Endpoint | Description |
|----------|----------|-------------|
| WS | `/ws?room={roomName}&participant={name}` | WebSocket connection |

### Health Check
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |

## Usage Examples

### Create Chat Room
```bash
curl -X POST "http://localhost:8080/api/chatapp/chatrooms/" \
  -H "Content-Type: application/json" \
  -d '{"roomName":"general"}'
```

### Join Room
```bash
curl -X POST "http://localhost:8080/api/chatapp/chatrooms/general/join" \
  -H "Content-Type: application/json" \
  -d '{"participant":"john"}'
```

### Send Message
```bash
curl -X POST "http://localhost:8080/api/chatapp/messages/" \
  -H "Content-Type: application/json" \
  -d '{
    "room":"general",
    "participant":"john",
    "message":"Hello everyone!"
  }'
```

### Get Messages
```bash
curl "http://localhost:8080/api/chatapp/messages/general?limit=10"
```

### Health Check
```bash
curl "http://localhost:8080/health"
```

### WebSocket Connection (JavaScript)
```javascript
// Connect to WebSocket
const ws = new WebSocket('ws://localhost:8080/ws?room=general&participant=john');

// Send message
ws.send(JSON.stringify({
  "room": "general",
  "participant": "john", 
  "message": "Hello World!",
  "timestamp": new Date().toISOString()
}));

// Listen for messages
ws.onmessage = (event) => {
  console.log('Received:', JSON.parse(event.data));
};
```

## Configuration

### Application Properties

```properties
# Application Configuration
spring.application.name=freight-fox-chat-app
server.port=8080

# Redis Configuration
spring.data.redis.host=${SPRING_DATA_REDIS_HOST:localhost}
spring.data.redis.port=${SPRING_DATA_REDIS_PORT:6379}
spring.data.redis.database=0
spring.data.redis.timeout=2000ms
spring.data.redis.jedis.pool.max-active=8
spring.data.redis.jedis.pool.max-idle=8
spring.data.redis.jedis.pool.min-idle=0
```

## Development

### Running Tests
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### Building for Production
```bash
# Build optimized JAR
mvn clean package -Dmaven.test.skip=true

# Build Docker image
docker build -t freight-fox-chat .
```

## Docker Support

### Build and Run with Docker
```bash
# Build image
docker build -t freight-fox-chat .

# Start Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Run container (replace with your Redis host if different)
docker run -p 8080:8080 \
  -e SPRING_DATA_REDIS_HOST=localhost \
  -e SPRING_DATA_REDIS_PORT=6379 \
  freight-fox-chat
```

### Docker Compose
```bash
# Start both Redis and application
docker-compose up -d
```

## WebSocket Testing

### Using Browser Console
```javascript
// Connect
const ws = new WebSocket('ws://localhost:8080/ws?room=test&participant=user1');

// Send message
ws.send(JSON.stringify({
  "room": "test",
  "participant": "user1",
  "message": "Hello!",
  "timestamp": new Date().toISOString()
}));

// Receive messages
ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log(`${data.participant}: ${data.message}`);
};
```

### Using Postman
1. Create new WebSocket request
2. URL: `ws://localhost:8080/ws?room=test&participant=user1`
3. Send JSON messages in the format above

## Architecture

### Components
- **Spring Boot** - Web framework and REST API
- **WebSocket** - Real-time bidirectional communication
- **Redis** - Message persistence and pub/sub
- **Spring Data Redis** - Redis integration
- **Jackson** - JSON serialization

### Message Flow
1. Client connects via WebSocket
2. Messages sent through WebSocket or REST API
3. Messages stored in Redis lists
4. Messages published to Redis pub/sub
5. All connected clients receive real-time updates

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   - Verify Redis is running: `redis-cli ping`
   - Check host and port configuration
   - Ensure Redis is accessible from application

2. **WebSocket Connection Failed**
   - Check if port 8080 is available
   - Verify WebSocket URL format
   - Check browser console for errors

3. **Application Won't Start**
   - Check Java version (requires Java 21+)
   - Verify Redis connection
   - Check port 8080 availability

## Performance

### Redis Configuration
- **Connection pooling** with configurable pool sizes
- **Pub/Sub** for real-time messaging
- **Lists** for message history storage
- **TTL** support for automatic cleanup

### Scaling
- **Horizontal scaling** supported through Redis pub/sub
- **Load balancer** compatible
- **Session-less** design for stateless scaling
