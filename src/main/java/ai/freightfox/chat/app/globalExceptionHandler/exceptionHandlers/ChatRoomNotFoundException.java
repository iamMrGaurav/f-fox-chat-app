package ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers;

public class ChatRoomNotFoundException extends RuntimeException {
    public ChatRoomNotFoundException(String message) {
        super(message);
    }
    
    public ChatRoomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
