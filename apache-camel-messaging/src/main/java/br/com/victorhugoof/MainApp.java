package br.com.victorhugoof;

import br.com.victorhugoof.camel.CamelInstance;
import br.com.victorhugoof.camel.rabbitmq.RabbitMQ;
import br.com.victorhugoof.camel.rabbitmq.models.RabbitMQListener;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.main.BaseMainSupport;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class MainApp {
    private static final AtomicReference<String> LAST_MESSAGE = new AtomicReference<>();

    public static void main(String... args) throws Exception {
        changeRootLogLevel(Level.INFO);

        Main main = new Main();
        main.addMainListener(new MainListenerSupport() {
            @Override
            public void afterConfigure(BaseMainSupport main) {
                CamelInstance.init(main.getCamelContext());
                startListen();
                startHandler();
            }
        });
        main.run(args);
    }

    private static void changeRootLogLevel(Level level) {
        var root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    private static void startListen() {
        var listener = RabbitMQListener.builder()
                .exchange("br.com.victorhugoof")
                .queue("br.com.victorhugoof.teste.queue")
                .routingKey("br.com.victorhugoof.teste")
                .build();

        RabbitMQ.getInstance().listen(listener, message -> {
            log.info("Recebi a mensagem no listem do MainApp: {}", message.getBody(String.class));
            LAST_MESSAGE.set(message.getBody(String.class));
        });
    }

    private static void startHandler() {
        var listener = RabbitMQListener.builder()
                .exchange("br.com.victorhugoof")
                .queue("br.com.victorhugoof.message.queue")
                .routingKey("br.com.victorhugoof.message")
                .build();

        RabbitMQ.getInstance().handle(listener, message -> {
            log.info("Recebi a mensagem no handler do MainApp: {}", message.getBody(String.class));
            throw new RuntimeException("Teste", new RuntimeException("Causa"));
//            return LAST_MESSAGE.get();
        });
    }
}

