package com.kody;

import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import com.kodypay.grpc.pay.v1.PayResponse;
import com.kodypay.grpc.pay.v1.PaymentDetailsRequest;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.TimeUnit;

public class ExampleGetPaymentDetails {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your Order ID
        String orderId = "ORDER ID";

        getPaymentDetails(storeId, orderId);
    }

    private static void getPaymentDetails(String storeId, String orderId) {
        var paymentClient = createKodyTerminalPaymentsClient();
        PaymentDetailsRequest paymentDetailsRequest = PaymentDetailsRequest.newBuilder()
                .setStoreId(storeId)
                .setOrderId(orderId)
                .build();

        System.out.println("Fetching payment details...");
        PayResponse payResponse = paymentClient.paymentDetails(paymentDetailsRequest);
        System.out.println("Payment details response: " + payResponse);
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
}