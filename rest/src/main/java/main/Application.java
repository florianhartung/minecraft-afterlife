package main;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"controller", "repository", "data", "ui"})
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "data")
@EnableVaadin("ui")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
