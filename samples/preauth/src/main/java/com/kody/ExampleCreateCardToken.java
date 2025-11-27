package com.kody;

import com.kodypay.grpc.common.CardTokenStatus;
import com.kodypay.grpc.pay.v1.*;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExampleCreateCardToken {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your Terminal ID
        String terminalId = "TERMINAL ID";
        //TODO: Replace this with your Payer Reference
        String payerReference = "Payer-" + System.currentTimeMillis();

        createCardToken(storeId, terminalId, payerReference);

        //TODO: Optional - Replace these with your reference IDs or leave null
        String tokenReference = "REF-" + System.currentTimeMillis();
        String idempotencyUuid = UUID.randomUUID().toString();
        createCardTokenIdempotently(storeId, terminalId, payerReference, tokenReference, idempotencyUuid);
    }

    // Example of a card tokenisation
    private static void createCardToken(String storeId, String terminalId, String payerReference) {
        var tokenClient = createKodyTerminalTokenClient();

        CreateCardTokenRequest tokenRequest = CreateCardTokenRequest.newBuilder()
                .setStoreId(storeId)
                .setTerminalId(terminalId)
                .setPayerReference(payerReference)
                .build();

        Iterator<TokenDetailsResponse> responseIterator = tokenClient.createCardToken(tokenRequest);
        while (responseIterator.hasNext()) {
            TokenDetailsResponse tokenDetailsResponse = responseIterator.next();
            processTokenisationResponse(tokenDetailsResponse);
        }
    }

    // Example of a card tokenisation with idempotency and reference IDs
    private static void createCardTokenIdempotently(String storeId, String terminalId, String payerReference, String idempotencyUuid, String tokenReference) {
        var tokenClient = createKodyTerminalTokenClient();

        CreateCardTokenRequest tokenRequest = CreateCardTokenRequest.newBuilder()
                .setStoreId(storeId)
                .setTerminalId(terminalId)
                .setPayerReference(payerReference)
                .setIdempotencyUuid(idempotencyUuid)
                .setTokenReference(tokenReference)
                .build();

        TokenDetailsResponse tokenDetailsResponse = tokenClient.createCardToken(tokenRequest).next();
        processTokenisationResponse(tokenDetailsResponse);
    }

    private static KodyTerminalTokenServiceGrpc.KodyTerminalTokenServiceBlockingStub createKodyTerminalTokenClient() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), API_KEY);

        return KodyTerminalTokenServiceGrpc.newBlockingStub(ManagedChannelBuilder
                .forAddress(HOSTNAME, 443)
                .idleTimeout(3, TimeUnit.MINUTES)
                .keepAliveTimeout(3, TimeUnit.MINUTES)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build());
    }

    // Helper method to process tokenisation response
    private static void processTokenisationResponse(TokenDetailsResponse response) {
        System.out.println("Token ID: " + response.getTokenId() + ", Token Status: " + response.getStatus());

        switch (response.getStatus()) {
            case READY:
                System.out.println("Token success: " + response.getTokenId());
                System.out.println("Payment Token: " + response.getPaymentToken());
                System.out.println("Card Info: " +  response.getCardInfo());
                break;
            case PENDING:
                System.out.println("Token pending: " + response.getTokenId());
                break;
            case FAILED:
                System.out.println("Token failed: " + response.getTokenId());
                break;
            case UNKNOWN_STATUS, UNRECOGNIZED:
                System.out.println("TopUp status unknown: " + response.getTokenId());
                break;
        }
    }
}
