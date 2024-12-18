package com.kody;

import com.kodypay.grpc.ecom.v1.GetPaymentsRequest;
import com.kodypay.grpc.ecom.v1.GetPaymentsResponse;
import com.kodypay.grpc.ecom.v1.KodyEcomPaymentsServiceGrpc;
import com.kodypay.grpc.sdk.common.PageCursor;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ExampleGetPayments {
    //TODO: Replace this with the testing or live environment
    public static final String HOSTNAME = "grpc-staging.kodypay.com";
    public static final String API_KEY = "API KEY";

    public static void main(String[] args) {
        //TODO: Replace this with your Store ID
        String storeId = "STORE ID";

        getEcomPayments(storeId);
    }

    public static void getEcomPayments(String storeId) {
        var paymentClient = createKodyEcomPaymentsClient();

        GetPaymentsRequest getPaymentsRequest = GetPaymentsRequest.newBuilder()
                .setStoreId(storeId)
                .setPageCursor(PageCursor.newBuilder().setPageSize(10)).build();

        GetPaymentsResponse payments = paymentClient.getPayments(getPaymentsRequest);
        for (GetPaymentsResponse.Response.PaymentDetails payment : payments.getResponse().getPaymentsList()) {
            // Payment ID is Kody generated
            System.out.println("Payment ID: " + payment.getPaymentId());
            // Payment reference and order ID is set by client
            System.out.println("Payment reference: " + payment.getPaymentReference());
            System.out.println("Payment order ID: " + payment.getOrderId());
            // Payment Status enumeration: PENDING, SUCCESS, FAILED, CANCELLED, EXPIRED, UNRECOGNIZED
            System.out.println("Payment status: " + payment.getStatus());
            System.out.println("Payment created timestamp: " + new Date(payment.getDateCreated().getSeconds() * 1000L));
            if (payment.getStatus() == GetPaymentsResponse.Response.PaymentDetails.PaymentStatus.SUCCESS) {
                System.out.println("Payment paid timestamp: " + new Date(payment.getDatePaid().getSeconds() * 1000L));
            }
            // Metadata sent by client in the payment initiation
            System.out.println("Payment order metadata: " + payment.getOrderMetadata());
            // Data related with payment method
            System.out.println("Payment data: " + payment.getPaymentDataJson());
        }
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
