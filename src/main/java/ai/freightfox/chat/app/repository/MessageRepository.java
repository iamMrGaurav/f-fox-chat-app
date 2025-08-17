package ai.freightfox.chat.app.repository;

import ai.freightfox.chat.app.globalExceptionHandler.RedisOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class MessageRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveChat(String chatRoomKey, String messageJson) {
        try {
            redisTemplate.opsForList().rightPush(chatRoomKey, messageJson);
        } catch (Exception e) {
            log.error("Redis operation failed for saveChat: {}", e.getMessage());
            throw new RedisOperationException("Failed to save message", e);
        }
    }

    public List<Object> getLastNMessages(String chatRoomKey, int limit) {
        try {
            return redisTemplate.opsForList().range(chatRoomKey, -limit, -1);
        } catch (Exception e) {
            log.error("Redis operation failed for getLastNMessages: {}", e.getMessage());
            throw new RedisOperationException("Failed to retrieve last N messages", e);
        }
    }

    public List<Object> getAllMessages(String chatRoomKey) {
        try {
            return redisTemplate.opsForList().range(chatRoomKey, 0, -1);
        } catch (Exception e) {
            log.error("Redis operation failed for getAllMessages: {}", e.getMessage());
            throw new RedisOperationException("Failed to retrieve all messages", e);
        }
    }

    public List<Object> getMessagesWithPagination(String chatRoomKey, int limit, int offset) {
        try {
            return redisTemplate.opsForList().range(chatRoomKey, offset, offset + limit - 1);
        } catch (Exception e) {
            log.error("Redis operation failed for getMessagesWithPagination: {}", e.getMessage());
            throw new RedisOperationException("Failed to retrieve paginated messages", e);
        }
    }

    public long getMessageCount(String chatRoomKey) {
        try {
            return redisTemplate.opsForList().size(chatRoomKey);
        } catch (Exception e) {
            log.error("Redis operation failed for getMessageCount: {}", e.getMessage());
            throw new RedisOperationException("Failed to get message count", e);
        }
    }
}
