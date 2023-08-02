package br.com.victorhugoof.camel.helper;

import lombok.experimental.UtilityClass;
import org.slf4j.helpers.MessageFormatter;

import java.util.concurrent.Callable;

@UtilityClass
public class CamelHelper {

    public static String format(String pattern, Object ... arguments) {
        return MessageFormatter.arrayFormat(pattern, arguments).getMessage();
    }

    public static void wrapException(RunnableException runnable) {
        wrapException(() -> {
            runnable.run();
            return null;
        });
    }

    public static <R> R wrapException(Callable<R> consumer) {
        try {
            return consumer.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
