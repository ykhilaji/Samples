import org.apache.activemq.artemis.core.server.Queue;
import org.apache.activemq.artemis.junit.EmbeddedActiveMQResource;
import org.junit.*;


import static org.junit.Assert.*;

public class ActiveMQTest {
    @Rule
    public EmbeddedActiveMQResource broker = new EmbeddedActiveMQResource();

    @Test
    public void testSendMessageToQueue() {
        Queue queue = broker.createQueue("test");

        broker.sendMessage(queue.getAddress(), "test message");

        String message = broker.receiveMessage(queue.getName()).getBodyBuffer().readString();

        assertEquals("test message", message);
    }
}
