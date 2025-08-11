package com.kody;

import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import com.kodypay.grpc.pay.v1.RefundRequest;
import com.kodypay.grpc.pay.v1.RefundResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.math.BigDecimal;
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
        BigDecimal amount = new BigDecimal("10.00");

        requestRefund(storeId, orderId, amount);
    }

    private static void requestRefund(String storeId, String orderId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();
        RefundRequest refundRequest = RefundRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setExtOrderId(orderId)
                .build();

        RefundResponse refundResponse = paymentClient.refund(refundRequest).next();
        System.out.println("Refund response: " + refundResponse);
        System.out.println("refundResponse.status: " + refundResponse.getStatus());
        System.out.println("refundResponse.orderId: " + refundResponse.getOrderId());
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