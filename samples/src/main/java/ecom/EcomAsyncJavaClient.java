package ecom;

import com.kodypay.grpc.ecom.v1.*;
import com.kodypay.grpc.ecom.v1.GetPaymentsResponse.Response.PaymentDetails;
import common.CurrencyEnum;
import common.PaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Currency;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static common.Utils.*;

public class EcomAsyncJavaClient {
    private static final Logger LOG = LoggerFactory.getLogger(EcomAsyncJavaClient.class.getName());

    private final PaymentClient client;

    public EcomAsyncJavaClient() {
        Properties properties = loadProperties();
        var address = URI.create(properties.getProperty("address", "http://localhost"));
        var apiKey = properties.getProperty("apiKey");
        if (apiKey == null)
            throw new IllegalArgumentException("Invalid config, expected apiKey");

        var storeId = UUID.fromString(properties.getProperty("storeId"));

        client = new PaymentClient(address, storeId, apiKey);
    }

    public CompletableFuture<PaymentInitiationResponse> sendPaymentAsync(long amount) {
        LOG.info("Initiating payment for amount: {}", amount);
        String paymentReference = generatePaymentReference();
        String orderId = generateOrderId();
        String currency = CurrencyEnum.HKD.name();

        return client.sendOnlinePayment(paymentReference, amount, currency, orderId, "returnUrl")
                .thenApply(res -> {
                    LOG.info("Sent Online Payment - Payment Id: {}, Payment URL: {}", res.getResponse().getPaymentId(), res.getResponse().getPaymentUrl());
                    return res;
                });
    }

    public CompletableFuture<List<PaymentDetails>> getPayments() {
        LOG.info("Get payments");

        return client.getPayments()
                .thenApply(res -> {
                    LOG.info("Payments: {}", res.stream()
                            .map(p -> "PaymentId: " + p.getPaymentId() + ", OrderId: " + p.getOrderId())
                            .collect(Collectors.joining(", ")));
                    return res;
                });
    }

    public static void main(String[] args) {
        EcomAsyncJavaClient ecomAsyncJavaClient = new EcomAsyncJavaClient();

        long amountInPence = 100;

        ecomAsyncJavaClient.getPayments();
        ecomAsyncJavaClient.sendPaymentAsync(amountInPence);
    }
}
