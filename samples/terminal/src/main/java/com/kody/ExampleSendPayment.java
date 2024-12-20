package com.kody;

import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import com.kodypay.grpc.pay.v1.PayRequest;
import com.kodypay.grpc.pay.v1.PayResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.TimeUnit;

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
        String amount = "2000";

        sendPayment(storeId, terminalId, amount);
    }

    private static void sendPayment(String storeId, String terminalId, String amount) {
        var paymentClient = createKodyTerminalPaymentsClient();
        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(amount)
                .setTerminalId(terminalId)
                .setShowTips(true)
                .build();

        System.out.println("Sending payment to terminal...");
        PayResponse payResponse = paymentClient.pay(payRequest).next();
        System.out.println("Payment response: " + payResponse);
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
