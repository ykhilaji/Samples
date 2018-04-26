import core.configuration.AppConfiguration;
import core.model.Address;
import core.model.Contact;
import core.model.Gender;
import core.model.User;
import core.repository.AddressRepository;
import core.repository.ContactRepository;
import core.repository.UserRepository;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfiguration.class})
public class RedisCrudTest {
    private Logger logger = Logger.getLogger(RedisCrudTest.class);

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisKeyValueTemplate redisKeyValueTemplate;

    @After
    public void clear() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    public void userSaving() throws InterruptedException {
        User user = new User(1, "name", "lastname", 20, Gender.MALE, null, Collections.emptyList(), 10);
        userRepository.save(user);

        assertTrue(userRepository.existsById(1L));
        assertEquals(user, userRepository.findById(1L).orElse(null));
    }

    @Test
    public void addressSaving() throws InterruptedException {
        Address address = new Address(1, "Country", "city", "street", 1);
        addressRepository.save(address);

        assertTrue(addressRepository.existsById(1L));
        assertEquals(address, addressRepository.findById(1L).orElse(null));
    }

    @Test
    public void contactSaving() throws InterruptedException {
        Contact contact = new Contact(1, "000", "123-456-789");
        contactRepository.save(contact);

        assertTrue(contactRepository.existsById(1L));
        assertEquals(contact, contactRepository.findById(1L).orElse(null));
    }

    @Test
    public void getContactsByCode() {
        Contact contact1 = new Contact(1, "000", "123");
        Contact contact2 = new Contact(2, "000", "456");
        Contact contact3 = new Contact(3, "001", "456");
        contactRepository.saveAll(Arrays.asList(contact1, contact2, contact3));

        assertArrayEquals(Arrays.asList(contact1, contact2).toArray(), contactRepository.findByCode("000").toArray());
    }

    @Test
    public void saveUserWithContactAndAddressReference() {
        Address address = new Address(1, "Country", "city", "street", 1);
        Contact contact = new Contact(1, "000", "123-456-789");
        User user = new User(1, "name", "lastname", 20, Gender.MALE, address, Collections.singletonList(contact), 10);

        userRepository.save(user);
        addressRepository.save(address);
        contactRepository.save(contact);

        assertTrue(addressRepository.existsById(1L));
        assertTrue(contactRepository.existsById(1L));
        assertTrue(userRepository.existsById(1L));

        assertEquals(address, addressRepository.findById(1L).orElse(null));
        assertEquals(contact, contactRepository.findById(1L).orElse(null));
        assertEquals(user, userRepository.findById(1L).orElse(null));

        assertEquals(address, userRepository.findById(1L).orElse(null).getAddress());
        assertEquals(Collections.singletonList(contact), userRepository.findById(1L).orElse(null).getContacts());
    }

    @Test
    public void userPartialUpdate() {
        User user = new User(1, "name", "lastname", 20, Gender.MALE, null, Collections.emptyList(), -1);
        userRepository.save(user);

        PartialUpdate<User> partialUpdate = new PartialUpdate<>(1L, User.class)
                .set("firstName", "updatedName")
                .set("age", 21);

        redisKeyValueTemplate.update(partialUpdate);
        assertEquals(new User(1, "updatedName", "lastname", 21, Gender.MALE, null, Collections.emptyList(), -1), userRepository.findById(1L).orElse(null));
    }
}
