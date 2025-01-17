package com.kody;

import com.kodypay.grpc.ecom.v1.KodyEcomPaymentsServiceGrpc;
import com.kodypay.grpc.ecom.v1.PaymentInitiationRequest;
import com.kodypay.grpc.ecom.v1.PaymentInitiationResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ExampleNewPayment {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";

        // Example 1: Initiate a payment with default flags
        initiateEcomPayment(storeId);

        // Example 2: Initiate a payment with CaptureOptions
        initiateEcomPaymentWithCaptureOptions(storeId);
    }

    public static void initiateEcomPayment(String storeId) {
        var paymentClient = createKodyEcomPaymentsClient();

        // Generate unique order ID and payment reference
        String orderId = "order_" + UUID.randomUUID();
        String paymentReference = "pay_" + UUID.randomUUID();

        // Set payment details
        String currencyCode = "HKD"; // Replace with your store operating currency: ISO 4217
        long amountMinorUnits = 1000; // Replace with the payment amount in minor units
        String returnUrl = "https://display-parameters.com/"; // Replace with your return URL

        PaymentInitiationRequest paymentInitiationRequest = PaymentInitiationRequest.newBuilder()
                .setStoreId(storeId)
                .setPaymentReference(paymentReference)
                .setAmountMinorUnits(amountMinorUnits)
                .setCurrency(currencyCode)
                .setOrderId(orderId)
                .setReturnUrl(returnUrl)
                .build();

        PaymentInitiationResponse paymentInitiationResponse = paymentClient.initiatePayment(paymentInitiationRequest);

        System.out.println("paymentInitiationResponse.paymentUrl: " + paymentInitiationResponse.getResponse().getPaymentUrl());
        System.out.println("paymentInitiationResponse.paymentId: " + paymentInitiationResponse.getResponse().getPaymentId());
    }

    public static void initiateEcomPaymentWithCaptureOptions(String storeId) {
        var paymentClient = createKodyEcomPaymentsClient();

        // Generate unique order ID and payment reference
        String orderId = "order_" + UUID.randomUUID();
        String paymentReference = "pay_" + UUID.randomUUID();

        // Set payment details
        String currencyCode = "HKD"; // Replace with your store operating currency: ISO 4217
        long amountMinorUnits = 1000; // Replace with the payment amount in minor units
        String returnUrl = "https://display-parameters.com/"; // Replace with your return URL

        // Configure CaptureOptions
        PaymentInitiationRequest.CaptureOptions captureOptions = PaymentInitiationRequest.CaptureOptions.newBuilder()
                .setCaptureSettings(PaymentInitiationRequest.CaptureOptions.CaptureSettings.newBuilder()
                        .setDelayedCapture(true) // Enable delayed capture
                        .setAutoCaptureStoreCloseTime(false) // Disable auto-capture at store close time
                        .build())
                .setReleaseSettings(PaymentInitiationRequest.CaptureOptions.ReleaseSettings.newBuilder()
                        .setDelayedRelease(true) // Enable delayed release
                        .setAutoReleaseIntervalMins(5000) // Set auto-release interval in minutes
                        .setAutoReleaseStoreCloseTime(true) // Enable auto-release at store close time
                        .build())
                .build();

        PaymentInitiationRequest paymentInitiationRequest = PaymentInitiationRequest.newBuilder()
                .setStoreId(storeId)
                .setPaymentReference(paymentReference)
                .setAmountMinorUnits(amountMinorUnits)
                .setCurrency(currencyCode)
                .setOrderId(orderId)
                .setReturnUrl(returnUrl)
                .setCaptureOptions(captureOptions) // Add CaptureOptions to the request
                .build();

        System.out.println("Sending online payment with CaptureOptions...");

        PaymentInitiationResponse paymentInitiationResponse = paymentClient.initiatePayment(paymentInitiationRequest);
        System.out.println("paymentInitiationResponse.paymentUrl: " + paymentInitiationResponse.getResponse().getPaymentUrl());
        System.out.println("paymentInitiationResponse.paymentId: " + paymentInitiationResponse.getResponse().getPaymentId());
    }

    private static KodyEcomPaymentsServiceGrpc.KodyEcomPaymentsServiceBlockingStub createKodyEcomPaymentsClient() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), API_KEY);
        return KodyEcomPaymentsServiceGrpc.newBlockingStub(ManagedChannelBuilder
                .forAddress(HOSTNAME, 443)
                .idleTimeout(3, TimeUnit.MINUTES)
                .keepAliveTimeout(3, TimeUnit.MINUTES)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build());
    }
}
