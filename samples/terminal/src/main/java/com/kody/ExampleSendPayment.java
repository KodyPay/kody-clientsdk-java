package com.kody;

import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import com.kodypay.grpc.pay.v1.PayRequest;
import com.kodypay.grpc.pay.v1.PayResponse;
import com.kodypay.grpc.pay.v1.PaymentMethod;
import com.kodypay.grpc.pay.v1.PaymentMethodType;
import com.kodypay.grpc.pay.v1.PaymentStatus;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

public class ExampleSendPayment {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your Terminal ID
        String terminalId = "TERMINAL ID";
        //TODO: Replace this with your amount
        BigDecimal amount = new BigDecimal("10.00");

        sendRestrictedCardPayment(storeId, terminalId, amount);
        sendIdempotentPayment(storeId, terminalId, amount);
        sendQRPaymentDisplayOn(storeId, terminalId, amount);
        sendQRPaymentDisplayOff(storeId, terminalId, amount);
    }

    // Example of a card payment with specific accepted card types
    private static void sendRestrictedCardPayment(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setShowTips(true)
                // TODO: Coming soon - Card type restriction
                // .addAcceptsOnly(PayRequest.PaymentMethods.VISA)
                // .addAcceptsOnly(PayRequest.PaymentMethods.MASTERCARD)
                .build();

        PayResponse payResponse = paymentClient.pay(payRequest).next();
        processPaymentResponse(payResponse);
    }

    // Example of a payment with idempotency and reference IDs
    private static void sendIdempotentPayment(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setIdempotencyUuid(UUID.randomUUID().toString())
                .setPaymentReference("REF-" + System.currentTimeMillis())
                .setOrderId("ORDER-" + System.currentTimeMillis())
                .build();

        PayResponse payResponse = paymentClient.pay(payRequest).next();
        processPaymentResponse(payResponse);
    }

    // Example of an Alipay or Wechat payment with QR display on
    private static void sendQRPaymentDisplayOn(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.CARD)
                .setActivateQrCodeScanner(true)  // Display QR for customer to scan
                .build();

        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setPaymentMethod(paymentMethod)
                .build();

        PayResponse payResponse = paymentClient.pay(payRequest).next();
        processPaymentResponse(payResponse);
    }

    // Example of an Alipay or Wechat payment with QR display off
    private static void sendQRPaymentDisplayOff(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.CARD)
                .setActivateQrCodeScanner(false)  // Do not display QR for customer to scan
                .build();

        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setShowTips(true)
                .setPaymentMethod(paymentMethod)
                .build();

        PayResponse payResponse = paymentClient.pay(payRequest).next();
        processPaymentResponse(payResponse);
    }

    private static KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub createKodyTerminalPaymentsClient() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), API_KEY);

        return KodyPayTerminalServiceGrpc.newBlockingStub(ManagedChannelBuilder
                .forAddress(HOSTNAME, 443)
                .idleTimeout(3, TimeUnit.MINUTES)
                .keepAliveTimeout(3, TimeUnit.MINUTES)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build());
    }

    // Helper method to process payment response
    private static void processPaymentResponse(PayResponse response) {
        System.out.println("Payment Status: " + response.getStatus());

        if (response.getStatus() == PaymentStatus.PENDING) {
            System.out.println("Order ID: " + response.getOrderId());
        } else if (response.getStatus() == PaymentStatus.FAILED) {
            System.out.println("Payment failed: " + response.getFailureReason());
        }
    }
}
