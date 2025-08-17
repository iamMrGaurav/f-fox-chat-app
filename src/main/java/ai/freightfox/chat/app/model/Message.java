package ai.freightfox.chat.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @JsonProperty("message")
    private String message;

    @JsonProperty("participant")
    private String participant;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    public Message(String participant, String message){
        this.participant = participant;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}
