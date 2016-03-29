package hm.binkley.labs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation
        .EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation
        .JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation
        .StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@EnableAutoConfiguration
public class BatchConfiguration {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1").
                tasklet((contribution, chunkContext) -> null).
                build();
    }

    @Bean
    public Job job(final Step step1)
            throws Exception {
        return jobBuilderFactory.get("job1").
                incrementer(new RunIdIncrementer()).start(step1).
                build();
    }

    public static final class Main {
        public static void main(final String... args) {
            System.exit(SpringApplication.exit(SpringApplication
                    .run(BatchConfiguration.class, args)));
        }
    }
}
