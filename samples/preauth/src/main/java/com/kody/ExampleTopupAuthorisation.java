package com.kody;

import com.kodypay.grpc.preauth.v1.*;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExampleTopupAuthorisation {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your PreAuth ID returned from the pre-authorisation
        String preAuthId = "PRE AUTH ID";
        //TODO: Replace with your store operating currency: ISO 4217
        String currencyCode = "HKD";
        //TODO: Replace with the topup amount in minor units, topup amount is the auth amount after topup
        long topUpAmountMinorUnits = 1000;
        //TODO: Replace these with your reference ID
        String topUpReference = "REF-" + System.currentTimeMillis();
        //TODO: Replace these with your idempotency UUID
        String idempotencyUuid = UUID.randomUUID().toString();

        topUpAuthorisation(storeId, preAuthId, topUpAmountMinorUnits, currencyCode, topUpReference, idempotencyUuid);
    }

    // Example of a topup-authorisation with idempotency and reference IDs
    private static void topUpAuthorisation(String storeId, String preAuthId, long topUpAmountMinorUnits, String currency, String topUpReference, String idempotencyUuid) {
        var preAuthClient = createKodyPreAuthTerminalClient();

        TopUpAuthorisationRequest topUpAuthRequest = TopUpAuthorisationRequest.newBuilder()
                .setStoreId(storeId)
                .setPreAuthId(preAuthId)
                .setTopUpAmountMinorUnits(topUpAmountMinorUnits)
                .setCurrency(currency)
                .setTopUpReference(topUpReference)
                .setIdempotencyUuid(idempotencyUuid)
                .build();

        Iterator<TopUpAuthorisationResponse> responseIterator = preAuthClient.topUpAuthorisation(topUpAuthRequest);
        while (responseIterator.hasNext()) {
            TopUpAuthorisationResponse topUpAuthResponse = responseIterator.next();
            processTopUpAuthResponse(topUpAuthResponse);
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

    // Helper method to process topup-authorisation response
    private static void processTopUpAuthResponse(TopUpAuthorisationResponse response) {
        System.out.println("TopUp ID: " + response.getTopupId() + ", TopUp Status: " + response.getStatus());

        switch (response.getStatus()) {
            case AUTHORISED:
                System.out.println("TopUp success: " + response.getTopupId());
                System.out.println("PspReference: " + response.getPspReference());
                break;
            case PENDING_TOP_UP:
                System.out.println("TopUp pending: " + response.getTopupId());
                break;
            case TOP_UP_FAILED:
                System.out.println("TopUp failed: " + response.getTopupId());
                break;
            case AUTH_STATUS_UNSPECIFIED, UNRECOGNIZED:
                System.out.println("TopUp status unknown: " + response.getTopupId());
                break;
        }
    }
}
