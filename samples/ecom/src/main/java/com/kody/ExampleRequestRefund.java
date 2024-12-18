package com.kody;

import com.kodypay.grpc.ecom.v1.KodyEcomPaymentsServiceGrpc;
import com.kodypay.grpc.ecom.v1.RefundRequest;
import com.kodypay.grpc.ecom.v1.RefundResponse;
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
        //TODO: Replace this with the payment ID received during payment initiation
        String paymentId = "PAYMENT ID";

        refundEcomPayment(storeId, paymentId);
    }

    public static void refundEcomPayment(String storeId, String paymentId) {
        var paymentClient = createKodyEcomPaymentsClient();

        // Define refund request
        RefundRequest refundRequest = RefundRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount("1")
                .setPaymentId(paymentId)
                .build();
        System.out.println("Send refund request");

        RefundResponse refundResponse = paymentClient.refund(refundRequest).next();
        System.out.println("refundResponse: " + refundResponse);
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
