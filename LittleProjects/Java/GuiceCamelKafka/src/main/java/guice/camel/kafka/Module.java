package guice.camel.kafka;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.apache.camel.CamelContext;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaConstants;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Bean.class);
    }

    @Provides
    @Inject
    @Named("from")
    KafkaComponent kafkaFrom(CamelContext context) {
        KafkaComponent kafkaComponent = new KafkaComponent(context);
        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration();

        kafkaConfiguration.setBrokers("localhost:29092");
        kafkaConfiguration.setClientId("camelConsumer");
        kafkaConfiguration.setValueDeserializer(KafkaConstants.KAFKA_DEFAULT_DESERIALIZER);
        kafkaConfiguration.setKeyDeserializer(KafkaConstants.KAFKA_DEFAULT_DESERIALIZER);
        kafkaConfiguration.setConsumersCount(1);
        kafkaConfiguration.setConsumerStreams(1);

        kafkaComponent.setConfiguration(kafkaConfiguration);

        return kafkaComponent;
    }

    @Provides
    @Inject
    @Named("to")
    KafkaComponent kafkaTo(CamelContext context) {
        KafkaComponent kafkaComponent = new KafkaComponent(context);
        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration();

        kafkaConfiguration.setBrokers("localhost:29092");
        kafkaConfiguration.setClientId("camelProducer");
        kafkaConfiguration.setKeySerializerClass(KafkaConstants.KAFKA_DEFAULT_SERIALIZER);
        kafkaConfiguration.setSerializerClass(KafkaConstants.KAFKA_DEFAULT_SERIALIZER);
        kafkaConfiguration.setRequestRequiredAcks("all");

        kafkaComponent.setConfiguration(kafkaConfiguration);

        return kafkaComponent;
    }
}
