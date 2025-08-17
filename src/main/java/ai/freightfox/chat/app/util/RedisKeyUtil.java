package ai.freightfox.chat.app.util;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyUtil {
    
    private static final String BASE_KEY = "chatroom:";
    
    public static String getRoomHashKey(String roomName) {
        return BASE_KEY + roomName;
    }
    
    public static String getParticipantRoomHashKey(String roomName) {
        return BASE_KEY + roomName + ":participants";
    }
    
    public static String getMessageRoomKey(String roomName) {
        return BASE_KEY + roomName + ":messages";
    }
}