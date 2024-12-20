package com.kody;

import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import com.kodypay.grpc.pay.v1.RefundRequest;
import com.kodypay.grpc.pay.v1.RefundResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.TimeUnit;

public class ExampleRequestRefund {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your Order ID
        String orderId = "ORDER ID";
        //TODO: Replace this with your amount
        String amount = "1000";

        requestRefund(storeId, orderId, amount);
    }

    private static void requestRefund(String storeId, String orderId, String amount) {
        var paymentClient = createKodyTerminalPaymentsClient();
        RefundRequest refundRequest = RefundRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount)
                .setOrderId(orderId)
                .build();

        System.out.println("Requesting refund...");
        RefundResponse refundResponse = paymentClient.refund(refundRequest).next();
        System.out.println("Refund response: " + refundResponse);
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