package ai.freightfox.chat.app.service;


import ai.freightfox.chat.app.globalExceptionHandler.exceptionHandlers.RedisOperationException;
import ai.freightfox.chat.app.model.MessageModel;
import ai.freightfox.chat.app.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessagePublisherService {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    public void publishMessage(String roomName, MessageModel messageModel){
        try{
            String channel = RedisKeyUtil.getChannelKey(roomName);
            redisTemplate.convertAndSend(channel, messageModel);
            log.info("Message publish to channel {} successfully", channel);
        }catch (Exception e){
                throw  new RedisOperationException("Failed to send to redis channel", e);
        }
    }
}
