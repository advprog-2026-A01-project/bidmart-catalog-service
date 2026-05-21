package id.ac.ui.cs.advprog.bidmartcatalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BidmartCatalogServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidmartCatalogServiceApplication.class, args);
	}

}
