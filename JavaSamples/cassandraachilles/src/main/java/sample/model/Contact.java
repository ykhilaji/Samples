package sample.model;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.UDT;

@UDT(keyspace = "sample", name = "contact")
public class Contact {
    @Column
    private String code;
    @Column
    private String number;

    public Contact() {
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
    public String toString() {
        return "Contact{" +
                "code='" + code + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
