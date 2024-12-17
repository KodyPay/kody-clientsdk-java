package com.kody;

import com.kodypay.grpc.ecom.v1.*;
import com.kodypay.grpc.sdk.common.PageCursor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ExampleGetPayments {
    private static final Logger LOG = LoggerFactory.getLogger(ExampleGetPayments.class);
    private static final long TIMEOUT_MS = java.time.Duration.ofMinutes(3).toMillis();

    public static void main(String[] args) {

        Properties properties = loadProperties();
        var address = properties.getProperty("address", "grpc-developement.kodypay.com");
        var apiKey = properties.getProperty("apiKey");
        if (apiKey == null) {
            throw new IllegalArgumentException("Invalid config, expected apiKey");
        }
        var storeId = properties.getProperty("storeId");

        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), apiKey);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(address, 443)
                .idleTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build();

        KodyEcomPaymentsServiceGrpc.KodyEcomPaymentsServiceBlockingStub paymentClient = KodyEcomPaymentsServiceGrpc.newBlockingStub(channel);

        GetPaymentsRequest getPaymentsRequest = GetPaymentsRequest.newBuilder()
                .setStoreId(storeId)
                .setPageCursor(PageCursor.newBuilder().setPageSize(1)).build();
        LOG.info("getPayment");

        GetPaymentsResponse payments = paymentClient.getPayments(getPaymentsRequest);
        LOG.info("GetPaymentsResponse: {}", payments);
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

    private static String generatePaymentReference() {
        return "pay_" + UUID.randomUUID();
    }

}
