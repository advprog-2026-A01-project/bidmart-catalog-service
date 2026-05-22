package id.ac.ui.cs.advprog.bidmartcatalogservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:filtertest",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
        "app.rabbitmq.exchange=bidmart.events",
        "app.rabbitmq.queue.bid-placed=catalog.bid.placed",
        "app.rabbitmq.queue.auction-closed=catalog.auction.closed",
        "app.rabbitmq.routing-key.bid-placed=bid.placed",
        "app.rabbitmq.routing-key.auction-closed=auction.closed",
        "app.gateway.secret=test-secret",
        "grpc.server.port=0"
})
class GatewayAuthFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requestWithoutSecret_returns403() throws Exception {
        mockMvc.perform(get("/api/listings"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void requestWithWrongSecret_returns403() throws Exception {
        mockMvc.perform(get("/api/listings")
                        .header("X-Gateway-Secret", "wrong-secret"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Missing or invalid gateway secret"));
    }

    @Test
    void requestWithCorrectSecret_passes() throws Exception {
        mockMvc.perform(get("/api/listings")
                        .header("X-Gateway-Secret", "test-secret"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorHealthRequest_bypassesFilter() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
