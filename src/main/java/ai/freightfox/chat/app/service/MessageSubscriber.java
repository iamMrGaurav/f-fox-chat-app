package ai.freightfox.chat.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageSubscriber implements MessageListener {

    @Autowired
    private final ObjectMapper objectMapper;
    
    public MessageSubscriber() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            ai.freightfox.chat.app.model.Message deserializedMessage = objectMapper.readValue(body, ai.freightfox.chat.app.model.Message.class);

        } catch (Exception e) {
            log.error("Error processing received message: {}", e.getMessage());
        }
    }
}
