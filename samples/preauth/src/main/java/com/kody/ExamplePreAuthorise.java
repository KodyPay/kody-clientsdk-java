package com.kody;

import com.kodypay.grpc.preauth.v1.AuthStatus;
import com.kodypay.grpc.preauth.v1.KodyPreAuthTerminalServiceGrpc;
import com.kodypay.grpc.preauth.v1.PreAuthorisationRequest;
import com.kodypay.grpc.preauth.v1.PreAuthorisationResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExamplePreAuthorise {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your Terminal ID
        String terminalId = "TERMINAL ID";
        //TODO: Replace with your store operating currency: ISO 4217
        String currencyCode = "HKD";
        //TODO: Replace with the auth amount in minor units
        long amountMinorUnits = 1000;
        //TODO: Replace these with your reference ID
        String preAuthReference = "REF-" + System.currentTimeMillis();
        //TODO: Replace these with your idempotency UUID
        String idempotencyUuid = UUID.randomUUID().toString();

        preAuthorise(storeId, terminalId, amountMinorUnits, currencyCode, preAuthReference, idempotencyUuid);
    }

    // Example of a pre-authorisation with idempotency and reference IDs
    private static void preAuthorise(String storeId, String terminalId, long amountMinorUnits, String currency, String preAuthReference, String idempotencyUuid) {
        var preAuthClient = createKodyPreAuthTerminalClient();

        PreAuthorisationRequest preAuthRequest = PreAuthorisationRequest.newBuilder()
                .setStoreId(storeId)
                .setTerminalId(terminalId)
                .setAmountMinorUnits(amountMinorUnits)
                .setCurrency(currency)
                .setPreAuthReference(preAuthReference)
                .setIdempotencyUuid(idempotencyUuid)
                .build();

        Iterator<PreAuthorisationResponse> responseIterator = preAuthClient.preAuthorise(preAuthRequest);
        while (responseIterator.hasNext()) {
            PreAuthorisationResponse preAuthResponse = responseIterator.next();
            processPreAuthResponse(preAuthResponse);
        }
    }

    private static KodyPreAuthTerminalServiceGrpc.KodyPreAuthTerminalServiceBlockingStub createKodyPreAuthTerminalClient() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), API_KEY);

        return KodyPreAuthTerminalServiceGrpc.newBlockingStub(ManagedChannelBuilder
                .forAddress(HOSTNAME, 443)
                .idleTimeout(3, TimeUnit.MINUTES)
                .keepAliveTimeout(3, TimeUnit.MINUTES)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build());
    }

    // Helper method to process pre-authorisation response
    private static void processPreAuthResponse(PreAuthorisationResponse response) {
        System.out.println("PreAuth ID: " + response.getPreAuthId() + ", PreAuth Status: " + response.getStatus());

        if (response.getStatus() == AuthStatus.AUTHORISED) {
            System.out.println("PspReference: " + response.getPspReference());
            System.out.println("Auth Code: " +  response.getAuthCode());
        } else if (response.getStatus() == AuthStatus.PENDING_AUTHORISATION) {
            System.out.println("Pre-Auth pending: " + response.getPreAuthId());
        } else if (response.getStatus() == AuthStatus.FAILED) {
            System.out.println("Pre-Auth failed: " + response.getPreAuthId());
        }
    }
}
