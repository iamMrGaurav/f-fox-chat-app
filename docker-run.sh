#!/bin/bash

# FreightFox Chat Application - Quick Docker Run

echo "FreightFox Chat Application - Quick Docker Run"
echo "=============================================="

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker is not running! Please start Docker and try again."
    exit 1
fi

# Configuration
ECR_IMAGE="830325870732.dkr.ecr.ap-south-1.amazonaws.com/freight-fox:chat"

echo ""
echo "Configuration:"
echo "   Docker Image: $ECR_IMAGE"
echo "   Redis: Will be started automatically"
echo "   Application Port: 8080"
echo ""

# Start Redis
echo "Starting Redis container..."
docker stop freight-fox-redis 2>/dev/null || true
docker rm freight-fox-redis 2>/dev/null || true

docker run -d \
    --name freight-fox-redis \
    -p 6379:6379 \
    redis:7-alpine \
    redis-server --appendonly yes

if docker ps | grep -q freight-fox-redis; then
    echo "SUCCESS: Redis container started"
else
    echo "ERROR: Failed to start Redis container"
    exit 1
fi

# Pull and run chat application
echo "Pulling Docker image..."
docker pull $ECR_IMAGE

echo "Starting chat application..."
docker stop freight-fox-chat 2>/dev/null || true
docker rm freight-fox-chat 2>/dev/null || true

docker run -d \
    --name freight-fox-chat \
    -p 8080:8080 \
    --link freight-fox-redis:redis \
    -e SPRING_DATA_REDIS_HOST=redis \
    -e SPRING_DATA_REDIS_PORT=6379 \
    $ECR_IMAGE

echo ""
echo "Waiting for application to start..."
sleep 15

if docker ps | grep -q freight-fox-chat && docker ps | grep -q freight-fox-redis; then
    echo "SUCCESS: Application started successfully!"
    echo ""
    echo "Access your application at:"
    echo "   Main API: http://localhost:8080"
    echo "   Swagger UI: http://localhost:8080/swagger-ui/html"
    echo "   Health Check: http://localhost:8080/health"
    echo ""
    echo "Quick Test:"
    echo "   Create room: curl -X POST http://localhost:8080/api/chatapp/chatrooms/ -H 'Content-Type: application/json' -d '{\"roomName\":\"test\"}'"
    echo ""
    echo "WebSocket Connection:"
    echo "   URL: ws://localhost:8080/ws?room=test&participant=user1"
    echo ""
    echo "Management commands:"
    echo "   View chat logs: docker logs freight-fox-chat"
    echo "   View Redis logs: docker logs freight-fox-redis"
    echo "   Stop apps: docker stop freight-fox-chat freight-fox-redis"
    echo "   Remove apps: docker rm freight-fox-chat freight-fox-redis"
else
    echo "ERROR: Failed to start application"
    echo "Chat App Logs:"
    docker logs freight-fox-chat
    echo ""
    echo "Redis Logs:"
    docker logs freight-fox-redis
fi