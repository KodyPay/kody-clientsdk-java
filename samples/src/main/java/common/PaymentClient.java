package common;

import com.kodypay.grpc.ecom.v1.*;
import com.kodypay.grpc.ecom.v1.GetPaymentsResponse.Response.PaymentDetails;
import com.kodypay.grpc.ecom.v1.KodyEcomPaymentsServiceGrpc.KodyEcomPaymentsServiceStub;
import com.kodypay.grpc.ecom.v1.PaymentInitiationRequest.ExpirySettings;
import com.kodypay.grpc.pay.v1.CancelRequest;
import com.kodypay.grpc.pay.v1.CancelResponse;
import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc;
import com.kodypay.grpc.pay.v1.KodyPayTerminalServiceGrpc.KodyPayTerminalServiceStub;
import com.kodypay.grpc.pay.v1.PayRequest;
import com.kodypay.grpc.pay.v1.PayResponse;
import com.kodypay.grpc.pay.v1.PaymentDetailsRequest;
import com.kodypay.grpc.pay.v1.PaymentStatus;
import com.kodypay.grpc.pay.v1.Terminal;
import com.kodypay.grpc.pay.v1.TerminalsRequest;
import com.kodypay.grpc.pay.v1.TerminalsResponse;
import com.kodypay.grpc.sdk.common.PageCursor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PaymentClient {

    private static final long TIMEOUT_MS = java.time.Duration.ofMinutes(3).toMillis();
    private static final Logger LOG = LoggerFactory.getLogger(PaymentClient.class);

    private final UUID payStoreId;
    private final String apiKey;
    private final boolean tls;
    private final InetSocketAddress inetSocketAddress;
    private final KodyPayTerminalServiceStub terminalServiceStub;
    private final KodyEcomPaymentsServiceStub ecomServiceStub;

    public PaymentClient(URI address, UUID payStoreId, String apiKey) {
        this.payStoreId = payStoreId;
        this.apiKey = apiKey;
        this.tls = address.getScheme().startsWith("https");
        this.inetSocketAddress = toInetSocketAddress(address);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(inetSocketAddress.getHostName(), inetSocketAddress.getPort())
                .idleTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(getMetadata()))
                .build();

        this.terminalServiceStub = KodyPayTerminalServiceGrpc.newStub(channel);
        this.ecomServiceStub = KodyEcomPaymentsServiceGrpc.newStub(channel);
    }

    private InetSocketAddress toInetSocketAddress(URI uri) {
        int port = uri.getPort() > 0 ? uri.getPort() : (tls ? 443 : 80);
        return new InetSocketAddress(uri.getHost(), port);
    }

    private Metadata getMetadata() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("X-API-Key", Metadata.ASCII_STRING_MARSHALLER), apiKey);
        return metadata;
    }

    public CompletableFuture<PayResponse> sendPayment(String terminalId, BigDecimal amount, java.util.function.Consumer<String> onPending) {
        return sendPayment(terminalId, amount, false, onPending);
    }

    public CompletableFuture<PayResponse> sendPayment(String terminalId, BigDecimal amount, boolean showTips, java.util.function.Consumer<String> onPending) {
        LOG.debug("sendPayment: storeId={}, amount={}, terminalId={} (address: {})", payStoreId, amount, terminalId, inetSocketAddress);

        CompletableFuture<PayResponse> future = new CompletableFuture<>();
        terminalServiceStub.pay(PayRequest.newBuilder()
                .setStoreId(payStoreId.toString())
                .setAmount(amount.toPlainString())
                .setTerminalId(terminalId)
                .setShowTips(showTips)
                .build(), new StreamObserver<>() {
            PayResponse response;

            @Override
            public void onNext(PayResponse res) {
                response = res;
                LOG.debug("sendPayment: response={}", response);
                if (response.getStatus() == PaymentStatus.PENDING) {
                    onPending.accept(response.getOrderId());
                }
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("sendPayment: error sending payment, message={}, stack={}", e.getMessage(), e);
                future.completeExceptionally(e);
            }

            @Override
            public void onCompleted() {
                LOG.debug("sendPayment: complete");
                future.complete(response);
            }
        });
        return future;
    }

    public CompletableFuture<PaymentInitiationResponse> sendOnlinePayment(String paymentReference, long amount, String currency, String orderId, String returnUrl) {
        LOG.debug("sendOnlinePayment: storeId={}, paymentReference={}, amount={}, currency={}, orderId={}, returnUrl={} (address: {})", payStoreId, paymentReference, amount, currency, orderId, returnUrl, inetSocketAddress);

        CompletableFuture<PaymentInitiationResponse> future = new CompletableFuture<>();
        ecomServiceStub.initiatePayment(PaymentInitiationRequest.newBuilder()
                .setStoreId(payStoreId.toString())
                .setPaymentReference(paymentReference)
                .setAmount(amount)
                .setCurrency(currency)
                .setOrderId(orderId)
                .setReturnUrl(returnUrl)
                .setExpiry(ExpirySettings.newBuilder()
                        .setShowTimer(true)
                        .setExpiringSeconds(1800)
                        .build()
                ).build(), new StreamObserver<>() {
            PaymentInitiationResponse response;

            @Override
            public void onNext(PaymentInitiationResponse res) {
                response = res;
                LOG.debug("sendOnlinePayment: response={}", response);
                if (response.hasError()) {
                    future.completeExceptionally(new Throwable(response.getError().getMessage()));
                }
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("sendOnlinePayment: error sending online payment, message={}, stack={}", e.getMessage(), e);
                future.completeExceptionally(e);
            }

            @Override
            public void onCompleted() {
                LOG.debug("sendOnlinePayment: complete");
                future.complete(response);
            }
        });
        return future;
    }

    public CompletableFuture<PaymentStatus> cancelPayment(BigDecimal amount, String terminalId, String orderId) {
        LOG.debug("cancelPayment: storeId={}, amount={}, terminalId={}, orderId={}", payStoreId, amount, terminalId, orderId);

        CompletableFuture<PaymentStatus> future = new CompletableFuture<>();
        terminalServiceStub.cancel(CancelRequest.newBuilder()
                .setStoreId(payStoreId.toString())
                .setAmount(amount.toPlainString())
                .setTerminalId(terminalId)
                .setOrderId(orderId)
                .build(), new StreamObserver<CancelResponse>() {
            CancelResponse response;

            @Override
            public void onNext(CancelResponse res) {
                response = res;
                PaymentStatus paymentStatus = response.getStatus();
                LOG.debug("cancelPayment: response={}", response);
                if (paymentStatus == PaymentStatus.PENDING) {
                    LOG.error("cancelPayment: Failed to cancel payment, status={}, message={}", paymentStatus, response);
                }
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("cancelPayment: error canceling payment, message={}, stack={}", e.getMessage(), e);
                future.completeExceptionally(e);
            }

            @Override
            public void onCompleted() {
                LOG.debug("cancelPayment: complete");
                future.complete(response.getStatus());
            }
        });
        return future;
    }

    public CompletableFuture<PayResponse> getDetails(String orderId) {
        LOG.debug("getDetails: storeId={}, orderId={}", payStoreId, orderId);

        CompletableFuture<PayResponse> future = new CompletableFuture<>();
        terminalServiceStub.paymentDetails(PaymentDetailsRequest.newBuilder()
                .setStoreId(payStoreId.toString())
                .setOrderId(orderId)
                .build(), new StreamObserver<PayResponse>() {
            PayResponse response;

            @Override
            public void onNext(PayResponse res) {
                response = res;
                LOG.debug("getDetails: response={}", response);
                if (response.getStatus() == PaymentStatus.PENDING) {
                    LOG.error("getDetails: Failed to get payment details, status={}, message={}", response.getStatus(), response);
                }
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("getDetails: error getting details, message={}, stack={}", e.getMessage(), e);
                future.completeExceptionally(e);
            }

            @Override
            public void onCompleted() {
                LOG.debug("getDetails: complete");
                future.complete(response);
            }
        });
        return future;
    }

    public CompletableFuture<List<Terminal>> getTerminals() {
        LOG.debug("getTerminals");

        CompletableFuture<List<Terminal>> future = new CompletableFuture<>();
        terminalServiceStub.terminals(TerminalsRequest.newBuilder()
                .setStoreId(payStoreId.toString())
                .build(), new StreamObserver<>() {
            TerminalsResponse response;

            @Override
            public void onNext(TerminalsResponse res) {
                response = res;
                LOG.debug("getTerminals: response={}", response);
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("getTerminals: Failed to get terminals, message={}, stack={}", e.getMessage(), e);
                future.completeExceptionally(e);
            }

            @Override
            public void onCompleted() {
                LOG.debug("getTerminals: complete");
                future.complete(response.getTerminalsList());
            }
        });
        return future;
    }

    public CompletableFuture<List<PaymentDetails>> getPayments() {
        LOG.debug("getPayments");

        CompletableFuture<List<PaymentDetails>> future = new CompletableFuture<>();
        ecomServiceStub.getPayments(GetPaymentsRequest.newBuilder()
                .setStoreId(payStoreId.toString())
                .setPageCursor(PageCursor.newBuilder().setPageSize(1).build())
                .build(), new StreamObserver<>() {
            GetPaymentsResponse response;

            @Override
            public void onNext(GetPaymentsResponse res) {
                response = res;
                LOG.debug("getPayments: response={}", response);
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("getPayments: Failed to get payments, message={}, stack={}", e.getMessage(), e);
                future.completeExceptionally(e);
            }

            @Override
            public void onCompleted() {
                LOG.debug("getPayments: complete");
                future.complete(response.getResponse().getPaymentsList());
            }
        });
        return future;
    }
}
