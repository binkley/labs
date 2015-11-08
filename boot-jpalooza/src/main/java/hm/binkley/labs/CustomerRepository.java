package hm.binkley.labs;

import org.springframework.data.repository.CrudRepository;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomerRepository
        extends CrudRepository<Customer, Long> {
    List<Customer> findByName(@Nonnull final String name);
}
