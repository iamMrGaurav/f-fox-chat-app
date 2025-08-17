package ai.freightfox.chat.app.repository;


import ai.freightfox.chat.app.model.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.Set;

@Repository
public class ChatRoomRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private final String BASE_KEY = "chatroom:";

    public void saveChatRoom(ChatRoom chatRoom){
        String hashKey = getRoomHashKey(chatRoom.getRoomName());
        redisTemplate.opsForHash().put(hashKey, "createdAt", chatRoom.getCreatedAt());
        redisTemplate.opsForHash().put(hashKey, "roomName", chatRoom.getRoomName());
        redisTemplate.opsForHash().put(hashKey, "participantCount", String.valueOf(chatRoom.getParticipantCount()));
    }

    public Map<Object, Object> getChatRoomMetaData(String roomName){
        String hashKey = getRoomHashKey(roomName);
        return redisTemplate.opsForHash().entries(hashKey);
    }

    public boolean isRoomExist(String roomName){
        String hashKey = getRoomHashKey(roomName);
        return redisTemplate.hasKey(hashKey);
    }

    public String getRoomHashKey(String roomName){
        return BASE_KEY + roomName;
    }

    public String getParticipantRoomHashKey(String roomName){
        return BASE_KEY + roomName + ":participants";
    }

    public boolean addParticipant(String roomName, String participantName){
        String participantRoomHashKey = getParticipantRoomHashKey(roomName);

        if(!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participantName))){
            redisTemplate.opsForSet().add(participantRoomHashKey, participantName);
            String roomHashKey = getRoomHashKey(roomName);
            redisTemplate.opsForHash().increment(roomHashKey, "participantCount", 1);
            return true;
        }
        return false;
    }

    public boolean removeParticipant(String participantName, String roomName){
        String participantRoomHashKey = getParticipantRoomHashKey(roomName);
        if(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participantName))){
            String roomHashKey = getRoomHashKey(roomName);
            redisTemplate.opsForHash().increment(roomHashKey, "participantCount", -1);
            redisTemplate.opsForSet().remove(participantRoomHashKey, participantName);
            return true;
        }
        return false;
    }

    public Set<Object> getParticipants(String roomName) {
        String participantRoomHashKey = getParticipantRoomHashKey(roomName);
        return redisTemplate.opsForSet().members(participantRoomHashKey);
    }

    public boolean isParticipantInRoom(String roomName, String participant) {
        String participantRoomHashKey = getParticipantRoomHashKey(roomName);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participant));
    }

}
