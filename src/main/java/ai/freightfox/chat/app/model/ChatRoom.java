package ai.freightfox.chat.app.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    private String roomName;
    private LocalDateTime createdAt;
    private Set<String> participants;

    public ChatRoom(String roomName) {
        this.roomName = roomName;
        this.createdAt = LocalDateTime.now();
        this.participants = new HashSet<>();
    }
}
