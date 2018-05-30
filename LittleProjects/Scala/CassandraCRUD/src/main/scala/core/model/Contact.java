package core.model;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;

@UDT(keyspace = "users", name = "contact")
public class Contact {
    @Field(name = "code")
    private String code;
    @Field(name = "number")
    private String number;

    public Contact() {
    }

    public Contact(String code, String number) {
        this.code = code;
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        if (code != null ? !code.equals(contact.code) : contact.code != null) return false;
        return number != null ? number.equals(contact.number) : contact.number == null;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "code='" + code + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
