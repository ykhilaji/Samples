package basic;

import org.apache.kafka.common.serialization.Serializer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

public class CustomSerializer {
    public static class User {
        private String firstName;
        private String lastName;

        public User() {
        }

        public String getFirstName() {
            return firstName != null ? firstName : "";
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName != null ? lastName : "";
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    public static class UserSerializer implements Serializer<User> {
        @Override
        public void configure(Map<String, ?> map, boolean b) {

        }

        @Override
        public byte[] serialize(String topic, User user) {
            if (user == null) {
                return null;
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(user.getFirstName().length() + user.getLastName().length());

            try {
                byteBuffer.put(user.getFirstName().getBytes("UTF-8"));
                byteBuffer.put(user.getLastName().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return byteBuffer.array();

        }

        @Override
        public void close() {

        }
    }
}
