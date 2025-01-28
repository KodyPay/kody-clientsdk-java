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
        sendAlipayPaymentQRDisplayOn(storeId, terminalId, amount);
        sendAlipayPaymentQRDisplayOff(storeId, terminalId, amount);
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

    // Example of an Alipay payment with QR display on
    private static void sendAlipayPaymentQRDisplayOn(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.ALIPAY)
                .setActivateQrCodeScanner(true)  // Activate QR code scanner
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

    // Example of an Alipay payment with QR display off
    private static void sendAlipayPaymentQRDisplayOff(String storeId, String terminalId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();

        PaymentMethod paymentMethod = PaymentMethod.newBuilder()
                .setPaymentMethodType(PaymentMethodType.ALIPAY)
                .setActivateQrCodeScanner(false)  // Do not activate QR code scanner
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
