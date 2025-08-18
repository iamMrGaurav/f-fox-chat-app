package ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers;

public class ParticipantNotFoundException extends RuntimeException {
    public ParticipantNotFoundException(String message) {
        super(message);
    }
}
