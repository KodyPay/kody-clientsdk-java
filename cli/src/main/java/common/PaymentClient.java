package common;

import com.kodypay.grpc.ecom.v1.*;
import com.kodypay.grpc.ecom.v1.GetPaymentsResponse.Response.PaymentDetails;
import com.kodypay.grpc.pay.v1.*;
import com.kodypay.grpc.pay.v1.PaymentDetailsRequest;
import com.kodypay.grpc.pay.v1.RefundRequest;
import com.kodypay.grpc.pay.v1.RefundResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PaymentClient {

    private static final long TIMEOUT_MS = java.time.Duration.ofMinutes(3).toMillis();
    private static final Logger LOG = LoggerFactory.getLogger(PaymentClient.class);

    private final String apiKey;
    private final boolean tls;
    private final InetSocketAddress inetSocketAddress;
    private final KodyPayTerminalServiceGrpc.KodyPayTerminalServiceBlockingStub terminalServiceStub;
    private final KodyEcomPaymentsServiceGrpc.KodyEcomPaymentsServiceBlockingStub ecomServiceStub;

    public PaymentClient(URI address, String apiKey) {
        this.apiKey = apiKey;
        this.tls = address.getScheme().startsWith("https");
        this.inetSocketAddress = toInetSocketAddress(address);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(inetSocketAddress.getHostName(), inetSocketAddress.getPort())
                .idleTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .keepAliveTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(getMetadata()))
                .build();

        this.terminalServiceStub = KodyPayTerminalServiceGrpc.newBlockingStub(channel);
        this.ecomServiceStub = KodyEcomPaymentsServiceGrpc.newBlockingStub(channel);
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

    //////////////////////////////////////////////////////
    //////////////// TERMINAL - IN PERSON ////////////////
    //////////////////////////////////////////////////////

    public PayResponse sendTerminalPayment(PayRequest payRequest) {
        LOG.debug("sendPayment: storeId={}, amount={}, terminalId={} (address: {})", payRequest.getStoreId(), payRequest.getAmount(), payRequest.getTerminalId(), inetSocketAddress);

        return terminalServiceStub.pay(payRequest).next();
    }

    public PaymentStatus cancelPayment(CancelRequest cancelRequest) {
        LOG.debug("cancelPayment: storeId={}, amount={}, terminalId={}, orderId={}",
                cancelRequest.getStoreId(),
                cancelRequest.getAmount(),
                cancelRequest.getTerminalId(),
                cancelRequest.getOrderId()
        );

        return terminalServiceStub.cancel(cancelRequest).getStatus();
    }

    public RefundResponse requestTerminalRefund(RefundRequest refundRequest) {
        LOG.debug("requestTerminalRefund: storeId={}, amount={}, orderId={}", refundRequest.getStoreId(), refundRequest.getAmount(), refundRequest.getOrderId());

        return terminalServiceStub.refund(refundRequest).next();
    }

    public PayResponse getDetails(PaymentDetailsRequest paymentDetailsRequest) {
        LOG.debug("getDetails: storeId={}, orderId={}", paymentDetailsRequest.getStoreId(), paymentDetailsRequest.getOrderId());

        return terminalServiceStub.paymentDetails(paymentDetailsRequest);
    }

    public List<Terminal> getTerminals(TerminalsRequest terminalsRequest) {
        LOG.debug("getTerminals: storeId={}", terminalsRequest.getStoreId());

        return terminalServiceStub.terminals(terminalsRequest).getTerminalsList();
    }

    //////////////////////////////////////////////////////
    /////////////////// ECOM - ONLINE ////////////////////
    //////////////////////////////////////////////////////

    public PaymentInitiationResponse sendOnlinePayment(PaymentInitiationRequest paymentInitiationRequest) {
        LOG.debug("sendOnlinePayment: storeId={}, paymentReference={}, amount={}, currency={}, orderId={}, returnUrl={} (address: {})",
                paymentInitiationRequest.getStoreId(),
                paymentInitiationRequest.getPaymentReference(),
                paymentInitiationRequest.getAmount(),
                paymentInitiationRequest.getCurrency(),
                paymentInitiationRequest.getOrderId(),
                paymentInitiationRequest.getReturnUrl(),
                inetSocketAddress
        );

        return ecomServiceStub.initiatePayment(paymentInitiationRequest);
    }

    public com.kodypay.grpc.ecom.v1.RefundResponse requestOnlineRefund(com.kodypay.grpc.ecom.v1.RefundRequest refundRequest) {
        LOG.debug("requestOnlineRefund: storeId={}, paymentId={}, amount={} (address: {})",
                refundRequest.getStoreId(),
                refundRequest.getPaymentId(),
                refundRequest.getAmount(),
                inetSocketAddress
        );

        return ecomServiceStub.refund(refundRequest).next();
    }

    public PaymentDetailsResponse getPaymentDetails(com.kodypay.grpc.ecom.v1.PaymentDetailsRequest paymentDetailsRequest) {
        LOG.debug("get payment details: storeId={} paymentId={}", paymentDetailsRequest.getStoreId(), paymentDetailsRequest.getPaymentId());

        return ecomServiceStub.paymentDetails(paymentDetailsRequest);
    }

    public List<PaymentDetails> getPayments(GetPaymentsRequest getPaymentsRequest) {
        LOG.debug("getPayments: storeId={}", getPaymentsRequest.getStoreId());

        return ecomServiceStub.getPayments(getPaymentsRequest).getResponse().getPaymentsList();
    }
}
