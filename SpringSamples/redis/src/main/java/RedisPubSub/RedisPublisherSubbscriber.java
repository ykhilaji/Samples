package RedisPubSub;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisPublisherSubbscriber {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(RedisConfiguration.class);
        RedisTemplate<byte[], byte[]> template = context.getBean(RedisTemplate.class);

        template.convertAndSend("sample", "first sample message");
    }
}
