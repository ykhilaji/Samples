package serialization;

import avro.Message;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class MessageSerializationTest {
    @Test
    public void serializationTest() throws IOException {
        Message message = new Message();

        message.setId(1L);
        message.setBody("body");
        message.setExtra("extra");
        message.setTimestamp(System.currentTimeMillis());

        ByteBuffer buffer = message.toByteBuffer();

        Message fromBytes = Message.fromByteBuffer(buffer);

        assertEquals(message, fromBytes);
    }
}
