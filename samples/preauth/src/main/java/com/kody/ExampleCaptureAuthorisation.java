package com.kody;

import com.kodypay.grpc.preauth.v1.KodyPreAuthTerminalServiceGrpc;
import com.kodypay.grpc.preauth.v1.CaptureAuthorisationRequest;
import com.kodypay.grpc.preauth.v1.CaptureAuthorisationResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExampleCaptureAuthorisation {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your Terminal ID, required for Batch Payments
        String terminalId = "TERMINAL ID";
        //TODO: Replace this with your PreAuth ID returned from the pre-authorisation
        String preAuthId = "PRE AUTH ID";
        //TODO: Replace with your store operating currency: ISO 4217
        String currencyCode = "HKD";
        //TODO: Replace with the auth amount in minor units, the remaining amount will be automatically released
        long captureAmountMinorUnits = 1000;
        //TODO: Replace these with your reference ID
        String captureReference = "REF-" + System.currentTimeMillis();
        //TODO: Replace these with your idempotency UUID
        String idempotencyUuid = UUID.randomUUID().toString();

        captureAuthorisation(storeId, terminalId, preAuthId, captureAmountMinorUnits, currencyCode, captureReference, idempotencyUuid);
    }

    // Example of a capture authorisation with idempotency and reference IDs
    private static void captureAuthorisation(String storeId, String terminalId, String preAuthId, long captureAmountMinorUnits, String currency, String captureReference, String idempotencyUuid) {
        var preAuthClient = createKodyPreAuthTerminalClient();

        CaptureAuthorisationRequest captureAuthRequest = CaptureAuthorisationRequest.newBuilder()
                .setStoreId(storeId)
                .setTerminalId(terminalId)
                .setPreAuthId(preAuthId)
                .setCaptureAmountMinorUnits(captureAmountMinorUnits)
                .setCurrency(currency)
                .setCaptureReference(captureReference)
                .setIdempotencyUuid(idempotencyUuid)
                .build();

        CaptureAuthorisationResponse captureAuthResponse = preAuthClient.captureAuthorisation(captureAuthRequest);
        processCaptureAuthResponse(captureAuthResponse);
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

    // Helper method to process capture authorisation response
    private static void processCaptureAuthResponse(CaptureAuthorisationResponse response) {
        // Capture is processed asynchronously, and the final status can be queried through the GetPreAuthorisation interface.
        System.out.println("Capture Ref: " + response.getCaptureReference() + ", Status: " + response.getStatus());

        switch (response.getStatus()) {
            case CAPTURED:
                System.out.println("Capture success: " + response.getCaptureReference());
                System.out.println("Capture Id: " + response.getCaptureId());
                System.out.println("PspReference: " + response.getPspReference());
                System.out.println("CapturedAt: " + response.getCapturedAt());
                break;
            case PENDING_CAPTURE:
                System.out.println("Capture pending: " + response.getCaptureReference());
                System.out.println("Capture Id: " + response.getCaptureId());
                break;
            case CAPTURE_FAILED:
                System.out.println("Capture failed: " + response.getCaptureReference());
                break;
            case AUTH_STATUS_UNSPECIFIED, UNRECOGNIZED:
                System.out.println("Capture status unknown: " + response.getCaptureReference());
                break;
        }
    }
}
