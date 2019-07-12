package project;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;
import protobuf.Protocol;

import static org.junit.jupiter.api.Assertions.*;

public class ProtobufSerializationTest {
    @Test
    public void test() throws InvalidProtocolBufferException {
        Protocol.Request request = Protocol.Request
                .newBuilder()
                .setId(1L)
                .setBody("body")
                .setExtra("extra")
                .build();

        Protocol.Response response = Protocol.Response
                .newBuilder()
                .setId(1L)
                .setBody("body")
                .setExtra("extra")
                .build();

        byte[] requestBytes = request.toByteArray();
        byte[] responseBytes = response.toByteArray();

        assertEquals(request, Protocol.Request.parseFrom(requestBytes));
        assertEquals(response, Protocol.Response.parseFrom(responseBytes));
    }
}
