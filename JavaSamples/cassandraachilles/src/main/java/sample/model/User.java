package sample.model;

import com.datastax.driver.core.ConsistencyLevel;
import info.archinnov.achilles.annotations.*;

import java.util.List;

@Table(keyspace = "sample", table = "user")
@Consistency(read = ConsistencyLevel.QUORUM, write = ConsistencyLevel.QUORUM, serial = ConsistencyLevel.SERIAL)
public class User {
    @PartitionKey
    private int id;
    @ClusteringColumn(asc = true)
    private String firstName;
    @Column
    @Index
    private String lastName;
    @Column
    private int age;
    @Column
    private @Frozen Address address;
    @Column
    private List<@Frozen Contact> contactList;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", address=" + address +
                ", contactList=" + contactList +
                '}';
    }
}