package jp.co.rottenpear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SpringBootTesterApplication {

    static {
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTesterApplication.class, args);
    }

}
