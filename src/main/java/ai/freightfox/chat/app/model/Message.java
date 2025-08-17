package ai.freightfox.chat.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Message {
    private String message;
    private String participantName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    public Message(String participantName, String message){
        this.participantName = participantName;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
