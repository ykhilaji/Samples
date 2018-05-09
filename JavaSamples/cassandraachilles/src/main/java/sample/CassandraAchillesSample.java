package sample;

import com.datastax.driver.core.Cluster;
import info.archinnov.achilles.generated.ManagerFactory;
import info.archinnov.achilles.generated.ManagerFactoryBuilder;
import info.archinnov.achilles.generated.manager.User_Manager;
import sample.model.Address;
import sample.model.Contact;
import sample.model.User;

import java.util.ArrayList;
import java.util.List;

public class CassandraAchillesSample {
    public static void main(String[] args) {
        Cluster cluster = Cluster
                .builder()
                .addContactPoint("192.168.99.100")
                .withPort(9042)
                .build();

        ManagerFactory managerFactory = ManagerFactoryBuilder
                .builder(cluster)
                .withDefaultKeyspaceName("sample")
                .doForceSchemaCreation(true)
                .build();

        User_Manager userManager = managerFactory.forUser();

        Address address = new Address();
        address.setCity("City");
        address.setCountry("Country");

        Contact contact1 = new Contact();
        contact1.setCode("000");
        contact1.setNumber("123321");

        Contact contact2 = new Contact();
        contact2.setCode("111");
        contact2.setNumber("321123");

        User user = new User();
        user.setAge(15);
        user.setId(1);
        user.setFirstName("firstName");
        user.setLastName("lastName");

        List<Contact> contactList = new ArrayList<>();
        contactList.add(contact1);
        contactList.add(contact2);

        user.setAddress(address);
        user.setContactList(contactList);

        userManager
                .crud()
                .insert(user)
                .execute();

        userManager
                .dsl()
                .update()
                .fromBaseTable()
                .age().Set(16)
                .lastName().Set("UpdatedLastName")
                .where().id().Eq(1).firstName().Eq("firstName")
                .execute();

        User updatedUser = userManager
                .crud()
                .findById(1, "firstName")
                .get();

        System.out.println(updatedUser);

        userManager
                .crud()
                .deleteById(1, "firstName");

        System.exit(0);
    }
}