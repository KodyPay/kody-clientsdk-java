package terminal;

import com.kodypay.grpc.pay.v1.PayResponse;
import com.kodypay.grpc.pay.v1.PaymentStatus;
import com.kodypay.grpc.pay.v1.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class TerminalBlockingJavaClient {
    private static final Logger LOG = LoggerFactory.getLogger(TerminalBlockingJavaClient.class.getName());

    private final TerminalAsyncJavaClient asyncClient;

    public TerminalBlockingJavaClient() {
        asyncClient = new TerminalAsyncJavaClient();
    }

    public PayResponse sendPaymentBlocking(String amountStr) throws ExecutionException, InterruptedException, TimeoutException {
        return asyncClient.sendPaymentAsync(amountStr).get(1, TimeUnit.MINUTES);
    }

    public PaymentStatus cancelPaymentBlocking(String amountStr, String orderId) throws ExecutionException, InterruptedException, TimeoutException {
        return asyncClient.cancelPaymentAsync(amountStr, orderId).get(1, TimeUnit.MINUTES);
    }

    public PayResponse getDetailsBlocking(String orderId) throws ExecutionException, InterruptedException, TimeoutException {
        return asyncClient.getDetailsAsync(orderId).get(1, TimeUnit.MINUTES);
    }

    public List<Terminal> getTerminalsBlocking() throws ExecutionException, InterruptedException, TimeoutException {
        return asyncClient.getTerminalsAsync().get(1, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws Exception {
        TerminalBlockingJavaClient example = new TerminalBlockingJavaClient();
        String amountStr = "1.00";

        var terminals = example.getTerminalsBlocking();
        LOG.info("Terminals: {}", terminals.stream()
                .map(t -> t.getTerminalId() + " = " + t.getOnline())
                .collect(Collectors.joining(", ")));

        var orderId = example.sendPaymentBlocking(amountStr).getOrderId();
        LOG.info("Completed order: {}", orderId);

        var status = example.getDetailsBlocking(orderId).getStatus();
        LOG.info("Payment status: {}", status);

        if (status == PaymentStatus.PENDING) {
            var cancelled = example.cancelPaymentBlocking(amountStr, orderId);
            LOG.info("Order is cancelled? {}", cancelled == PaymentStatus.CANCELLED);
        }

        System.exit(0);
    }
}
