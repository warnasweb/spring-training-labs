package training.payment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication(scanBasePackages={"training.payment","training.common"})
@EnableScheduling
public class Application { public static void main(String[] args) { SpringApplication.run(Application.class,args); } }
