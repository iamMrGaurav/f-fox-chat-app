package ai.freightfox.chat.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
public class WebSocketSessionManager {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${server.port:8080}")
    private String serverPort;

    @Getter
    private String podInstanceId;

    // Local session storage (each pod stores only its own sessions)
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> localSessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void initPodInstanceId() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            this.podInstanceId = hostName + ":" + serverPort;
            log.info("Pod Instance ID initialized: {}", podInstanceId);
        } catch (Exception e) {
            this.podInstanceId = "pod-" + System.currentTimeMillis();
            log.warn("Could not get hostname, using fallback ID: {}", podInstanceId);
        }
    }

    public void addSession(String roomName, WebSocketSession session) {
        // Store session locally on this pod
        localSessions.computeIfAbsent(roomName, k -> new CopyOnWriteArraySet<>()).add(session);
        
        log.info("Session {} added to room '{}' on pod {}. Local connections: {}", 
                session.getId(), roomName, podInstanceId, localSessions.get(roomName).size());
    }

    public void removeSession(String roomName, WebSocketSession session) {
        // Remove from local storage
        CopyOnWriteArraySet<WebSocketSession> sessions = localSessions.get(roomName);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                localSessions.remove(roomName);
            }
        }
        
        log.info("Session {} removed from room '{}' on pod {}. Local connections: {}", 
                session.getId(), roomName, podInstanceId, 
                sessions != null ? sessions.size() : 0);
    }

    // This method broadcasts to LOCAL sessions only (called by Redis subscriber)
    public void broadcastToLocalSessions(String roomName, Object message) {
        CopyOnWriteArraySet<WebSocketSession> sessions = localSessions.get(roomName);
        
        if (sessions == null || sessions.isEmpty()) {
            log.debug("No local WebSocket sessions for room '{}' on pod {}", roomName, podInstanceId);
            return;
        }

        try {
            String messageJson = objectMapper.writeValueAsString(message);
            
            log.info("Broadcasting to {} local WebSocket sessions in room '{}' on pod {}", 
                    sessions.size(), roomName, podInstanceId);
            
            // Send to all active local sessions
            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(messageJson));
                        return false; // Keep session
                    } else {
                        log.debug("Removing closed session {} from room '{}'", session.getId(), roomName);
                        return true; // Remove closed session
                    }
                } catch (Exception e) {
                    log.error("Failed to send to session {}: {}", session.getId(), e.getMessage());
                    return true; // Remove failed session
                }
            });
            
        } catch (Exception e) {
            log.error("Failed to broadcast message to local sessions: {}", e.getMessage());
        }
    }
}