package stream.filter.flink;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

// https://github.com/twitter/chill/blob/develop/chill-thrift/src/main/java/com/twitter/chill/thrift/TBaseSerializer.java
public class TBaseSerializer extends Serializer<TBase> {
    private final TSerializer serializer  = new TSerializer();
    private final TDeserializer deserializer  = new TDeserializer();

    @Override
    public void write(Kryo kryo, Output output, TBase tBase) {
        try {
            byte[] serThrift = serializer.serialize(tBase);
            output.writeInt(serThrift.length, true);
            output.writeBytes(serThrift);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TBase read(Kryo kryo, Input input, Class<TBase> tBaseClass) {
        try {
            TBase prototype = tBaseClass.newInstance();
            int tSize = input.readInt(true);
            byte[] barr = new byte[tSize];
            input.readBytes(barr);
            deserializer.deserialize(prototype, barr);
            return prototype;
        } catch (Exception e) {
            throw new RuntimeException("Could not create " + tBaseClass, e);
        }
    }
}