package com.kody;

import com.kodypay.grpc.ecom.v1.KodyEcomPaymentsServiceGrpc;
import com.kodypay.grpc.ecom.v1.PaymentDetailsRequest;
import com.kodypay.grpc.ecom.v1.PaymentDetailsResponse;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ExampleGetPaymentDetails {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";
        //TODO: Replace this with the payment ID received during payment initiation
        String paymentId = "PAYMENT ID";

        getEcomPaymentDetails(storeId, paymentId);
    }

    public static void getEcomPaymentDetails(String storeId, String paymentId) {
        var paymentClient = createKodyEcomPaymentsClient();

        PaymentDetailsRequest paymentDetailsRequest = PaymentDetailsRequest.newBuilder()
                .setStoreId(storeId)
                .setPaymentId(paymentId)
                .build();

        PaymentDetailsResponse.Response paymentDetails = paymentClient.paymentDetails(paymentDetailsRequest).getResponse();

        // Payment ID is Kody generated
        System.out.println("Payment ID: " + paymentDetails.getPaymentId());
        // Payment reference and order ID is set by client
        System.out.println("Payment reference: " + paymentDetails.getPaymentReference());
        System.out.println("Payment order ID: " + paymentDetails.getOrderId());
        // Payment Status enumeration: PENDING, SUCCESS, FAILED, CANCELLED, EXPIRED, UNRECOGNIZED
        System.out.println("Payment status: " + paymentDetails.getStatus());
        System.out.println("Payment created timestamp: " + new Date(paymentDetails.getDateCreated().getSeconds() * 1000L));
        if (paymentDetails.getStatus() == PaymentDetailsResponse.Response.PaymentStatus.SUCCESS) {
            System.out.println("Payment paid timestamp: " + new Date(paymentDetails.getDatePaid().getSeconds() * 1000L));
        }
        // Metadata sent by client in the payment initiation
        System.out.println("Payment order metadata: " + paymentDetails.getOrderMetadata());
        // Data related with payment method
        System.out.println("Payment data: " + paymentDetails.getPaymentDataJson());
    }

    private static KodyEcomPaymentsServiceGrpc.KodyEcomPaymentsServiceBlockingStub createKodyEcomPaymentsClient() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), API_KEY);
        return KodyEcomPaymentsServiceGrpc.newBlockingStub(ManagedChannelBuilder
                .forAddress(HOSTNAME, 443)
                .idleTimeout(3, TimeUnit.MINUTES)
                .keepAliveTimeout(3, TimeUnit.MINUTES)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build());
    }
}
