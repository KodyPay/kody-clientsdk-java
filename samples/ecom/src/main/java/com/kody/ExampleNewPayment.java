package com.kody;

import com.kodypay.grpc.ecom.v1.KodyEcomPaymentsServiceGrpc;
import com.kodypay.grpc.ecom.v1.PaymentInitiationRequest;
import com.kodypay.grpc.ecom.v1.PaymentInitiationResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ExampleNewPayment {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";

        //TODO: Replace with your internal order ID
        String orderId = "order_" + UUID.randomUUID();
        //TODO: Replace with your internal payment reference
        String paymentReference = "pay_" + UUID.randomUUID();

        //TODO: Replace this with your store operating currency: ISO 4217
        String currencyCode = "HKD";
        //TODO: Set the payment amount in minor units
        long amountMinorUnits = 1000;
        //TODO: Set the URL where the payment page will redirect to after the user completes the payment
        String returnUrl = "https://display-parameters.com/";

        initiateEcomPayment(storeId, paymentReference, orderId, currencyCode, amountMinorUnits, returnUrl);
    }

    public static void initiateEcomPayment(String storeId, String orderId, String paymentReference,
                                           String currencyCode, long amountMinorUnits, String returnUrl) {
        var paymentClient = createKodyEcomPaymentsClient();

        PaymentInitiationRequest paymentInitiationRequest = PaymentInitiationRequest.newBuilder()
                .setStoreId(storeId)
                .setPaymentReference(paymentReference)
                .setAmount(amountMinorUnits)
                .setCurrency(currencyCode)
                .setOrderId(orderId)
                .setReturnUrl(returnUrl)
                .build();
        System.out.println("Send online payment");

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
