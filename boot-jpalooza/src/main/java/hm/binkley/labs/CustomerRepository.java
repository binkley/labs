package hm.binkley.labs;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CustomerRepository
        extends CrudRepository<Customer, Long> {
    List<Customer> findByName(final String name);
}
