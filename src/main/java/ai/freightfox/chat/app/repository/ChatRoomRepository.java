package ai.freightfox.chat.app.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Repository
public class ChatRoomRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveChatRoom(String chatRoomName, LocalDateTime createdAt, int participantCount, String hashKey){
        redisTemplate.opsForHash().put(hashKey, "createdAt", createdAt);
        redisTemplate.opsForHash().put(hashKey, "roomName", chatRoomName);
        redisTemplate.opsForHash().put(hashKey, "participantCount", String.valueOf(participantCount));
    }

    public Map<Object, Object> getChatRoomMetaData(String hashKey){
        return redisTemplate.opsForHash().entries(hashKey);
    }

    public boolean isRoomExist(String hashKey){
        return redisTemplate.hasKey(hashKey);
    }

    public boolean addParticipant(String roomName, String participantName, String  participantRoomHashKey, String roomHashKey){
        if(!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participantName))){
            redisTemplate.opsForSet().add(participantRoomHashKey, participantName);
            redisTemplate.opsForHash().increment(roomHashKey, "participantCount", 1);
            return true;
        }
        return false;
    }

    public boolean removeParticipant(String participantName, String roomName, String participantRoomHashKey, String roomHashKey){
        if(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participantName))){
            redisTemplate.opsForHash().increment(roomHashKey, "participantCount", -1);
            redisTemplate.opsForSet().remove(participantRoomHashKey, participantName);
            log.info("Participant: {} removed from room: {}",participantName, roomName);
            return true;
        }
        log.info("Participant :{}  does not exist in room :{}", participantName, roomName);
        return false;
    }

    public Set<Object> getParticipants(String roomName, String participantRoomHashKey) {
        return redisTemplate.opsForSet().members(participantRoomHashKey);
    }

    public boolean isParticipantInRoom(String participantRoomHashKey, String participant) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participant));
    }

}
