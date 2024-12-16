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

public class ExampleSendPayment {
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

        // Initialize PaymentClient
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), apiKey);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(address, 443)
                .idleTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .build();

        KodyPayTerminalServiceGrpc.KodyPayTerminalServiceStub paymentClient = KodyPayTerminalServiceGrpc.newStub(channel);

        // Define terminal request
        PayRequest payRequest = PayRequest.newBuilder()
                .setStoreId(storeId)
                .setAmount(String.valueOf(3000))
                .setTerminalId(terminalId)
                .setShowTips(true)
                .build();
        LOG.info("Sent Payment to Terminal");

        CountDownLatch latch = new CountDownLatch(1);

        paymentClient.pay(payRequest, new StreamObserver<>() {
            PayResponse response;

            @Override
            public void onNext(PayResponse res) {
                response = res;
                LOG.debug("Sent Payment to Terminal: response={}", response);
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("Sent Payment to Terminal: Failed to get terminals, message={}, stack={}", e.getMessage(), e);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                LOG.debug("Sent Payment to Terminal: complete");
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
