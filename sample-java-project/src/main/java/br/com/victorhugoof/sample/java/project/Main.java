package br.com.victorhugoof.sample.java.project;

import br.com.victorhugoof.camel.CamelInstance;
import br.com.victorhugoof.camel.rabbitmq.RabbitMQ;
import br.com.victorhugoof.camel.rabbitmq.models.RabbitMQProducer;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

@Slf4j
public class Main {

    public static void main(String[] args) throws InterruptedException {
        changeRootLogLevel(Level.INFO);
        CamelInstance.init();

        while (true) {
            publish();
            get();
            Thread.sleep(1000);
        }
    }

    private static void changeRootLogLevel(Level level) {
        var root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    private static void publish() {
        var producer = RabbitMQProducer.builder()
                .exchange("br.com.victorhugoof")
                .routingKey("br.com.victorhugoof.teste")
                .build();

        RabbitMQ.getInstance().publish(producer, "Publiquei do Main. " + System.currentTimeMillis());
    }

    private static void get() {
        var producer = RabbitMQProducer.builder()
                .exchange("br.com.victorhugoof")
                .routingKey("br.com.victorhugoof.message")
                .build();

        var response = RabbitMQ.getInstance().get(producer, "Requisitei do Main. " + System.currentTimeMillis(), String.class);
        log.info("Recebi no main: {}", response);
    }
}
