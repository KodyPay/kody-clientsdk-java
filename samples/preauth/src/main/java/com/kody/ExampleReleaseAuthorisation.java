package com.kody;

import com.kodypay.grpc.preauth.v1.AuthStatus;
import com.kodypay.grpc.preauth.v1.KodyPreAuthTerminalServiceGrpc;
import com.kodypay.grpc.preauth.v1.ReleaseAuthorisationRequest;
import com.kodypay.grpc.preauth.v1.ReleaseAuthorisationResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExampleReleaseAuthorisation {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with your PreAuth ID returned from the pre-authorisation
        String preAuthId = "PRE AUTH ID";
        //TODO: Replace these with your reference ID
        String releaseReference = "REF-" + System.currentTimeMillis();
        //TODO: Replace these with your idempotency UUID
        String idempotencyUuid = UUID.randomUUID().toString();

        releaseAuthorisation(storeId, preAuthId, releaseReference, idempotencyUuid);
    }

    // Example of a release authorisation with idempotency and reference IDs
    private static void releaseAuthorisation(String storeId, String preAuthId, String releaseReference, String idempotencyUuid) {
        var preAuthClient = createKodyPreAuthTerminalClient();

        ReleaseAuthorisationRequest releaseAuthRequest = ReleaseAuthorisationRequest.newBuilder()
                .setStoreId(storeId)
                .setPreAuthId(preAuthId)
                .setReleaseReference(releaseReference)
                .setIdempotencyUuid(idempotencyUuid)
                .build();

        ReleaseAuthorisationResponse releaseAuthResponse = preAuthClient.releaseAuthorisation(releaseAuthRequest);
        processReleaseAuthResponse(releaseAuthResponse);
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

    // Helper method to process release authorisation response
    private static void processReleaseAuthResponse(ReleaseAuthorisationResponse response) {
        // Release is processed asynchronously, and the final status can be queried through the GetPreAuthorisation interface.
        System.out.println("Release Ref: " + response.getReleaseReference());
        System.out.println("PspReference: " + response.getPspReference());
        System.out.println("ReleasedAt: " + response.getReleasedAt());
    }
}
