package RedisPubSub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class RedisListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] bytes) {
        System.out.println(String.format("Message: %s", new String(message.getBody())));
        System.out.println(String.format("Topic: %s", new String(bytes)));
    }
}
