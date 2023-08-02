package br.com.victorhugoof.camel.rabbitmq;

import br.com.victorhugoof.camel.CamelInstance;
import static br.com.victorhugoof.camel.helper.CamelHelper.*;
import br.com.victorhugoof.camel.rabbitmq.configuration.RabbitMQConfiguration;
import br.com.victorhugoof.camel.rabbitmq.exception.RabbitMQRequestException;
import br.com.victorhugoof.camel.rabbitmq.models.RabbitMQListener;
import br.com.victorhugoof.camel.rabbitmq.models.RabbitMQProducer;
import br.com.victorhugoof.camel.rabbitmq.models.RabbitMQRequestError;
import static java.util.Objects.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.util.URISupport;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RabbitMQ {

    @Getter
    private static final RabbitMQ instance = new RabbitMQ();

    public void listen(RabbitMQListener listener, Consumer<Message> consumer) {
        configure();

        var endpoint = toEndpoint(listener);
        log.info("Starting listener to endpoint {}", endpoint);
        addRoutes(builder -> builder.from(endpoint)
                .id(format("rabbitmq-listener.{}", listener.getQueue().getName()))
                .setExchangePattern(ExchangePattern.InOnly)
                .process(exchange -> consumer.accept(exchange.getIn())));
    }

    public <R> void handle(RabbitMQListener listener, Function<Message, R> handler) {
        configure();

        var endpoint = toEndpoint(listener);
        log.info("Starting handler to endpoint {}", endpoint);
        addRoutes(builder -> builder.from(endpoint)
                .id(format("rabbitmq-handler.{}", listener.getQueue().getName()))
                .setExchangePattern(ExchangePattern.InOut)
                .process(exchange -> {
                    try {
                        var output = handler.apply(exchange.getIn());
                        exchange.getMessage().setBody(output);
                    } catch (Throwable e) {
                        exchange.getMessage().setBody(RabbitMQRequestError.of(e));
                        exchange.getMessage().setHeader("X-Error-Response", true);
                    }
                }));
    }

    public void publish(RabbitMQProducer producer, Object payload) {
        configure();

        var endpoint = toEndpoint(producer);
        log.info("Publishing to endpoint {} with payload {}", endpoint, payload);
        wrapException(() -> {
            try (var template = CamelInstance.getContext().createProducerTemplate()) {
                template.sendBody(endpoint, payload);
            }
        });
    }

    public CompletableFuture<Object> publishAsync(RabbitMQProducer producer, Object payload) {
        configure();

        var endpoint = toEndpoint(producer);
        log.info("Publishing to endpoint {} with payload {}", endpoint, payload);
        return wrapException(() -> {
            try (var template = CamelInstance.getContext().createProducerTemplate()) {
                return template.asyncSendBody(endpoint, payload);
            }
        });
    }

    public <R> R get(RabbitMQProducer producer, Object payload, Class<R> responseClass) {
        configure();

        var endpoint = toEndpoint(producer);
        log.info("Requesting from endpoint {} with payload {}", endpoint, payload);
        return wrapException(() -> {
            try (var template = CamelInstance.getContext().createProducerTemplate()) {
                var exchange = template.request(endpoint, inputExchange -> inputExchange.getIn().setBody(payload));
                return convertExchange(exchange, responseClass);
            }
        });
    }

    public <R> CompletableFuture<R> getAsync(RabbitMQProducer producer, Object payload, Class<R> responseClass) {
        configure();

        var endpoint = toEndpoint(producer);
        log.info("Requesting from endpoint {} with payload {}", endpoint, payload);
        return wrapException(() -> {
            try (var template = CamelInstance.getContext().createProducerTemplate()) {
                return template
                        .asyncSend(endpoint, inputExchange -> {
                            inputExchange.setPattern(ExchangePattern.InOut);
                            inputExchange.getIn().setBody(payload);
                        })
                        .thenApply(exchange -> convertExchange(exchange, responseClass));
            }
        });
    }

    private void configure() {
        wrapException(() -> {
            var context = CamelInstance.getContext();
            var rabbitmq = context.hasComponent("rabbitmq");
            if (isNull(rabbitmq)) {
                new RabbitMQConfiguration().configure(context);
            }
        });
    }

    private void addRoutes(Consumer<RouteBuilder> configure) {
        wrapException(() -> {
            CamelInstance.getContext().addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    configure.accept(this);
                }
            });
        });
    }

    private String toEndpoint(RabbitMQProducer producer) {
        return wrapException(() -> {
            var parameters = new HashMap<String, Object>();
            parameters.put("routingKey", producer.getRoutingKey());
            parameters.put("autoDelete", true);
            parameters.put("declare", false);
            parameters.put("requestTimeout", producer.getRequestTimeout());

            return format("rabbitmq://{}?{}", producer.getExchange(), URISupport.createQueryString(parameters));
        });
    }

    private String toEndpoint(RabbitMQListener listener) {
        return wrapException(() -> {
            var parameters = new HashMap<String, Object>();
            parameters.put("exchangeType", listener.getExchange().getType().name().toLowerCase());
            parameters.put("queue", listener.getQueue().getName());
            parameters.put("routingKey", listener.getRoutingKey());
            parameters.put("autoDelete", listener.getQueue().isAutoDelete());
            parameters.put("prefetchCount", listener.getQueue().getPrefetchCount());
            parameters.put("prefetchEnabled", true);
            parameters.put("prefetchSize", 0);

            return format("rabbitmq://{}?{}", listener.getExchange().getName(), URISupport.createQueryString(parameters));
        });
    }

    private <R> R convertExchange(Exchange exchange, Class<R> responseClass) {
        if (Boolean.TRUE.equals(exchange.getMessage().getHeader("X-Error-Response", Boolean.class))) {
            var error = exchange.getMessage().getBody(RabbitMQRequestError.class);
            throw new RabbitMQRequestException(error);
        }
        return exchange.getMessage().getBody(responseClass);
    }
}
