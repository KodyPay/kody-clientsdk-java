package com.kody;

import com.kodypay.grpc.preauth.v1.AuthStatus;
import com.kodypay.grpc.preauth.v1.GetPreAuthorisationRequest;
import com.kodypay.grpc.preauth.v1.GetPreAuthorisationResponse;
import com.kodypay.grpc.preauth.v1.KodyPreAuthTerminalServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.TimeUnit;

public class ExampleGetPreAuthorisation {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your PreAuth ID returned from the pre-authorisation
        String preAuthId = "PRE AUTH ID";

        getPreAuthorisation(storeId, preAuthId);
    }

    // Example of a get pre-authorisation with idempotency and reference IDs
    private static void getPreAuthorisation(String storeId, String preAuthId) {
        var preAuthClient = createKodyPreAuthTerminalClient();

        GetPreAuthorisationRequest preAuthRequest = GetPreAuthorisationRequest.newBuilder()
                .setStoreId(storeId)
                .setPreAuthId(preAuthId)
                .build();

        GetPreAuthorisationResponse getPreAuthResponse = preAuthClient.getPreAuthorisation(preAuthRequest);
        processGetPreAuthResponse(getPreAuthResponse);
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

    // Helper method to process get pre-authorisation response
    private static void processGetPreAuthResponse(GetPreAuthorisationResponse response) {
        System.out.println("PreAuth ID: " + response.getPreAuthId() + ", PreAuth Status: " + response.getStatus());

        switch (response.getStatus()) {
            case AUTHORISED:
                System.out.println("Pre-Auth success: " + response.getPreAuthId());
                System.out.println("PspReference: " + response.getPspReference());
                break;
            case PENDING_AUTHORISATION:
                System.out.println("Pre-Auth pending: " + response.getPreAuthId());
                break;
            case FAILED, EXPIRED, CANCELLED, DECLINED:
                System.out.println("Pre-Auth failed: " + response.getPreAuthId());
                break;
            case UNRECOGNIZED:
                System.out.println("Pre-Auth status unknown: " + response.getPreAuthId());
                break;
        }
        for (var adjustment : response.getAdjustmentsList()) {
            System.out.println("Adjustment ID: " + adjustment.getAdjustmentId() + ", Adjustment Amount: " + adjustment.getAmountMinorUnits() + ", Adjustment Status: " + adjustment.getStatus());
        }
        for (var capture : response.getCapturesList()) {
            System.out.println("Capture ID: " + capture.getCaptureId() + ", Capture Amount: " + capture.getAmountMinorUnits() + ", Capture Status: " + capture.getStatus());
        }
    }
}
