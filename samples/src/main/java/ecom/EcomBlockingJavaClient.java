package ecom;

import com.kodypay.grpc.ecom.v1.GetPaymentsResponse.Response.PaymentDetails;
import com.kodypay.grpc.ecom.v1.PaymentInitiationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EcomBlockingJavaClient {
    private static final Logger LOG = LoggerFactory.getLogger(EcomBlockingJavaClient.class.getName());
    private final EcomAsyncJavaClient asyncClient;

    public EcomBlockingJavaClient() {
        asyncClient = new EcomAsyncJavaClient();
    }

    public PaymentInitiationResponse sendOnlinePaymentBlocking(long amountStr) throws ExecutionException, InterruptedException, TimeoutException {
        return asyncClient.sendPaymentAsync(amountStr).get(1, TimeUnit.MINUTES);
    }

    public void requestOnlineRefund(String paymentId, long amountStr) {
        asyncClient.requestRefundAsync(paymentId, amountStr).thenAccept(it -> LOG.info("Requested refund response: {}", it));
    }

    public List<PaymentDetails> getPaymentsBlocking() throws ExecutionException, InterruptedException, TimeoutException {
        return asyncClient.getPayments().get(1, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        EcomBlockingJavaClient ecomBlockingJavaClient = new EcomBlockingJavaClient();

        long amountInPence = 100;
        Optional<PaymentDetails> payment;
        PaymentInitiationResponse paymentResponse = ecomBlockingJavaClient.sendOnlinePaymentBlocking(amountInPence);

        // Wait for payment to be complete before refunding it
        do {
            Thread.sleep(5000);
            payment = ecomBlockingJavaClient
                    .getPaymentsBlocking()
                    .stream()
                    .filter(c -> c.getPaymentId().equals(paymentResponse.getResponse().getPaymentId()))
                    .findFirst();
        } while (payment.isEmpty() || payment.stream().allMatch(e -> e.getStatus() == PaymentDetails.PaymentStatus.PENDING));

        ecomBlockingJavaClient.requestOnlineRefund(paymentResponse.getResponse().getPaymentUrl(), amountInPence);
    }
}
