package br.com.victorhugoof.camel.rabbitmq.configuration;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelConfiguration;
import org.apache.camel.CamelContext;
import org.apache.camel.component.rabbitmq.RabbitMQComponent;

import java.util.HashMap;
import java.util.UUID;

@Slf4j
public class RabbitMQConfiguration implements CamelConfiguration {

    @Override
    public void configure(CamelContext camelContext) {
        log.info("Configuring RabbitMQ component on CamelContext");

        var connection = buildConnectionFactory();
        log.info("RabbitMQ will connect to {}@{}:{}", connection.getUsername(), connection.getHost(), connection.getPort());

        var rabbitmq = new RabbitMQComponent();
        rabbitmq.setConnectionFactory(connection);
        rabbitmq.setAutoDelete(false);
        rabbitmq.setAutoAck(false);
        rabbitmq.setTransferException(false); // FIXME pq nao funcioan essa bosta

        camelContext.addComponent("rabbitmq", rabbitmq);
        log.info("Component RabbitMQ configurated");
    }

    private ConnectionFactory buildConnectionFactory() {
        var clientProperties = new HashMap<String, Object>();
        clientProperties.put("connection_name", "apache-camel-" + UUID.randomUUID());

        var factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setClientProperties(clientProperties);
        return factory;
    }
}
