package core.component;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class CustomConsumer extends DefaultConsumer {
    private CustomEndpoint endpoint;
    private Timer timer;

    public CustomConsumer(CustomEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.timer = new Timer();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        this.timer.cancel();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Exchange exchange = getEndpoint().createExchange();

                exchange.setProperty(Exchange.TIMER_FIRED_TIME, LocalDateTime.now());
                exchange.getOut().setBody(UUID.randomUUID());

                try {
                    getProcessor().process(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },0, 5000);
    }
}
