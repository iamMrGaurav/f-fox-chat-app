package ai.freightfox.chat.app.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveChat(String chatRoomKey, String messageJson) {
        redisTemplate.opsForList().rightPush(chatRoomKey, messageJson);
    }

    public List<Object> getLastNMessages(String chatRoomKey, int limit) {
        return redisTemplate.opsForList().range(chatRoomKey, -limit, -1);
    }

    public List<Object> getAllMessages(String chatRoomKey) {
        return redisTemplate.opsForList().range(chatRoomKey, 0, -1);
    }
}
