package ee.tenman.automaks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class AutomaksApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutomaksApplication.class, args);
    }

}
