package id.ac.ui.cs.advprog.bidmartcatalogservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
        "app.rabbitmq.exchange=bidmart.events",
        "app.rabbitmq.queue.bid-placed=catalog.bid.placed",
        "app.rabbitmq.queue.auction-closed=catalog.auction.closed",
        "app.rabbitmq.routing-key.bid-placed=bid.placed",
        "app.rabbitmq.routing-key.auction-closed=auction.closed",
        "app.gateway.secret=test-secret"
})
class BidmartCatalogServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(applicationContext, "Application context should not be null");
    }
}