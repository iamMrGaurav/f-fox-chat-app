package ai.freightfox.chat.app.service;

import ai.freightfox.chat.app.model.MessageModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageSubscriberService implements MessageListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            // Extract room name from channel where the format is chatroom:roomName:channel
            String roomName = extractRoomNameFromChannel(channel);
            
            MessageModel deserializedMessageModel = objectMapper.readValue(body, MessageModel.class);

            if (roomName != null) {
                log.info("Broadcasting message from {} to room {}", deserializedMessageModel.getParticipant(), roomName);
                messagingTemplate.convertAndSend("/freight-fox/" + roomName, deserializedMessageModel);
            }

        } catch (Exception e) {
            log.error("Error processing received message: {}", e.getMessage());
        }
    }
    
    private String extractRoomNameFromChannel(String channel) {
        if (channel != null && channel.startsWith("chatroom:") && channel.endsWith(":channel")) {
            return channel.substring(9, channel.length() - 8);
        }
        return null;
    }
}
