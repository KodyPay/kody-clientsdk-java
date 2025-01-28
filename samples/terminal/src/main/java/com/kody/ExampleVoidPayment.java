package com.kody;

// import com.kodypay.grpc.pay.v1.VoidPaymentRequest;
// import com.kodypay.grpc.pay.v1.VoidPaymentResponse;
import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.concurrent.TimeUnit;

public class ExampleVoidPayment {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Payment ID
        String paymentId = "PAYMENT ID";

        voidPayment(paymentId);
    }

    private static void voidPayment(String paymentId) {
        var paymentClient = createKodyTerminalPaymentsClient();

        // TODO: Coming soon - Void payment functionality
        // VoidPaymentRequest voidPaymentRequest = VoidPaymentRequest.newBuilder()
        //         .setPaymentId(paymentId)
        //         .build();
        //
        // VoidPaymentResponse voidPaymentResponse = paymentClient.void(voidPaymentRequest);
        // System.out.println("voidPaymentResponse.status: " + voidPaymentResponse.getStatus());

        System.out.println("Void payment functionality coming soon");
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
