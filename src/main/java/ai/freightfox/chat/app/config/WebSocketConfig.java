package ai.freightfox.chat.app.config;

import ai.freightfox.chat.app.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ChatRoomService chatRoomService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        registry.enableSimpleBroker("/freight-fox");
        registry.setApplicationDestinationPrefixes("/chat-rooms");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws/{roomName}")
                .setAllowedOrigins("*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, 
                                                 ServerHttpResponse response, 
                                                 WebSocketHandler wsHandler, 
                                                 Map<String, Object> attributes) throws Exception {

                        String path = request.getURI().getPath();
                        String roomName = extractRoomNameFromPath(path);

                        String query = request.getURI().getQuery();
                        String participantName = extractParticipantFromQuery(query);
                        
                        log.info("WebSocket connection attempt - Room: {}, Participant: {}", roomName, participantName);

                        if (roomName == null || participantName == null) {
                            log.warn("Connection blocked - missing room name or participant");
                            response.setStatusCode(HttpStatus.BAD_REQUEST);
                            response.getHeaders().add("X-Error-Message", "Room name and participant are required");
                            return false;
                        }

                        if (!chatRoomService.isRoomExists(roomName)) {
                            log.warn("Connection blocked - room '{}' does not exist", roomName);
                            response.setStatusCode(HttpStatus.NOT_FOUND);
                            response.getHeaders().add("X-Error-Message", "Room '" + roomName + "' does not exist");
                            return false;
                        }

                        if (!chatRoomService.isParticipantInRoom(roomName, participantName)) {
                            log.warn("Connection blocked - user '{}' is not a member of room '{}'", participantName, roomName);
                            response.setStatusCode(HttpStatus.FORBIDDEN);
                            response.getHeaders().add("X-Error-Message", "You are not a member of room '" + roomName + "'");
                            return false;
                        }

                        attributes.put("roomName", roomName);
                        attributes.put("participantName", participantName);
                        
                        log.info("WebSocket connection authorized for user '{}' in room '{}'", participantName, roomName);
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, 
                                             ServerHttpResponse response, 
                                             WebSocketHandler wsHandler, 
                                             Exception exception) {
                        if (exception == null) {
                            log.info("WebSocket handshake completed successfully");
                        } else {
                            log.error("WebSocket handshake failed: {}", exception.getMessage());
                        }
                    }
                    
                    private String extractRoomNameFromPath(String path) {
                        if (path != null && path.startsWith("/ws/")) {
                            String roomPart = path.substring(4);
                            if (roomPart.contains("/")) {
                                return roomPart.split("/")[0];
                            }
                            return roomPart;
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
                })
                .withSockJS();
    }
}
