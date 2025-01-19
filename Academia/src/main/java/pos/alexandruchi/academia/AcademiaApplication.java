package pos.alexandruchi.academia;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
public class AcademiaApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(AcademiaApplication.class, args);
        } catch (BeanCreationException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
}
