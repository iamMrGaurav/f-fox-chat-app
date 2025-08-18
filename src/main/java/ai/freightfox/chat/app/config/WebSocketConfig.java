package ai.freightfox.chat.app.config;

import ai.freightfox.chat.app.service.ChatRoomService;
import ai.freightfox.chat.app.service.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private final ChatRoomService chatRoomService;

    @Autowired
    private WebSocketSessionManager sessionManager;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler() {

            @Override
            public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session) throws Exception {
                String roomName = (String) session.getAttributes().get("roomName");
                String participant = (String) session.getAttributes().get("participantName");
                
                log.info("WebSocket connected: {} - Room: {}, Participant: {}", session.getId(), roomName, participant);

                sessionManager.addSession(roomName, session);
            }

            @Override
            public void handleMessage(org.springframework.web.socket.WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) throws Exception {
                log.debug("WebSocket message received: {}", message.getPayload());
            }

            @Override
            public void handleTransportError(org.springframework.web.socket.WebSocketSession session, Throwable exception) throws Exception {
                log.error("WebSocket transport error: {}", exception.getMessage());
            }

            @Override
            public void afterConnectionClosed(org.springframework.web.socket.WebSocketSession session, org.springframework.web.socket.CloseStatus closeStatus) throws Exception {
                String roomName = (String) session.getAttributes().get("roomName");
                
                log.info("WebSocket disconnected: {} from room: {}", session.getId(), roomName);

                if (roomName != null) {
                    sessionManager.removeSession(roomName, session);
                }
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        }, "/ws")
                .setAllowedOrigins("*", "http://localhost:3000")
                .addInterceptors(new HandshakeInterceptor() {

                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, 
                                                 ServerHttpResponse response, 
                                                 WebSocketHandler wsHandler, 
                                                 Map<String, Object> attributes) throws Exception {

                        String query = request.getURI().getQuery();
                        String roomName = extractRoomNameFromQuery(query);
                        String participantName = extractParticipantFromQuery(query);
                        
                        log.info("Raw WebSocket connection attempt - Room: {}, Participant: {}", roomName, participantName);

                        if (roomName == null || participantName == null) {
                            log.warn("Raw WS: Connection blocked - missing room name or participant");
                            response.setStatusCode(HttpStatus.BAD_REQUEST);
                            response.getHeaders().add("X-Error-Message", "Room name and participant are required");
                            return false;
                        }

                        if (!chatRoomService.isRoomExists(roomName)) {
                            log.warn("Raw WS: Connection blocked - room '{}' does not exist", roomName);
                            response.setStatusCode(HttpStatus.NOT_FOUND);
                            response.getHeaders().add("X-Error-Message", "Room '" + roomName + "' does not exist");
                            return false;
                        }

                        if (!chatRoomService.isParticipantInRoom(roomName, participantName)) {
                            log.warn("Raw WS: Connection blocked - user '{}' is not a member of room '{}'", participantName, roomName);
                            response.setStatusCode(HttpStatus.FORBIDDEN);
                            response.getHeaders().add("X-Error-Message", "You are not a member of room '" + roomName + "'");
                            return false;
                        }

                        attributes.put("roomName", roomName);
                        attributes.put("participantName", participantName);
                        
                        log.info("Raw WebSocket connection authorized for user '{}' in room '{}'", participantName, roomName);
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, 
                                             ServerHttpResponse response, 
                                             WebSocketHandler wsHandler, 
                                             Exception exception) {
                        if (exception == null) {
                            log.info("Raw WebSocket handshake completed successfully");
                        } else {
                            log.error("Raw WebSocket handshake failed: {}", exception.getMessage());
                        }
                    }
                });
    }

    private String extractRoomNameFromQuery(String query) {
        if (query != null && query.contains("room=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("room=")) {
                    return param.split("=")[1];
                }
            }
        }
        return null;
    }

    private String extractParticipantFromQuery(String query) {
        if (query != null && query.contains("participant=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("participant=")) {
                    return param.split("=")[1];
                }
            }
        }
        return null;
    }
}
