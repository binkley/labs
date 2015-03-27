package hello;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * {@code HelloWorldSwagger} <strong>needs documentation</strong>.
 *
 * @author <a href="mailto:boxley@thoughtworks.com">Brian Oxley</a>
 * @todo Needs documentation
 */
@ComponentScan("hello")
@Configuration
@EnableWebMvc
@EnableSwagger
public class HelloWorldSwagger {
    @Bean
    public SpringSwaggerConfig config() {
        return new SpringSwaggerConfig();
    }

    @Bean
    public SwaggerSpringMvcPlugin custom(final SpringSwaggerConfig config) {
        return new SwaggerSpringMvcPlugin(config);
    }
}
