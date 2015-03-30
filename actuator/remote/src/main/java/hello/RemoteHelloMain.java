package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RemoteHelloMain {
    public static void main(final String... args) {
        SpringApplication.run(RemoteHelloMain.class, args);
    }
}
