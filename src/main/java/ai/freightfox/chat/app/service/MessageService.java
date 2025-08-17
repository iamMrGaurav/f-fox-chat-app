package ai.freightfox.chat.app.service;

import ai.freightfox.chat.app.model.Message;
import ai.freightfox.chat.app.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_KEY = "chatroom:";

    public void saveMessage(String roomName, Message message) {
        try {
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(LocalDateTime.now());
            }
            
            String chatRoomKey = getChatRoomKey(roomName);
            String messageJson = objectMapper.writeValueAsString(message);
            messageRepository.saveChat(chatRoomKey, messageJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to save message: " + e.getMessage(), e);
        }
    }

    public void saveMessage(String roomName, String participant, String messageText) {
        Message message = new Message(participant, messageText, LocalDateTime.now());
        saveMessage(roomName, message);
    }

    public List<Message> getLastNMessages(String roomName, int limit) {
        String chatRoomKey = getChatRoomKey(roomName);
        List<Object> messageJsonList = messageRepository.getLastNMessages(chatRoomKey, limit);
        return convertJsonListToMessages(messageJsonList);
    }

    public List<Message> getAllMessages(String roomName) {
        String chatRoomKey = getChatRoomKey(roomName);
        List<Object> messageJsonList = messageRepository.getAllMessages(chatRoomKey);
        return convertJsonListToMessages(messageJsonList);
    }

    public List<Message> getMessages(String roomName, Integer limit) {
        if (limit != null && limit > 0) {
            return getLastNMessages(roomName, limit);
        } else {
            return getAllMessages(roomName);
        }
    }


    private List<Message> convertJsonListToMessages(List<Object> messageJsonList) {
        List<Message> messages = new ArrayList<>();
        if (messageJsonList != null) {
            for (Object messageJson : messageJsonList) {
                try {
                    Message message = objectMapper.readValue((String) messageJson, Message.class);
                    messages.add(message);
                } catch (Exception e) {
                    System.err.println("Warning: Skipping malformed message: " + messageJson + ", Error: " + e.getMessage());
                }
            }
        }
        return messages;
    }

    private String getChatRoomKey(String roomName) {
        return BASE_KEY + roomName + ":messages";
    }
}
