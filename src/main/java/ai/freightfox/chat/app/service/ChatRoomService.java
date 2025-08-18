package ai.freightfox.chat.app.service;

import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.BadRequestException;
import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.ChatRoomNotFoundException;
import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.ResourceAlreadyExistException;
import ai.freightfox.chat.app.model.ChatRoomModel;
import ai.freightfox.chat.app.repository.ChatRoomRepository;
import ai.freightfox.chat.app.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;


    public void createChatRoom(String roomName) {
        if (isRoomExists(roomName)) {
            throw new ResourceAlreadyExistException("Chat room '" + roomName + "' already exists");
        }

        ChatRoomModel chatRoomModel = new ChatRoomModel(roomName);

        saveChatRoom(chatRoomModel);
        log.info("Chat room: {} created successfully",  roomName);
    }

    public void saveChatRoom(ChatRoomModel chatRoomModel) {
        String hashKey = RedisKeyUtil.getRoomHashKey(chatRoomModel.getRoomName());
        chatRoomRepository.saveChatRoom(chatRoomModel.getRoomName(), chatRoomModel.getCreatedAt(), hashKey);
    }

    public boolean isRoomExists(String roomName) {
        String hashKey = RedisKeyUtil.getRoomHashKey(roomName);
        return chatRoomRepository.isRoomExist(hashKey);
    }

    public void joinChatRoom(String roomName, String participantName) {
        if (!isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }

        if (participantName == null || participantName.trim().isEmpty()) {
            throw new BadRequestException("Participant name cannot be empty");
        }

        String participantRoomHashKey = RedisKeyUtil.getParticipantRoomHashKey(roomName);
        String roomHashKey = RedisKeyUtil.getRoomHashKey(roomName);
        chatRoomRepository.joinChatRoom(roomName, participantName.trim(), participantRoomHashKey, roomHashKey);
    }

    public boolean removeParticipant(String roomName, String participantName) {
        if (!isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }

        if (participantName == null || participantName.trim().isEmpty()) {
            throw new BadRequestException("Participant name cannot be empty");
        }

        String participantRoomHashKey = RedisKeyUtil.getParticipantRoomHashKey(roomName);
        String roomHashKey = RedisKeyUtil.getRoomHashKey(roomName);
        return chatRoomRepository.removeParticipant(participantName.trim(), roomName, participantRoomHashKey, roomHashKey);
    }

    public Set<Object> getParticipants(String roomName) {
        if (!isRoomExists(roomName)) {
            throw new ChatRoomNotFoundException("Chat room '" + roomName + "' does not exist");
        }
        
        String participantRoomHashKey = RedisKeyUtil.getParticipantRoomHashKey(roomName);
        return chatRoomRepository.getParticipants(roomName, participantRoomHashKey);
    }

    public boolean isParticipantInRoom(String roomName, String participantName) {
        if (!isRoomExists(roomName)) {
            return false;
        }

        if (participantName == null || participantName.trim().isEmpty()) {
            return false;
        }

        String participantRoomHashKey = RedisKeyUtil.getParticipantRoomHashKey(roomName);
        return chatRoomRepository.isParticipantInRoom(participantRoomHashKey, participantName.trim());
    }

    public long getParticipantCount(String roomName) {
        String participantRoomHashKey = RedisKeyUtil.getParticipantRoomHashKey(roomName);
        return chatRoomRepository.getParticipantCount(participantRoomHashKey);
    }

    public boolean isValidRoomName(String roomName) {
        return roomName != null &&
                roomName.trim().length() >= 3 &&
                roomName.trim().length() <= 50 &&
                roomName.matches("^[a-zA-Z0-9_-]+$");
    }

    public void createValidatedChatRoom(String roomName) {
        if (!isValidRoomName(roomName)) {
            throw new BadRequestException("Invalid room name, Must be 3-50 characters, alphanumeric, underscore, or hyphen only.");
        }
        createChatRoom(roomName.trim());
    }

    public void removeRoom(String roomName){
        String roomKey = RedisKeyUtil.getRoomHashKey(roomName);
        String participantKey = RedisKeyUtil.getParticipantRoomHashKey(roomName);
        String messageRoomKey = RedisKeyUtil.getMessageRoomKey(roomName);

        Map<String, Boolean> keyExistence = Map.of(
            "room", chatRoomRepository.isRoomExist(roomKey),
            "participants", chatRoomRepository.isRoomExist(participantKey),
            "messages", chatRoomRepository.isRoomExist(messageRoomKey)
        );

        if (!keyExistence.get("room")) {
            throw new ChatRoomNotFoundException("Room '" + roomName + "' does not exist");
        }

        boolean allKeysExist = keyExistence.values().stream().allMatch(Boolean::booleanValue);

        if (!allKeysExist) {
            log.warn("Some keys missing for room {} - proceeding with deletion anyway: {}", roomName, keyExistence);
        }

        log.info("Deleting room {} - Key existence: {}", roomName, keyExistence);
        
        chatRoomRepository.removeRemoveRoomData(roomKey, participantKey, messageRoomKey);
        
        log.info("Successfully deleted room {}", roomName);
    }

}
