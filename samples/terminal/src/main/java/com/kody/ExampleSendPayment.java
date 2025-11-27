package com.kody;

import com.kodypay.grpc.pay.v1.*;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.math.BigDecimal;
import java.util.Iterator;
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
        sendQRPaymentQRScannerOn(storeId, terminalId, amount);
        sendQRPaymentQRScannerOff(storeId, terminalId, amount);

        //TODO: Optional - Replace these with your reference IDs or leave null
        String paymentReference = "REF-" + System.currentTimeMillis();
        String orderId = "ORDER-" + System.currentTimeMillis();
        String idempotencyUuid = UUID.randomUUID().toString();
        sendIdempotentPayment(storeId, terminalId, amount, paymentReference, orderId, idempotencyUuid);
    }

    // Example of a card payment with specific accepted card types
    private static void sendRestrictedCardPayment(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setShowTips(true)
                .addAcceptsOnly(PayRequest.PaymentMethods.VISA)
                .addAcceptsOnly(PayRequest.PaymentMethods.MASTERCARD)
                .build();

        Iterator<PayResponse> responseIterator = paymentClient.pay(payRequest);
        while (responseIterator.hasNext()) {
            PayResponse payResponse = responseIterator.next();
            processPaymentResponse(payResponse);
        }
    }

    // Example of an Alipay or Wechat Pay payment with QR scanner on
    private static void sendQRPaymentQRScannerOn(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.E_WALLET)
                .setActivateQrCodeScanner(true)  // Activate QR code scanner
                .build();

        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setPaymentMethod(paymentMethod)
                .build();

        Iterator<PayResponse> responseIterator = paymentClient.pay(payRequest);
        while (responseIterator.hasNext()) {
            PayResponse payResponse = responseIterator.next();
            processPaymentResponse(payResponse);
        }
    }

    // Example of an Alipay or Wechat Pay payment with QR scanner off
    private static void sendQRPaymentQRScannerOff(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.E_WALLET)
                .setActivateQrCodeScanner(false)  // De-activate QR code scanner
                .build();

        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setPaymentMethod(paymentMethod)
                .build();

        Iterator<PayResponse> responseIterator = paymentClient.pay(payRequest);
        while (responseIterator.hasNext()) {
            PayResponse payResponse = responseIterator.next();
            processPaymentResponse(payResponse);
        }
    }

    // Example of a payment with idempotency and reference IDs
    private static void sendIdempotentPayment(String storeId, String terminalId, BigDecimal amount,
                                              String paymentReference, String orderId, String idempotencyUuid) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setIdempotencyUuid(idempotencyUuid)
                .setPaymentReference(paymentReference)
                .setOrderId(orderId)
                .build();

        Iterator<PayResponse> responseIterator = paymentClient.pay(payRequest);
        while (responseIterator.hasNext()) {
            PayResponse payResponse = responseIterator.next();
            processPaymentResponse(payResponse);
        }
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
