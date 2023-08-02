package br.com.victorhugoof.camel.rabbitmq.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RabbitMQListener {
    private final RabbitMQExchange exchange;
    private final RabbitMQQueue queue;
    private final String routingKey;

    public static class RabbitMQListenerBuilder {
        public RabbitMQListenerBuilder exchange(RabbitMQExchange exchange) {
            this.exchange = exchange;
            return this;
        }

        public RabbitMQListenerBuilder exchange(String exchangeName) {
            return exchange(RabbitMQExchange.builder().name(exchangeName).build());
        }

        public RabbitMQListenerBuilder queue(RabbitMQQueue queue) {
            this.queue = queue;
            return this;
        }

        public RabbitMQListenerBuilder queue(String queueName) {
            return queue(RabbitMQQueue.builder().name(queueName).build());
        }
    }
}
