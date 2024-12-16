package com.kody;

import com.kodypay.grpc.pay.v1.*;
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

public class ExampleCancelPayment {
    private static final Logger LOG = LoggerFactory.getLogger(ExampleSendPayment.class);
    private static final long TIMEOUT_MS = java.time.Duration.ofMinutes(3).toMillis();

    public static void main(String[] args) {
        // Load configuration properties
        Properties properties = loadProperties();
        var address = properties.getProperty("address", "grpc-staging.kodypay.com");
        var apiKey = properties.getProperty("apiKey");
        var terminalId = properties.getProperty("terminalId");
        if (apiKey == null) {
            throw new IllegalArgumentException("Environment variable API-KEY is missing");
        }
        var storeId = properties.getProperty("storeId");
        var orderId = properties.getProperty("orderId");

        // Initialize PaymentClient
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), apiKey);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(address, 443)
                .idleTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build();

        KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub paymentClient = KodyPayTerminalServiceGrpc.newBlockingStub(channel);

        // Define terminal request
        CancelRequest cancelRequest = CancelRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(String.valueOf(1000))
                .setTerminalId(terminalId)
                .setOrderId(orderId)
                .build();
        LOG.info("Cancel Payment in Terminal");

        CancelResponse cancelResponse = paymentClient.cancel(cancelRequest);
        LOG.info("cancelResponse: {}", cancelResponse);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (var inputStream = ExampleGetTerminals.class.getClassLoader().getResourceAsStream("config.properties")) {
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
