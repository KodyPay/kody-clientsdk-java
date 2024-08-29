package terminal;

import common.PaymentClient;
import com.kodypay.grpc.pay.v1.PayResponse;
import com.kodypay.grpc.pay.v1.PaymentStatus;
import com.kodypay.grpc.pay.v1.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static common.Utils.loadProperties;

public class TerminalAsyncJavaClient {
    private static final Logger LOG = LoggerFactory.getLogger(TerminalAsyncJavaClient.class.getName());

    private final PaymentClient client;
    private String exTerminalId = "AMS1-000168230430466";

    public TerminalAsyncJavaClient() {
        Properties properties = loadProperties();
        var address = URI.create(properties.getProperty("address", "http://localhost"));
        var apiKey = properties.getProperty("apiKey");
        if (apiKey == null)
            throw new IllegalArgumentException("Invalid config, expected apiKey");

        var storeId = UUID.fromString(properties.getProperty("storeId"));

        client = new PaymentClient(address, storeId, apiKey);
    }

    public CompletableFuture<PayResponse> sendPaymentAsync(String amountStr) {
        LOG.info("Sending payment for amount: {} to terminal: {}", amountStr, exTerminalId);
        BigDecimal amount = new BigDecimal(amountStr);
        CompletableFuture<PayResponse> response = client.sendPayment(exTerminalId, amount, orderId -> {
            LOG.info("onPending: orderId={}", orderId);
            // optionally cancel payment after delay
            Executor delayed = CompletableFuture.delayedExecutor(10L, TimeUnit.SECONDS);

            CompletableFuture.supplyAsync(() -> {
                LOG.info("Cancelling payment: {} = {}", amount, orderId);
                return cancelPaymentAsync(amountStr, orderId);
            }, delayed);
        });
        response.thenAccept(res -> LOG.info("Sent Payment: {}, {}", amount, dump(res)));
        return response;
    }

    public CompletableFuture<PaymentStatus> cancelPaymentAsync(String amountStr, String orderId) {
        LOG.info("Cancel payment: {} with amount: {}", orderId, amountStr);
        BigDecimal amount = new BigDecimal(amountStr);

        CompletableFuture<PaymentStatus> response = client.cancelPayment(amount, exTerminalId, orderId);

        response.thenAccept(res -> LOG.info("Cancelled Payment: {} = {}", orderId, res));

        return response;
    }

    public CompletableFuture<PayResponse> getDetailsAsync(String orderId) {
        LOG.info("Get payment details for: {}", orderId);

        CompletableFuture<PayResponse> details = client.getDetails(orderId);

        details.thenAccept(res -> LOG.info("Payment Details: {} = {}", orderId, dump(res)));

        return details;
    }

    public CompletableFuture<List<Terminal>> getTerminalsAsync() {
        LOG.info("Get terminals");
        CompletableFuture<List<Terminal>> response = client.getTerminals();
        response.thenAccept(it -> {
            this.exTerminalId = it.stream()
                    .filter(Terminal::getOnline)
                    .findFirst()
                    .orElse(it.getFirst())
                    .getTerminalId();
            LOG.info("Selected terminal: {}", exTerminalId);
        });

        return response;
    }

    public static void main(String[] args) {
        TerminalAsyncJavaClient terminalAsyncJavaClient = new TerminalAsyncJavaClient();
        String amountStr = "1.00";

        terminalAsyncJavaClient.getTerminalsAsync().thenAccept(terminals ->
                LOG.info("Terminals: {}", terminals.stream()
                        .map(t -> t.getTerminalId() + " = " + t.getOnline())
                        .collect(Collectors.joining(", ")))
        );

        terminalAsyncJavaClient.sendPaymentAsync(amountStr).thenAccept(order -> {
            var orderId = order.getOrderId();
            LOG.info("Completed order: {}", orderId);

            terminalAsyncJavaClient.getDetailsAsync(orderId).thenAccept(details -> {
                var status = details.getStatus();
                LOG.info("Payment status: {}", status);

                if (status == PaymentStatus.PENDING) {
                    terminalAsyncJavaClient.cancelPaymentAsync(amountStr, orderId).thenAccept(cancel -> {
                        LOG.info("Order is cancelled? {}", cancel == PaymentStatus.CANCELLED);
                    });
                }
            });
        });
    }

    private static String dump(PayResponse msg) {
        return msg.getAllFields().entrySet().stream().map(it -> {
            var key = it.getKey().getName();
            var value = it.getValue().toString().replace('\n', ' ');
            return key + "=" + value;
        }).collect(Collectors.joining(", "));
    }
}
