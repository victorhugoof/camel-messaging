package br.com.victorhugoof.camel.rabbitmq.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RabbitMQExchange {
    private final String name;

    @Builder.Default
    private final Type type = Type.TOPIC;

    @Builder.Default
    private final boolean durable = true;

    @Builder.Default
    private final boolean autoDelete = false;

    public enum Type {
        DIRECT, TOPIC
    }
}
