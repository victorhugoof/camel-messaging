package br.com.victorhugoof.camel.rabbitmq.models;

import static java.util.Objects.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RabbitMQRequestError implements Serializable {

    private String message;
    private String className;
    private RabbitMQRequestError cause;

    public static RabbitMQRequestError of(Throwable e) {
        var cause = nonNull(e.getCause()) ? of(e.getCause()) : null;

        return RabbitMQRequestError.builder()
                .message(e.getMessage())
                .className(e.getClass().getName())
                .cause(cause)
                .build();
    }
}
