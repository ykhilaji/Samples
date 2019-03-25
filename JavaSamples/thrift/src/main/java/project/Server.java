package project;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import test.RPCService;

public class Server {
    public static void main(String[] args) throws TTransportException {
        RPCService.Iface rpc = new RPCServiceImpl();
        RPCService.Processor processor = new RPCService.Processor(rpc);
        TServerTransport serverTransport = new TServerSocket(9000);
        TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.serve();
    }
}
