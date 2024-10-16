package terminal;

import cli.Command;
import cli.PaymentCommand;
import cli.PaymentInput;
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.PayResponse;
import com.kodypay.grpc.pay.v1.PaymentStatus;
import com.kodypay.grpc.pay.v1.Terminal;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static common.Utils.loadProperties;

public class TerminalJavaClient {
    private static final Logger LOG = LoggerFactory.getLogger(TerminalJavaClient.class.getName());
    private static final TerminalJavaClient terminalClient = new TerminalJavaClient();


    private final PaymentClient client;
    private String exTerminalId;
    private final long timeout = 1;
    public TerminalJavaClient() {
        Properties properties = loadProperties();
        var address = URI.create(properties.getProperty("address", "http://localhost"));
        var apiKey = properties.getProperty("apiKey");
        if (apiKey == null)
            throw new IllegalArgumentException("Invalid config, expected apiKey");

        var storeId = UUID.fromString(properties.getProperty("storeId"));

        client = new PaymentClient(address, storeId, apiKey);
    }

    public PayResponse sendPayment(String amountStr) throws ExecutionException, InterruptedException, TimeoutException {
       return sendPayment(amountStr, false);
    }

    public PayResponse sendPayment(String amountStr, boolean showTips) throws ExecutionException, InterruptedException, TimeoutException {
        LOG.info("Sending payment for amount: {} to terminal: {}", amountStr, exTerminalId);
        BigDecimal amount = new BigDecimal(amountStr);
        CompletableFuture<PayResponse> response = client.sendPayment(exTerminalId, amount, showTips, orderId-> {
            LOG.info("onPending: orderId={}", orderId);
            // optionally cancel payment after delay
            Executor delayed = CompletableFuture.delayedExecutor(30L, TimeUnit.SECONDS);

            CompletableFuture.supplyAsync(() -> {
                LOG.info("Cancelling payment: {} = {}", amount, orderId);
                return cancelPaymentAsync(amountStr, orderId);
            }, delayed);
        });
        response.thenAccept(res -> LOG.info("Sent Payment: {}, {}", amount, dump(res)));
        return response.get(timeout, TimeUnit.MINUTES);
    }

    public CompletableFuture<PaymentStatus> cancelPaymentAsync(String amountStr, String orderId) {
        LOG.info("Cancel payment: {} with amount: {}", orderId, amountStr);
        BigDecimal amount = new BigDecimal(amountStr);

        CompletableFuture<PaymentStatus> response = client.cancelPayment(amount, exTerminalId, orderId);

        response.thenAccept(res -> LOG.info("Cancelled Payment: {} = {}", orderId, res));

        return response;
    }

    public PayResponse getDetails(String orderId) throws ExecutionException, InterruptedException, TimeoutException {
        LOG.info("Get payment details for: {}", orderId);

        CompletableFuture<PayResponse> details = client.getDetails(orderId);

        details.thenAccept(res -> LOG.info("Payment Details: {} = {}", orderId, dump(res)));

        return details.get(timeout, TimeUnit.MINUTES);
    }

    public List<Terminal> getTerminals() throws ExecutionException, InterruptedException, TimeoutException {
        LOG.info("Get terminals");
        CompletableFuture<List<Terminal>> response = client.getTerminals();
        var defaultTerminalId = loadProperties().getProperty("terminalId");
        response.thenAccept(it -> {
            this.exTerminalId = it.stream()
                    .filter(Terminal::getOnline)
                    .filter(t -> {
                        return defaultTerminalId == null || t.getTerminalId().equals(defaultTerminalId);
                    })
                    .findFirst()
                    .orElse(it.get(0))
                    .getTerminalId();
            LOG.info("Selected terminal: {}", exTerminalId);
        });

        return response.get(timeout, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws Exception {
        String amountStr = "1.00";
        boolean isShowTips = false;

        listTerminals();

        var orderId = terminalClient.sendPayment(amountStr, isShowTips).getOrderId();
        LOG.info("Completed order: {}", orderId);

        var status = terminalClient.getDetails(orderId).getStatus();
        LOG.info("Payment status: {}", status);

        if (status == PaymentStatus.PENDING) {
            terminalClient.cancelPaymentAsync(amountStr, orderId).thenAccept(cancel -> {
                LOG.info("Order is cancelled? {}", cancel == PaymentStatus.CANCELLED);
            });
        }
    }

    private static void listTerminals() {
        try {
            LOG.info("Terminals: {}",
                    terminalClient.getTerminals().stream()
                            .map(t -> String.format("%s (Online: %b)", t.getTerminalId(), t.getOnline()))
                            .collect(Collectors.joining(", "))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String dump(PayResponse msg) {
        return msg.getAllFields().entrySet().stream().map(it -> {
            var key = it.getKey().getName();
            var value = it.getValue().toString().replace('\n', ' ');
            return key + "=" + value;
        }).collect(Collectors.joining(", "));
    }

    public static class SendPaymentCommand implements PaymentCommand {
        private static final PaymentInput input = new PaymentInput();

        public SendPaymentCommand() {
        }

        @Override
        public void execute() {
            listTerminals();
            String orderId = null;
            try {
                orderId = terminalClient.sendPayment(String.format("%.2f", (float) input.getAmount()/100), input.isShowTips()).getOrderId();
                LOG.info("Completed order: {}", orderId);

                var status = terminalClient.getDetails(orderId).getStatus();
                LOG.info("Payment status: {}", status);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void gatherInput() {
            LineReader reader = LineReaderBuilder.builder().build();
            input.setAmount(Long.parseLong(reader.readLine("\nAmount (in minor units, only digits): ")));

            LineReader booleanReader = LineReaderBuilder.builder().completer(new StringsCompleter("true", "false")) .build();
            input.setShowTips(Boolean.parseBoolean(booleanReader.readLine("\n Do you want to enable Terminal to show Tips (true/false): ")));
        }
    }

    public static class GetPaymentDetailsCommand implements Command {
        @Override
        public void execute() {
            LineReader reader = LineReaderBuilder.builder().build();

            var orderId = reader.readLine("\nOrder ID: ");
            try {
                dump(terminalClient.getDetails(orderId));
            } catch (Exception e) {
                LOG.error("Unable to get payment details: ", e);
            }
        }
    }

    public static class GetTerminalsCommand implements Command {

        @Override
        public void execute() {
            listTerminals();
        }
    }
}
