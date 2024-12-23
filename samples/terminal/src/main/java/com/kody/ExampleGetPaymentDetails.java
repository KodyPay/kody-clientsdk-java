package com.kody;

import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import com.kodypay.grpc.pay.v1.PayResponse;
import com.kodypay.grpc.pay.v1.PaymentDetailsRequest;
import com.kodypay.grpc.pay.v1.PaymentStatus;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.Date;
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

        PayResponse payResponse = paymentClient.paymentDetails(paymentDetailsRequest);

        System.out.println("Payment details response: " + payResponse);
        System.out.println("Payment status: " + payResponse.getStatus());
        System.out.println("Payment receipt json: " + payResponse.getReceiptJson());
        System.out.println("Payment order ID " + payResponse.getOrderId());
        System.out.println("Payment created timestamp: " + new Date(payResponse.getDateCreated().getSeconds() * 1000L));
        if (payResponse.getStatus() == PaymentStatus.SUCCESS) {
            System.out.println("Payment ext payment reference: " + payResponse.getExtPaymentRef());
            System.out.println("Payment paid timestamp: " + new Date(payResponse.getDatePaid().getSeconds() * 1000L));
        }
        System.out.println("Payment total amount: " + payResponse.getTotalAmount());
        System.out.println("Payment sale amount: " + payResponse.getSaleAmount());
        System.out.println("Payment tips amount: " + payResponse.getTipsAmount());
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