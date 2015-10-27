package hm.binkley.labs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

@SpringBootApplication
public class Application {
    public static void main(final String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner demo(final CustomerRepository repository) {
        return args -> {
            repository.save(new Customer("Bob"));
            repository.save(new Customer("Nancy"));

            repository.findAll().
                    forEach(out::println);

            out.println("--");

            out.println(repository.findOne(1L));

            out.println("--");

            repository.findByName("Nancy").
                    forEach(out::println);

            // To see JMX
            TimeUnit.MINUTES.sleep(5);
        };
    }
}
