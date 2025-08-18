# FreightFox Chat Application

A real-time WebSocket chat application built with Spring Boot, Redis pub/sub, and deployed on AWS EKS with auto-scaling capabilities.

## Features Implemented

### **Chat Rooms**
- **Create chat rooms** with unique names
- **Join existing chat rooms** with participant validation
- **Room management** with proper error handling for duplicates

### **Messaging** 
- **Send and retrieve messages** in chat rooms
- **Store messages in Redis** using Redis List data structure
- **Retrieve last N messages** from any chat room
- **Message persistence** with Redis durability

### **Real-time Messaging**
- **Redis Pub/Sub** for instant message delivery across all connected users
- **WebSocket connections** for real-time bidirectional communication
- **Multi-pod support** for horizontal scaling

### **Error Handling**
- **Duplicate chat room names** - Prevents creation of rooms with existing names
- **Non-existent chat rooms** - Validates room existence before allowing messages
- **Invalid participants** - Ensures only joined users can send messages
- **Connection failures** - Graceful handling of WebSocket disconnections

### **Persistence & Durability**
- **Redis persistence** with snapshots and append-only file (AOF)
- **Message history** stored permanently in Redis Lists
- **Session management** with Redis for multi-pod deployments

## Live Demo

**Hosted URL (India - Mumbai):**
```
http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com
```

**Test the API:**
- **Health Check**: [/health](http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/health)
- **Swagger UI**: [/swagger-ui/index.html](http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/swagger-ui/index.html)
- **Create Room**: POST `/api/chatapp/chatrooms/`
- **WebSocket**: `ws://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/ws?room=test&participant=user`

## Postman Collections

Import the complete API collections to test all endpoints:

**REST API Collection:**
- **[Postman Collection JSON](./postman-collection.json)** - Local file for REST API testing

**WebSocket Collection:**
- **[WebSocket Postman Collection](https://orange-station-108385.postman.co/workspace/My-Workspace~e5aa16b9-1b4b-4a85-8c59-9deb007b20c1/collection/67fbd689be3dfe9bc0f646f5?action=share&creator=23006953)** - Online collection for WebSocket testing

```bash
# Quick test commands:
# Create a room
curl -X POST http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/api/chatapp/chatrooms/ \
  -H 'Content-Type: application/json' \
  -d '{"roomName":"test-room"}'

# Join a room
curl -X POST http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/api/chatapp/chatrooms/test-room/join \
  -H 'Content-Type: application/json' \
  -d '{"participant":"john"}'
```

## Local Setup with Docker

### Prerequisites
- Docker & Docker Compose
- Java 21 (for development)

### Option 1: Quick Start with Docker Compose
```bash
# 1. Clone the repository
git clone <your-repo-url>
cd freight-fox-chat-app

# 2. Start Redis
docker-compose -f redis-docker-compose.yml up -d

# 3. Build and run the application
./mvnw spring-boot:run
```

### Option 2: Full Docker Setup
```bash
# 1. Start Redis
docker-compose -f redis-docker-compose.yml up -d

# 2. Build Docker image
docker build -t freight-fox-chat .

# 3. Run the application
docker run -p 8080:8080 \
  -e SPRING_DATA_REDIS_HOST=host.docker.internal \
  -e SPRING_DATA_REDIS_PORT=6379 \
  freight-fox-chat
```

### Option 3: Development Setup
```bash
# 1. Start Redis
docker-compose -f redis-docker-compose.yml up -d

# 2. Run application in development mode
./mvnw spring-boot:run

# Application will be available at: http://localhost:8080
```


## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/health` | Health check |
| `POST` | `/api/chatapp/chatrooms/` | Create chat room |
| `POST` | `/api/chatapp/chatrooms/{roomName}/join` | Join room |
| `POST` | `/api/chatapp/chatrooms/{roomName}/leave` | Leave room |
| `GET` | `/api/chatapp/chatrooms/{roomName}/participants` | Get participants |
| `DELETE` | `/api/chatapp/chatrooms/{roomName}` | Delete room |
| `WS` | `/ws?room={roomName}&participant={name}` | WebSocket connection |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATA_REDIS_HOST` | `localhost` | Redis host |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis port |
| `SERVER_PORT` | `8080` | Application port |

## Cloud Deployment

The application is deployed on AWS EKS in Mumbai (ap-south-1) region with auto-scaling, load balancing, and Redis for session management.

## Testing WebSocket

### Using Postman Collection
Import the **[WebSocket Postman Collection](https://orange-station-108385.postman.co/workspace/My-Workspace~e5aa16b9-1b4b-4a85-8c59-9deb007b20c1/collection/67fbd689be3dfe9bc0f646f5?action=share&creator=23006953)** for easy WebSocket testing with pre-configured requests.

### Using Browser Console
```javascript
// Connect to WebSocket
const ws = new WebSocket('ws://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/ws?room=test&participant=user');

// Send message
ws.send(JSON.stringify({
  "room": "test",
  "participant": "user", 
  "message": "Hello World!",
  "timestamp": new Date().toISOString()
}));

// Listen for messages
ws.onmessage = (event) => {
  console.log('Received:', JSON.parse(event.data));
};
```

### Using Node.js Test Script
```bash
# Run the WebSocket test
node test-websocket.js
```

## Monitoring

Check application status:
```bash
# Health check
curl http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/health

# For local development
curl http://localhost:8080/health
```

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   ```bash
   # Check Redis is running
   docker ps | grep redis
   
   # Restart Redis
   docker-compose -f redis-docker-compose.yml restart
   ```

2. **WebSocket Connection Issues**
   - Ensure room exists and participant has joined
   - Check browser console for error messages
   - Verify WebSocket URL format

3. **Application Won't Start**
   ```bash
   # Check Java version
   java -version  # Should be 21+
   
   # Clean and rebuild
   ./mvnw clean package
   ```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.

---

**Made with passion for real-time chat communication**

**Quick Links:**
- [Live Demo](http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com)
- [Health Check](http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/health)
- [Swagger UI](http://af89a7a02e53d467fbb316f5173ee150-446467663.ap-south-1.elb.amazonaws.com/swagger-ui/index.html)
- [REST API Postman Collection](./postman-collection.json)
- [WebSocket Postman Collection](https://orange-station-108385.postman.co/workspace/My-Workspace~e5aa16b9-1b4b-4a85-8c59-9deb007b20c1/collection/67fbd689be3dfe9bc0f646f5?action=share&creator=23006953)