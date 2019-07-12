package rpc;

import com.google.common.util.concurrent.ListenableFuture;
import grpc.Protocol;
import grpc.ServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RpcAsyncClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 12345)
                .usePlaintext() // disable TLS
                .build();

        ServiceGrpc.ServiceFutureStub futureStub = ServiceGrpc.newFutureStub(channel);
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<Protocol.Empty> emptyListenableFuture = futureStub.ping(Protocol.Empty.getDefaultInstance());
        ListenableFuture<Protocol.Response> responseListenableFuture = futureStub.task(Protocol.Request.newBuilder().setId(1L).setBody("Request").build());

        emptyListenableFuture.addListener(() -> {
            System.out.println("Got ping response");
        }, executor);
        responseListenableFuture.addListener(() -> {
            try {
                System.out.println(String.format("Got response: %s", responseListenableFuture.get().toString()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, executor);

        while (!emptyListenableFuture.isDone() && !responseListenableFuture.isDone()) {}

        channel.shutdown();
    }
}
