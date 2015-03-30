package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class HelloWorldMain {
    public static void main(final String... args) {
        SpringApplication.run(HelloWorldMain.class, args);
    }
}
