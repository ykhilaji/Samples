package rpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;

import grpc.Protocol;
import grpc.ServiceGrpc;

public class RpcServer {
    static class ServiceImpl extends ServiceGrpc.ServiceImplBase {
        @Override
        public void task(Protocol.Request request, StreamObserver<Protocol.Response> responseObserver) {
            responseObserver.onNext(Protocol.Response
                    .newBuilder()
                    .setId(request.getId())
                    .setBody(String.format("Response: %s", request.getBody()))
                    .build()
            );
            responseObserver.onCompleted();
        }

        @Override
        public void ping(Protocol.Empty request, StreamObserver<Protocol.Empty> responseObserver) {
            System.out.println("Got ping request");
            responseObserver.onNext(Protocol.Empty.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(12345)
                .addService(new ServiceImpl())
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        server.start();
        server.awaitTermination();
    }
}
