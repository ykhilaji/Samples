package stream.filter.kafka.streams;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ProcessingExceptionHandler implements ProductionExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProcessingExceptionHandler.class);


    @Override
    public ProductionExceptionHandlerResponse handle(ProducerRecord<byte[], byte[]> record, Exception exception) {
        if (exception instanceof RecordTooLargeException) {
            logger.error("Record too large exception", exception);
            return ProductionExceptionHandlerResponse.FAIL;
        } else {
            return ProductionExceptionHandlerResponse.CONTINUE;
        }
    }

    @Override
    public void configure(Map<String, ?> configs) {}
}
