package ecom;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EcomBlockingJavaClient {
    private final EcomAsyncJavaClient asyncClient;

    public EcomBlockingJavaClient() {
        asyncClient = new EcomAsyncJavaClient();
    }

    public void sendOnlinePaymentBlocking(long amountStr) throws ExecutionException, InterruptedException, TimeoutException {
        asyncClient.sendPaymentAsync(amountStr).get(1, TimeUnit.MINUTES);
    }

    public void getPaymentsBlocking() throws ExecutionException, InterruptedException, TimeoutException {
        asyncClient.getPayments().get(1, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        EcomBlockingJavaClient ecomBlockingJavaClient = new EcomBlockingJavaClient();

        long amountInPence = 100;
        ecomBlockingJavaClient.getPaymentsBlocking();
        ecomBlockingJavaClient.sendOnlinePaymentBlocking(amountInPence);
    }
}
