package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"controller", "repository", "data"})
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "data")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
