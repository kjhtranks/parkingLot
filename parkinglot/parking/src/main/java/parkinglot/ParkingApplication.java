package parkinglot;
import parkinglot.config.kafka.KafkaProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableBinding(KafkaProcessor.class)
@EnableFeignClients
public class ParkingApplication {
    protected static ApplicationContext applicationContext;
    public static void main(String[] args) {
        applicationContext = SpringApplication.run(ParkingApplication.class, args);
    }
}
