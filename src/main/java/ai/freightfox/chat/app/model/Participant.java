package ai.freightfox.chat.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {
    private String username;
    private LocalDateTime joinedAt;
    
    public Participant(String username) {
        this.username = username;
        this.joinedAt = LocalDateTime.now();
    }
}