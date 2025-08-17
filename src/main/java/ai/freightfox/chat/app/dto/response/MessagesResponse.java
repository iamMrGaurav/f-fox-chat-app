
package ai.freightfox.chat.app.dto.response;

import ai.freightfox.chat.app.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagesResponse {
    private List<Message> messages;
}