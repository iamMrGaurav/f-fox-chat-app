package ai.freightfox.chat.app.service;


import ai.freightfox.chat.app.globalExceptionHandler.RedisOperationException;
import ai.freightfox.chat.app.model.Message;
import ai.freightfox.chat.app.util.RedisKeyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessagePublisher {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void publishMessage(String roomName, Message message){
        try{
            String channel = RedisKeyUtil.getChannelKey(roomName);
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, jsonMessage);
            log.info("Message publish to channel {} successfully", channel);
        }catch (Exception e){
                throw  new RedisOperationException("Failed to send to redis channel", e);
        }
    }
}
