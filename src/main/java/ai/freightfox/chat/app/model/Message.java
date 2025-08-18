package ai.freightfox.chat.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class Message {
    @JsonProperty("message")
    private String message;

    @JsonProperty("participant")
    private String participant;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    public Message(String participant, String message){
        this.participant = participant;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}
