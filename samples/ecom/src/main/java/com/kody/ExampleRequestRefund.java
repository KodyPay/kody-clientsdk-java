package com.kody;

import com.kodypay.grpc.ecom.v1.KodyEcomPaymentsServiceGrpc;
import com.kodypay.grpc.ecom.v1.RefundRequest;
import com.kodypay.grpc.ecom.v1.RefundResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class ExampleRequestRefund {
    private static final Logger LOG = LoggerFactory.getLogger(ExampleRequestRefund.class);
    private static final long TIMEOUT_MS = java.time.Duration.ofMinutes(3).toMillis();

    public static void main(String[] args) {

        // Load configuration properties
        Properties properties = loadProperties();
        var address = properties.getProperty("address", "grpc-developement.kodypay.com");
        var apiKey = properties.getProperty("apiKey");
        if (apiKey == null) {
            throw new IllegalArgumentException("Invalid config, expected apiKey");
        }
        var storeId = properties.getProperty("storeId");

        // Initialize PaymentClient
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), apiKey);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(address, 443)
                .idleTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build();

        KodyEcomPaymentsServiceGrpc.KodyEcomPaymentsServiceStub paymentClient = KodyEcomPaymentsServiceGrpc.newStub(channel);

        // Define refund request
        RefundRequest refundRequest = RefundRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount("2.22")
                .setPaymentId("paymentId")
                .build();
        LOG.info("sendrequestRefund");

        CountDownLatch latch = new CountDownLatch(1);

        paymentClient.refund(refundRequest, new StreamObserver<>() {
            RefundResponse response;

            @Override
            public void onNext(RefundResponse res) {
                response = res;
                RefundResponse.RefundStatus refundStatus = response.getStatus();
                LOG.debug("sendrequestRefund: response={}", response);
                if (refundStatus == RefundResponse.RefundStatus.FAILED) {
                    LOG.error("requestRefund: Failed to request refund, status={}, message={}", refundStatus, response);
                }
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("sendRequestRefund: error sending request refund, message={}, stack={}", e.getMessage(), e);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                LOG.debug("sendOnlinePayment: complete");
                latch.countDown();
            }
        });

        try {
            latch.await(); // Wait for the response
        } catch (InterruptedException e) {
            LOG.error("Main thread interrupted while waiting for response", e);
        } finally {
            channel.shutdown(); // Ensure the channel is shut down
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (var inputStream = ExampleNewPayment.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Config file 'config.properties' not found in resources folder");
            }
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
        return properties;
    }
}
