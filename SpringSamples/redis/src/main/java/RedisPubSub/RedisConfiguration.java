package RedisPubSub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RedisConfiguration {
    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        return new RedisStandaloneConfiguration("localhost");
    }

    @Bean
    @Autowired
    public JedisConnectionFactory jedisConnectionFactory(RedisStandaloneConfiguration redisStandaloneConfiguration) {
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public MessageListener messageListener() {
        return new RedisListener();
    }

    @Bean
    @Autowired
    public MessageListenerAdapter messageListenerAdapter(MessageListener listener) {
        return new MessageListenerAdapter(listener);
    }

    @Bean
    public Topic topic() {
        return new ChannelTopic("sample");
    }

    @Bean
    @Autowired
    public RedisTemplate<byte[], byte[]> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }

    @Bean
    @Autowired
    public RedisMessageListenerContainer redisMessageListenerContainer(
            JedisConnectionFactory connectionFactory,
            MessageListenerAdapter adapter,
            Topic topic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(adapter, topic);

        return container;
    }
}
