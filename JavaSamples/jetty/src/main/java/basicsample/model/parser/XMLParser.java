package basicsample.model.parser;

import basicsample.model.entity.User;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

public class XMLParser {
    static JAXBContext jaxbContext = null;
    public static Marshaller jaxbMarshaller = null;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(User.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @XmlRootElement(name = "users")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UserWrapper {
        private List<User> userList;

        public UserWrapper(List<User> userList) {
            this.userList = userList;
        }
    }
}
