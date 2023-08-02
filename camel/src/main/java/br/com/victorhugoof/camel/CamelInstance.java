package br.com.victorhugoof.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jackson.JacksonConstants;
import org.apache.camel.impl.DefaultCamelContext;

@Slf4j
public class CamelInstance {
    private static CamelContext contextInstance;

    public static void init() {
        var context = new DefaultCamelContext();
        context.start();
        init(context);
    }

    public static void init(CamelContext context) {
        log.info("CamelContext set!");
        contextInstance = context;
        contextInstance.getGlobalOptions().put(JacksonConstants.ENABLE_TYPE_CONVERTER, "true");
        contextInstance.getGlobalOptions().put(JacksonConstants.TYPE_CONVERTER_TO_POJO, "true");
    }

    public static CamelContext getContext() {
        return contextInstance;
    }
}
