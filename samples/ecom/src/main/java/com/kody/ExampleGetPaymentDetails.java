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

        System.out.println("Payment ID: " + paymentDetails.getPaymentId());
        System.out.println("Payment Status: " + paymentDetails.getStatus());
        System.out.println("Payment Created Timestamp: " + paymentDetails.getDateCreated().getSeconds() * 1000L);


        System.out.println("PaymentData:");
        var paymentData = paymentDetails.getPaymentData();

        if (paymentData != null) {
            System.out.println("  PspReference: " + paymentData.getPspReference());
            System.out.println("  PaymentMethod: " + paymentData.getPaymentMethod());
            System.out.println("  PaymentMethodVariant: " + paymentData.getPaymentMethodVariant());
            System.out.println("  AuthStatus: " + paymentData.getAuthStatus());
            System.out.println("  AuthStatusDate: " + paymentData.getAuthStatusDate());

            // Assuming it is a card payment
            PaymentDetailsResponse.PaymentData.PaymentCard paymentCard = paymentData.getPaymentCard();
            if (paymentCard != null) {
                System.out.println("  PaymentCard:");
                System.out.println("    CardLast4Digits: " + paymentCard.getCardLast4Digits());
                System.out.println("    AuthCode: " + paymentCard.getAuthCode());
                System.out.println("    PaymentToken: " + paymentCard.getPaymentToken());
            }
        }

        System.out.println("SaleData:");
        var saleData = paymentDetails.getSaleData();

        if (saleData != null) {
            System.out.println("  AmountMinorUnits: " + saleData.getAmountMinorUnits());
            System.out.println("  Currency: " + saleData.getCurrency());
            System.out.println("  OrderId: " + saleData.getOrderId());
            System.out.println("  PaymentReference: " + saleData.getPaymentReference());
            System.out.println("  OrderMetadata: " + saleData.getOrderMetadata());
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
