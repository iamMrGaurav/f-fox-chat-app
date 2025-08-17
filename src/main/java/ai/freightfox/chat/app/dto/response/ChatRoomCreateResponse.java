package ai.freightfox.chat.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateResponse {
    private String message;
    private String roomId;
    private String status;
    
    public ChatRoomCreateResponse(String roomName) {
        this.message = "Chat room '" + roomName + "' created successfully.";
        this.roomId = roomName;
        this.status = "success";
    }
}