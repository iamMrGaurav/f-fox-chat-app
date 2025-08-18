package ai.freightfox.chat.app.service;

import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.BadRequestException;
import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.ChatRoomNotFoundException;
import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.ParticipantNotFoundException;
import ai.freightfox.chat.app.model.MessageModel;
import ai.freightfox.chat.app.repository.MessageRepository;
import ai.freightfox.chat.app.util.RedisKeyUtil;
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
    private ChatRoomService chatRoomService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessagePublisherService messagePublisher;


    public void saveMessage(String roomName, MessageModel messageModel) {

        validateParam(roomName, messageModel);

        try {
            if (messageModel.getTimestamp() == null) {
                messageModel.setTimestamp(LocalDateTime.now());
            }
            
            String chatRoomKey = RedisKeyUtil.getMessageRoomKey(roomName);
            String messageJson = objectMapper.writeValueAsString(messageModel);
            messageRepository.saveChat(chatRoomKey, messageJson);

            messagePublisher.publishMessage(roomName, messageModel);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to save message: " + e.getMessage(), e);
        }
    }

    private void validateParam(String roomName, MessageModel messageModel){
        if (!chatRoomService.isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }

        if (messageModel == null) {
            throw new BadRequestException("Message cannot be null");
        }

        if (messageModel.getParticipant() == null || messageModel.getParticipant().trim().isEmpty()) {
            throw new BadRequestException("Participant name cannot be empty");
        }

        if (messageModel.getMessage() == null || messageModel.getMessage().trim().isEmpty()) {
            throw new BadRequestException("Message content cannot be empty");
        }

        if(!chatRoomService.isParticipantInRoom(roomName, messageModel.getParticipant()))
            throw new ParticipantNotFoundException("Participant '" + messageModel.getParticipant() + "' is not a member of room '" + roomName + "'");
    }

    public void saveMessage(String roomName, String participant, String messageText) {
        MessageModel messageModel = new MessageModel(participant, messageText);
        saveMessage(roomName, messageModel);
    }

    public List<MessageModel> getLastNMessages(String roomName, int limit) {
        if (!chatRoomService.isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }

        if (limit <= 0) {
            throw new BadRequestException("Limit must be greater than 0");
        }

        String chatRoomKey = RedisKeyUtil.getMessageRoomKey(roomName);
        List<Object> messageJsonList = messageRepository.getLastNMessages(chatRoomKey, limit);
        return convertJsonListToMessages(messageJsonList);
    }

    public List<MessageModel> getAllMessages(String roomName) {
        if (!chatRoomService.isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }

        String chatRoomKey = RedisKeyUtil.getMessageRoomKey(roomName);
        List<Object> messageJsonList = messageRepository.getAllMessages(chatRoomKey);
        return convertJsonListToMessages(messageJsonList);
    }

    public List<MessageModel> getMessages(String roomName, Integer limit) {
        if (!chatRoomService.isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }

        if (limit != null && limit > 0) {
            return getLastNMessages(roomName, limit);
        } else {
            return getAllMessages(roomName);
        }
    }

    public List<MessageModel> getMessages(String roomName, Integer limit, Integer offset) {
        if (!chatRoomService.isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }

        if (limit != null && limit > 0) {
            return getMessagesWithPagination(roomName, limit, offset);
        } else {
            return getAllMessages(roomName);
        }
    }

    public List<MessageModel> getMessagesWithPagination(String roomName, int limit, int offset) {
        String chatRoomKey = RedisKeyUtil.getMessageRoomKey(roomName);
        List<Object> messageJsonList = messageRepository.getMessagesWithPagination(chatRoomKey, limit, offset);
        return convertJsonListToMessages(messageJsonList);
    }

    public long getTotalMessageCount(String roomName) {
        if (!chatRoomService.isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }
        String chatRoomKey = RedisKeyUtil.getMessageRoomKey(roomName);
        return messageRepository.getMessageCount(chatRoomKey);
    }


    private List<MessageModel> convertJsonListToMessages(List<Object> messageJsonList) {
        List<MessageModel> messageModels = new ArrayList<>();
        if (messageJsonList != null) {
            for (Object messageJson : messageJsonList) {
                try {
                    MessageModel messageModel = objectMapper.readValue((String) messageJson, MessageModel.class);
                    messageModels.add(messageModel);
                } catch (Exception e) {
                    System.err.println("Warning: Skipping malformed message: " + messageJson + ", Error: " + e.getMessage());
                }
            }
        }
        return messageModels;
    }

}
