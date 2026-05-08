package id.ac.ui.cs.advprog.bidmartcatalogservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.bid-placed}")
    private String bidPlacedQueue;

    @Value("${app.rabbitmq.queue.auction-closed}")
    private String auctionClosedQueue;

    @Value("${app.rabbitmq.routing-key.bid-placed}")
    private String bidPlacedRoutingKey;

    @Value("${app.rabbitmq.routing-key.auction-closed}")
    private String auctionClosedRoutingKey;

    @Bean
    public TopicExchange bidmartExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue bidPlacedQueue() {
        return new Queue(bidPlacedQueue, true);
    }

    @Bean
    public Queue auctionClosedQueue() {
        return new Queue(auctionClosedQueue, true);
    }

    @Bean
    public Binding bidPlacedBinding() {
        return BindingBuilder
                .bind(bidPlacedQueue())
                .to(bidmartExchange())
                .with(bidPlacedRoutingKey);
    }

    @Bean
    public Binding auctionClosedBinding() {
        return BindingBuilder
                .bind(auctionClosedQueue())
                .to(bidmartExchange())
                .with(auctionClosedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}