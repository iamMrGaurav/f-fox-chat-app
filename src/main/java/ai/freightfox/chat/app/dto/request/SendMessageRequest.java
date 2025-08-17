package ai.freightfox.chat.app.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {
    private String participant;
    private String message;
    private String roomName;

    public SendMessageRequest(String participant, String message){
        this.message = message;
        this.participant = participant;
    }
}
