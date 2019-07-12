package avro.calculator.rpc;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.netty.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class CalculatorClient {
    private final static Logger logger = LogManager.getLogger(CalculatorClient.class);

    private String host;
    private int port;

    public CalculatorClient() {
        this("localhost", 12345);
    }

    public CalculatorClient(int port) {
        this("localhost", port);
    }

    public CalculatorClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static CalculatorClient createInstance(String host, int port) {
        return new CalculatorClient(host, port);
    }

    public void start() throws IOException {
        logger.info("Starting client");
        logger.info("Trying to connect to server: {}:{}", host, port);
        NettyTransceiver transceiver = new NettyTransceiver(new InetSocketAddress(host, port));
        logger.info("Successfully connected");
        Calculator proxy = SpecificRequestor.getClient(Calculator.class, transceiver);
        logger.info("Ready to execute queries");

        Runtime.getRuntime().addShutdownHook(new Thread(transceiver::close));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            logger.info("Input your query:");
            String query = reader.readLine();

            if (query.isEmpty()) {
                break;
            }

            logger.info("Your query: {}", query);
            try {
                Response result = proxy.task(Request.newBuilder().setQuery(query).build());
                logger.info("Result: {}", result.toString());
            } catch (AvroRemoteException e) {
                logger.error(e.getLocalizedMessage());
            }

        }

        logger.info("Disconnect from the server");
        transceiver.close();
    }
}
