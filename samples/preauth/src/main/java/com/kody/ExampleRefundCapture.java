package com.kody;

import com.kodypay.grpc.preauth.v1.*;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExampleRefundCapture {
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
        //TODO: Replace this with your Capture ID returned from the capture authorisation
        String captureId = "CAPTURE ID";
        //TODO: Replace with your store operating currency: ISO 4217
        String currencyCode = "HKD";
        //TODO: Replace with the auth amount in minor units, cannot exceed the captured amount
        long refundAmountMinorUnits = 1000;
        //TODO: Replace these with your reference ID
        String refundReference = "REF-" + System.currentTimeMillis();
        //TODO: Replace these with your idempotency UUID
        String idempotencyUuid = UUID.randomUUID().toString();

        refundCapture(storeId, preAuthId, captureId, refundAmountMinorUnits, currencyCode, refundReference, idempotencyUuid);
    }

    // Example of a capture authorisation with idempotency and reference IDs
    private static void refundCapture(String storeId, String preAuthId, String captureId, long refundAmountMinorUnits, String currency, String refundReference, String idempotencyUuid) {
        var preAuthClient = createKodyPreAuthTerminalClient();

        RefundCaptureRequest refundCaptureRequest = RefundCaptureRequest.newBuilder()
                .setStoreId(storeId)
                .setPreAuthId(preAuthId)
                .setCaptureId(captureId)
                .setRefundAmountMinorUnits(refundAmountMinorUnits)
                .setCurrency(currency)
                .setRefundReference(refundReference)
                .setIdempotencyUuid(idempotencyUuid)
                .build();

        RefundCaptureResponse refundCaptureResponse = preAuthClient.refundCapture(refundCaptureRequest);
        processRefundCaptureResponse(refundCaptureResponse);
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

    // Helper method to process refund capture response
    private static void processRefundCaptureResponse(RefundCaptureResponse response) {
        // There is no clear success status for refunds.
        // Generally, if REFUND_REQUESTED/REFUND_PENDING is returned, the refund request can be considered successful.
        // The actual success is subject to whether the user has received the refund.
        System.out.println("Refund Ref: " + response.getRefundReference());

        if (response.getStatus() == RefundStatus.REFUND_REQUESTED || response.getStatus() == RefundStatus.REFUND_PENDING) {
            System.out.println("PspReference: " + response.getPspReference());
            System.out.println("RefundedAt: " +  response.getRefundedAt());
        } else if (response.getStatus() == RefundStatus.REFUND_FAILED) {
            System.out.println("Refund failed: " + response.getFailureReason());
        }
    }
}
