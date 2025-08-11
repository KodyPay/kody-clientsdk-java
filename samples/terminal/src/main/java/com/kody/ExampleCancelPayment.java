package com.kody;

import com.kodypay.grpc.pay.v1.CancelRequest;
import com.kodypay.grpc.pay.v1.CancelResponse;
import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.math.BigDecimal;
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
        //TODO: Replace this with your Payment ID
        String paymentId = "PAYMENT ID";
        //TODO: Replace this with your amount
        BigDecimal amount = new BigDecimal("10.00");

        cancelPayment(storeId, terminalId, paymentId, amount);
    }

    private static void cancelPayment(String storeId, String terminalId, String paymentId, BigDecimal amount) {
        var paymentClient = createKodyTerminalPaymentsClient();
        CancelRequest cancelRequest = CancelRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount.toString())
                .setTerminalId(terminalId)
                .setPaymentId(paymentId)
                .build();

        CancelResponse cancelResponse = paymentClient.cancel(cancelRequest);
        System.out.println("cancelResponse.status: " + cancelResponse.getStatus());
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