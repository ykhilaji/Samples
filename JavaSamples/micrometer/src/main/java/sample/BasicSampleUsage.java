package sample;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.*;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.Collections;

public class BasicSampleUsage {
    public static void main(String[] args) throws InterruptedException {
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        createEndpoint(prometheusRegistry);
        exposeGcStatictics(prometheusRegistry);
    }

    public static void createEndpoint(PrometheusMeterRegistry prometheusRegistry) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/metrics", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void exposeGcStatictics(final MeterRegistry registry) {
        new Thread(() -> {
            while (true) {
                registry.counter("updates").increment();

                ManagementFactory.getGarbageCollectorMXBeans().forEach(m -> {
                    registry.gauge("gc_cycles", m.getCollectionCount());
                });

                ManagementFactory.getMemoryPoolMXBeans().forEach(m -> {
                    registry.gauge(m.getName(), Collections.singletonList(new ImmutableTag("type", "used")), m.getUsage().getUsed());
                    registry.gauge(m.getName(), Collections.singletonList(new ImmutableTag("type", "max")), m.getUsage().getMax());
                    registry.gauge(m.getName(), Collections.singletonList(new ImmutableTag("type", "init")), m.getUsage().getInit());
                    registry.gauge(m.getName(), Collections.singletonList(new ImmutableTag("type", "committed")), m.getUsage().getCommitted());
                });

                registry.gauge("threads", ManagementFactory.getThreadMXBean().getThreadCount());

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
