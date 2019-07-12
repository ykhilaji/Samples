package avro.calculator.rpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AvroCalculatorRpc {
    private final static Logger logger = LogManager.getLogger(AvroCalculatorRpc.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length == 0) {
            logger.warn("Usage: *.jar [server port] | [client host port]");
            return;
        }

        if ("server".equals(args[0])) {
            if (args.length < 2) {
                logger.warn("Usage: *.jar [server port] | [client host port]");
                return;
            }

            int port = Integer.parseInt(args[1]);
            CalculatorServer.createInstance(port).start();
        } else if ("client".equals(args[0])) {
            if (args.length < 3) {
                logger.warn("Usage: *.jar [server port] | [client host port]");
                return;
            }

            String host = args[1];
            int port = Integer.parseInt(args[2]);
            CalculatorClient.createInstance(host, port).start();
        } else {
            logger.warn("Usage: *.jar [server port] | [client host port]");
        }
    }
}
