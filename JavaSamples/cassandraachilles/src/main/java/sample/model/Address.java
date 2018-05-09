package sample.model;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.UDT;

@UDT(keyspace = "sample", name = "address")
public class Address {
    @Column
    private String country;
    @Column
    private String city;

    public Address() {
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "Address{" +
                "country='" + country + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
