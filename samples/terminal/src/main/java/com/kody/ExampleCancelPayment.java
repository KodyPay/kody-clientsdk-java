package com.kody;

import com.kodypay.grpc.pay.v1.CancelRequest;
import com.kodypay.grpc.pay.v1.CancelResponse;
import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.TimeUnit;

public class ExampleCancelPayment {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your Terminal ID
        String terminalId = "TERMINAL ID";
        //TODO: Replace this with your Order ID
        String orderId = "ORDER ID";
        //TODO: Replace this with your amount
        String amount = "1000";

        cancelPayment(storeId, terminalId, orderId, amount);
    }

    private static void cancelPayment(String storeId, String terminalId, String orderId, String amount) {
        var paymentClient = createKodyTerminalPaymentsClient();
        CancelRequest cancelRequest = CancelRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount)
                .setTerminalId(terminalId)
                .setOrderId(orderId)
                .build();

        System.out.println("Canceling payment...");
        CancelResponse cancelResponse = paymentClient.cancel(cancelRequest);
        System.out.println("Cancel response: " + cancelResponse);
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