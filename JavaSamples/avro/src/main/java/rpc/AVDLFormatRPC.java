package rpc;

import org.apache.avro.ipc.netty.NettyServer;
import org.apache.avro.ipc.netty.NettyTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder;

import avro.sample.avdl.IDLFormatTest;
import avro.sample.avdl.Request;
import avro.sample.avdl.Response;
import avro.sample.avdl.SomeError;

import java.io.IOException;
import java.net.InetSocketAddress;

public class AVDLFormatRPC {
    public static class ProtocolImpl implements IDLFormatTest {
        @Override
        public Response task(Request request) throws SomeError {
            if (request.getExtra() != null) {
                throw SomeError.newBuilder().setExplanation(request.getExtra()).build();
            }

            Response response = new Response();
            response.setId(request.getId());
            response.setBody(String.format("Response for: %s", request.getBody()));

            return response;
        }

        @Override
        public void ping() {
            System.out.println("Got ping request");
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = createServer();
        NettyTransceiver transceiver = createTransceiver();
        IDLFormatTest client = createClient(transceiver);

        client.ping();
        try {
            client.task(new Request(1L, "Some body", null));
            client.task(new Request(2L, "Some body", "Extra"));
        } catch (SomeError someError) {
            System.out.println(String.format("Error: %s", someError.getExplanation()));
        }

        transceiver.close();
        server.close();
    }

    public static NettyServer createServer() {
        return new NettyServer(new SpecificResponder(IDLFormatTest.PROTOCOL, new ProtocolImpl()), new InetSocketAddress(12345));
    }

    public static NettyTransceiver createTransceiver() throws IOException {
        return new NettyTransceiver(new InetSocketAddress(12345));
    }

    public static IDLFormatTest createClient(NettyTransceiver transceiver) throws IOException {
        IDLFormatTest proxy = SpecificRequestor.getClient(IDLFormatTest.class, transceiver);

        return proxy;
    }
}
