package com.kody;

import com.kodypay.grpc.pay.v1.CloseBatchRequest;
import com.kodypay.grpc.pay.v1.CloseBatchResponse;
import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExampleCloseBatch {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace these with your Terminal IDs (optional)
        List<String> terminalIds = List.of("TERMINAL ID 1", "TERMINAL ID 2");

        closeBatchForAllTerminals(storeId);
        closeBatchForSpecificTerminals(storeId, terminalIds);
    }

    // Example of closing batches for all terminals in the store
    private static void closeBatchForAllTerminals(String storeId) {
        var paymentClient = createKodyTerminalPaymentsClient();

        CloseBatchRequest closeBatchRequest = CloseBatchRequest.newBuilder()
                .setStoreId(storeId)
                .build();

        CloseBatchResponse closeBatchResponse = paymentClient.closeBatch(closeBatchRequest);

        System.out.println("Overall Status: " + closeBatchResponse.getStatus());
        System.out.println("Message: " + closeBatchResponse.getMessage());

        for (var result : closeBatchResponse.getResultsList()) {
            System.out.println("Terminal ID: " + result.getTerminalId());
            System.out.println("Batch ID: " + result.getBatchId());
            System.out.println("Success: " + result.getSuccess());
            System.out.println("Message: " + result.getMessage());
        }
    }

    // Example of closing batch for specific terminal(s)
    private static void closeBatchForSpecificTerminals(String storeId, List<String> terminalIds) {
        var paymentClient = createKodyTerminalPaymentsClient();

        CloseBatchRequest.Builder requestBuilder = CloseBatchRequest.newBuilder()
                .setStoreId(storeId);

        // Add all terminal IDs to the request
        for (String terminalId : terminalIds) {
            requestBuilder.addTerminalIds(terminalId);
        }

        CloseBatchRequest closeBatchRequest = requestBuilder.build();

        CloseBatchResponse closeBatchResponse = paymentClient.closeBatch(closeBatchRequest);

        System.out.println("Overall Status: " + closeBatchResponse.getStatus());
        System.out.println("Message: " + closeBatchResponse.getMessage());

        for (var result : closeBatchResponse.getResultsList()) {
            System.out.println("Terminal ID: " + result.getTerminalId());
            System.out.println("Batch ID: " + result.getBatchId());
            System.out.println("Success: " + result.getSuccess());
            System.out.println("Message: " + result.getMessage());
        }
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

