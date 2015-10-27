package hm.binkley.labs;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@ToString
public class Customer {
    @Id
    @GeneratedValue
    private long id;
    @Getter
    @Setter
    private String name;

    protected Customer() {}

    public Customer(final String name) {
        this.name = name;
    }
}
