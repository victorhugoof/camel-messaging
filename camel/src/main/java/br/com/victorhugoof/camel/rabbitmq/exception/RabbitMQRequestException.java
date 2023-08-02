package br.com.victorhugoof.camel.rabbitmq.exception;

import br.com.victorhugoof.camel.rabbitmq.models.RabbitMQRequestError;
import static java.util.Objects.*;
import lombok.Getter;

@Getter
public class RabbitMQRequestException extends RuntimeException {
    private final String className;

    public RabbitMQRequestException(RabbitMQRequestError error) {
        super(error.getMessage(), getCause(error));
        this.className = error.getClassName();
    }

    public static RabbitMQRequestException getCause(RabbitMQRequestError error) {
        if (nonNull(error.getCause())) {
            return new RabbitMQRequestException(error.getCause());
        }
        return null;
    }
}
