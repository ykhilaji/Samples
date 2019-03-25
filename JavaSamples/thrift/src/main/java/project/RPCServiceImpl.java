package project;

import org.apache.thrift.TException;
import test.Message;
import test.Operation;
import test.RPCError;
import test.RPCService;

import java.util.Random;

public class RPCServiceImpl implements RPCService.Iface {
    private Random random = new Random();

    @Override
    public void ping() throws TException {
        System.out.println("Ping");
    }

    @Override
    public void registerClient(int id) throws TException {
        System.out.println(String.format("Register new client with id: %d", id));
    }

    @Override
    public int nextRandom() throws TException {
        return random.nextInt();
    }

    @Override
    public int calculate(Message msg) throws RPCError, TException {
        if (msg.op == Operation.ADD) {
            return msg.a + msg.b;
        } else if (msg.op == Operation.SUBTRACT) {
            return msg.a - msg.b;
        } else if (msg.op == Operation.MULTIPLY) {
            return msg.a * msg.b;
        } else if (msg.op == Operation.DIVIDE) {
            if (msg.b == 0) {
                throw new RPCError(321);
            }

            return msg.a / msg.b;
        } else {
            throw new RPCError(123);
        }
    }
}
