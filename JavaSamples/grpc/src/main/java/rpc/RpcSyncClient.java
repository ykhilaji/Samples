package rpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import grpc.Protocol;
import grpc.ServiceGrpc;

public class RpcSyncClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 12345)
                .usePlaintext() // disable TLS
                .build();

        ServiceGrpc.ServiceBlockingStub blockingStub = ServiceGrpc.newBlockingStub(channel);

        blockingStub.ping(Protocol.Empty.getDefaultInstance());
        Protocol.Response response = blockingStub.task(Protocol.Request.newBuilder().setId(1L).setBody("Request").build());
        System.out.println(String.format("Got response: %s", response.toString()));

        channel.shutdown();
    }
}
