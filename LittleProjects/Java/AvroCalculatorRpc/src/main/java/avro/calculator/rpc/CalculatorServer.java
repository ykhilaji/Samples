package avro.calculator.rpc;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.netty.NettyServer;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class CalculatorServer {
    private final static Logger logger = LogManager.getLogger(CalculatorServer.class);

    private int port;

    static class CalculatorProtocolImpl implements Calculator {
        @Override
        public Response task(Request request) throws AvroRemoteException, IncorrectQuery {
            logger.info("Got request: {}", request.toString());

            try {
                Double result = CalculatorLogic.execute(request.getQuery());
                return Response.newBuilder().setResult(result).setQuery(request.getQuery()).build();
            } catch (IllegalArgumentException e) {
                logger.error("Incorrect request: {}", request.toString());
                throw new IncorrectQuery(e);
            }
        }
    }

    public CalculatorServer() {
        this(12345);
    }

    public CalculatorServer(int port) {
        this.port = port;
    }

    public static CalculatorServer createInstance(int port) {
        return new CalculatorServer(port);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        logger.info("Starting server on port: {}", port);
        Server server = new NettyServer(new SpecificResponder(Calculator.PROTOCOL, new CalculatorProtocolImpl()), new InetSocketAddress(port));
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        logger.info("Server started");

        server.join();
    }
}
