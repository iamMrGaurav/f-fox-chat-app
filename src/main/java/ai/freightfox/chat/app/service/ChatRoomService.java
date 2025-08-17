package ai.freightfox.chat.app.service;

import ai.freightfox.chat.app.model.ChatRoom;
import ai.freightfox.chat.app.repository.ChatRoomRepository;
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

    private final String BASE_KEY = "chatroom:";

    public ChatRoom createChatRoom(String roomName) {
        if (isRoomExists(roomName)) {
            throw new RuntimeException("Chat room '" + roomName + "' already exists");
        }

        ChatRoom chatRoom = new ChatRoom(roomName);

        saveChatRoom(chatRoom);
        log.info("Chat room: {} created successfully",  roomName);
        return chatRoom;
    }

    public void saveChatRoom(ChatRoom chatRoom) {
        String hashKey = getRoomHashKey(chatRoom.getRoomName());
        chatRoomRepository.saveChatRoom(chatRoom.getRoomName(), chatRoom.getCreatedAt(), chatRoom.getParticipantCount(), hashKey);
    }

    public boolean isRoomExists(String roomName) {
        String hashKey = getRoomHashKey(roomName);
        return chatRoomRepository.isRoomExist(hashKey);
    }

    public Map<Object, Object> getChatRoomMetadata(String roomName) {
        String hashKey = getRoomHashKey(roomName);
        return chatRoomRepository.getChatRoomMetaData(hashKey);
    }

    public boolean addParticipant(String roomName, String participantName) {
        if (!isRoomExists(roomName)) {
            throw new RuntimeException("Chat room '" + roomName + "' does not exist");
        }

        if (participantName == null || participantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Participant name cannot be empty");
        }

        String participantRoomHashKey = getParticipantRoomHashKey(roomName);
        String roomHashKey = getRoomHashKey(roomName);
        return chatRoomRepository.addParticipant(roomName, participantName.trim(), participantRoomHashKey, roomHashKey);
    }

    public boolean removeParticipant(String roomName, String participantName) {
        if (!isRoomExists(roomName)) {
            throw new RuntimeException("Chat room '" + roomName + "' does not exist");
        }

        if (participantName == null || participantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Participant name cannot be empty");
        }

        String participantRoomHashKey = getParticipantRoomHashKey(roomName);
        String roomHashKey = getRoomHashKey(roomName);
        return chatRoomRepository.removeParticipant(participantName.trim(), roomName, participantRoomHashKey, roomHashKey);
    }

    public Set<Object> getParticipants(String roomName) {
        if (!isRoomExists(roomName)) {
            throw new RuntimeException("Chat room '" + roomName + "' does not exist");
        }
        
        String participantRoomHashKey = getParticipantRoomHashKey(roomName);
        return chatRoomRepository.getParticipants(roomName, participantRoomHashKey);
    }

    public boolean isParticipantInRoom(String roomName, String participantName) {
        if (!isRoomExists(roomName)) {
            return false;
        }

        if (participantName == null || participantName.trim().isEmpty()) {
            return false;
        }

        String participantRoomHashKey = getParticipantRoomHashKey(roomName);
        return chatRoomRepository.isParticipantInRoom(participantRoomHashKey, participantName.trim());
    }

    public boolean isValidRoomName(String roomName) {
        return roomName != null &&
                roomName.trim().length() >= 3 &&
                roomName.trim().length() <= 50 &&
                roomName.matches("^[a-zA-Z0-9_-]+$");
    }

    public ChatRoom createValidatedChatRoom(String roomName) {
        if (!isValidRoomName(roomName)) {
            throw new IllegalArgumentException("Invalid room name, Must be 3-50 characters, alphanumeric, underscore, or hyphen only.");
        }
        return createChatRoom(roomName.trim());
    }

    public String getRoomHashKey(String roomName){
        return BASE_KEY + roomName;
    }

    public String getParticipantRoomHashKey(String roomName){
        return BASE_KEY + roomName + ":participants";
    }
}
