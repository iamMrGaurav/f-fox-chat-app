package ai.freightfox.chat.app.repository;

import ai.freightfox.chat.app.globalExceptionHandler.RedisOperationException;
import ai.freightfox.chat.app.globalExceptionHandler.ResourceAlreadyExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Slf4j
@Repository
public class ChatRoomRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveChatRoom(String chatRoomName, LocalDateTime createdAt, String hashKey){
        try {
            redisTemplate.opsForHash().put(hashKey, "createdAt", createdAt);
            redisTemplate.opsForHash().put(hashKey, "roomName", chatRoomName);
        } catch (Exception e) {
            log.error("Redis operation failed for saveChatRoom: {}", e.getMessage());
            throw new RedisOperationException("Failed to create chat room", e);
        }
    }

    public Map<Object, Object> getChatRoomMetaData(String hashKey){
        try {
            return redisTemplate.opsForHash().entries(hashKey);
        } catch (Exception e) {
            log.error("Redis operation failed for getChatRoomMetaData: {}", e.getMessage());
            throw new RedisOperationException("Failed to retrieve room metadata", e);
        }
    }

    public boolean isRoomExist(String hashKey){
        try {
            return redisTemplate.hasKey(hashKey);
        } catch (Exception e) {
            log.error("Redis operation failed for isRoomExist: {}", e.getMessage());
            throw new RedisOperationException("Failed to check room existence", e);
        }
    }

    public void joinChatRoom(String roomName, String participantName, String  participantRoomHashKey, String roomHashKey){
        try {
            if(!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participantName))){
                redisTemplate.opsForSet().add(participantRoomHashKey, participantName);
            } else {
                throw new ResourceAlreadyExistException("User already in room");
            }
        } catch (ResourceAlreadyExistException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis operation failed for addParticipant: {}", e.getMessage());
            throw new RedisOperationException("Failed to add participant to room", e);
        }
    }

    public boolean removeParticipant(String participantName, String roomName, String participantRoomHashKey, String roomHashKey){
        try {
            if(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participantName))){
                redisTemplate.opsForSet().remove(participantRoomHashKey, participantName);
                log.info("Participant: {} removed from room: {}",participantName, roomName);
                return true;
            }
            log.info("Participant :{}  does not exist in room :{}", participantName, roomName);
            return false;
        } catch (Exception e) {
            log.error("Redis operation failed for removeParticipant: {}", e.getMessage());
            throw new RedisOperationException("Failed to remove participant from room", e);
        }
    }

    public Set<Object> getParticipants(String roomName, String participantRoomHashKey) {
        try {
            return redisTemplate.opsForSet().members(participantRoomHashKey);
        } catch (Exception e) {
            log.error("Redis operation failed for getParticipants: {}", e.getMessage());
            throw new RedisOperationException("Failed to retrieve participants", e);
        }
    }

    public boolean isParticipantInRoom(String participantRoomHashKey, String participant) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantRoomHashKey, participant));
        } catch (Exception e) {
            log.error("Redis operation failed for isParticipantInRoom: {}", e.getMessage());
            throw new RedisOperationException("Failed to check participant membership", e);
        }
    }

    public void removeRemoveRoomData(String roomKey, String participantRoomKey, String messageRoomKey){
        try{
            redisTemplate.delete(Arrays.asList(roomKey, participantRoomKey, messageRoomKey));
        }catch (Exception e){
            log.error("Redis operation failed for deleteRoom: {}", e.getMessage());
            throw new RedisOperationException("Failed to Delete Chat Room Data", e);
        }
    }

    public long getParticipantCount(String participantRoomHashKey) {
        try {
            return redisTemplate.opsForSet().size(participantRoomHashKey);
        } catch (Exception e) {
            log.error("Redis operation failed for getParticipantCount: {}", e.getMessage());
            throw new RedisOperationException("Failed to get participant count", e);
        }
    }

}
