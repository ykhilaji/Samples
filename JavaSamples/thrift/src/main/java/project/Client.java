package project;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import test.Message;
import test.Operation;
import test.RPCService;

public class Client {
    public static void main(String[] args) throws TException {
        TTransport transport = new TSocket("localhost", 9000);
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);
        RPCService.Client client = new RPCService.Client(protocol);

        client.ping();
        client.registerClient(1);

        System.out.println(String.format("Next random int: %d", client.nextRandom()));
        System.out.println(String.format("a + b = %d", client.calculate(new Message().setA(1).setB(2).setOp(Operation.ADD))));
        System.out.println(String.format("a - b = %d", client.calculate(new Message().setA(3).setB(2).setOp(Operation.SUBTRACT))));
        System.out.println(String.format("a * b = %d", client.calculate(new Message().setA(2).setB(2).setOp(Operation.MULTIPLY))));
        System.out.println(String.format("a / b = %d", client.calculate(new Message().setA(10).setB(2).setOp(Operation.DIVIDE))));

        transport.close();
    }
}
