package hello;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;

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
    @Inject
    private SpringSwaggerConfig config;

    @Bean
    public SwaggerSpringMvcPlugin custom() {
        return new SwaggerSpringMvcPlugin(config);
    }
}
