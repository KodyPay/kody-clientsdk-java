package com.kody;

import com.kodypay.grpc.common.CardTokenStatus;
import com.kodypay.grpc.pay.v1.CreateCardTokenRequest;
import com.kodypay.grpc.pay.v1.GetCardTokenRequest;
import com.kodypay.grpc.pay.v1.KodyTerminalTokenServiceGrpc;
import com.kodypay.grpc.pay.v1.TokenDetailsResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExampleGetCardToken {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your Token ID returned from the createCardToken
        String tokenId = "TOKEN ID";

        getCardToken(storeId, tokenId);
    }

    // Example of get card token by token ID
    private static void getCardToken(String storeId, String tokenId) {
        var tokenClient = createKodyTerminalTokenClient();

        GetCardTokenRequest tokenRequest = GetCardTokenRequest.newBuilder()
                .setStoreId(storeId)
                .setTokenId(tokenId)
                .build();

        TokenDetailsResponse tokenDetailsResponse = tokenClient.getCardToken(tokenRequest);
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

        if (response.getStatus() == CardTokenStatus.READY) {
            System.out.println("Payment Token: " + response.getPaymentToken());
            System.out.println("Card Info: " +  response.getCardInfo());
        } else if (response.getStatus() == CardTokenStatus.PENDING) {
            System.out.println("Token pending: " + response.getTokenId());
        } else if (response.getStatus() == CardTokenStatus.FAILED) {
            System.out.println("Token failed: " + response.getTokenId());
        }
    }
}
