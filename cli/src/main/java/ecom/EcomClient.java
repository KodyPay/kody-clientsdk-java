package ecom;

import cli.Command;
import cli.EcomPaymentInput;
import cli.PaymentCommand;
import com.kodypay.grpc.ecom.v1.*;
import com.kodypay.grpc.ecom.v1.GetPaymentsResponse.Response.PaymentDetails;
import com.kodypay.grpc.sdk.common.PageCursor;
import common.CurrencyEnum;
import common.PaymentClient;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.EnumCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import static common.Utils.*;

public class EcomClient {
    private static final Logger LOG = LoggerFactory.getLogger(EcomClient.class.getName());
    private static final EcomClient ecomClient = new EcomClient();

    private final PaymentClient paymentClient;
    private final UUID storeId;

    public EcomClient() {
        Properties properties = loadProperties();
        var address = URI.create(properties.getProperty("address", "http://localhost"));
        var apiKey = properties.getProperty("apiKey");
        if (apiKey == null)
            throw new IllegalArgumentException("Invalid config, expected apiKey");

        storeId = UUID.fromString(properties.getProperty("storeId"));
        paymentClient = new PaymentClient(address, apiKey);
    }

    public PaymentInitiationResponse sendPayment(long amount) {
        String paymentReference = generatePaymentReference();
        String orderId = generateOrderId();
        String currency = CurrencyEnum.HKD.name();
        return sendPayment(paymentReference, amount, currency, orderId);
    }

    private PaymentInitiationResponse sendPayment(String paymentReference, long amount, String currency, String orderId) {
        LOG.info("Initiating payment for amount: {}", amount);

        PaymentInitiationRequest paymentInitiationRequest = PaymentInitiationRequest.newBuilder()
                .setStoreId(storeId.toString())
                .setPaymentReference(paymentReference)
                .setAmount(amount)
                .setCurrency(currency)
                .setOrderId(orderId)
                .setReturnUrl("returnUrl")
                .setExpiry(PaymentInitiationRequest.ExpirySettings.newBuilder()
                        .setShowTimer(true)
                        .setExpiringSeconds(1800)
                        .build()
                ).build();

        PaymentInitiationResponse response = paymentClient.sendOnlinePayment(paymentInitiationRequest);

        LOG.info("Sent Online Payment - Payment Id: {}, Payment URL: {}", response.getResponse().getPaymentId(), response.getResponse().getPaymentUrl());
        return response;
    }

    void requestRefund(String paymentId, String amount) {
        LOG.info("Request refund for amount: {}", amount);

        RefundRequest refundRequest = RefundRequest.newBuilder()
                .setStoreId(storeId.toString())
                .setPaymentId(paymentId)
                .setAmount(amount)
                .build();

        RefundResponse response = paymentClient.requestOnlineRefund(refundRequest);

        LOG.info("Requested Online Refund - Response: {}", response);
    }

    public PaymentDetailsResponse getPaymentDetails(String paymentId) {
        LOG.info("Get payment details");

        PaymentDetailsRequest paymentDetailsRequest = PaymentDetailsRequest.newBuilder()
                .setStoreId(storeId.toString())
                .setPaymentId(paymentId)
                .build();

        PaymentDetailsResponse response = paymentClient.getPaymentDetails(paymentDetailsRequest);

        LOG.info("PaymentDetailsResponse: {}", response);

        return response;
    }

    public void getPayments() {
        LOG.info("Get payments");

        GetPaymentsRequest getPaymentsRequest = GetPaymentsRequest.newBuilder()
                .setStoreId(storeId.toString())
                .setPageCursor(PageCursor.newBuilder().setPageSize(1).build())
                .build();

        List<PaymentDetails> response = paymentClient.getPayments(getPaymentsRequest);

        LOG.info("Payments: {}", response.stream()
                .map(p -> "PaymentId: " + p.getPaymentId() + ", OrderId: " + p.getOrderId())
                .collect(Collectors.joining(", ")));
    }

    public static void main(String[] args) throws InterruptedException {
        long amountInPence = 100;
        String amountString = "1.00"; // Refund request accepts a string with decimal points

        PaymentDetailsResponse.Response paymentDetailsResponse;
        PaymentInitiationResponse paymentResponse = ecomClient.sendPayment(amountInPence);
        String paymentId = paymentResponse.getResponse().getPaymentId();

        // Wait for payment to be complete before refunding it
        do {
            LOG.info("Waiting for online payment to complete");
            Thread.sleep(5000);
            paymentDetailsResponse = ecomClient.getPaymentDetails(paymentId).getResponse();
        } while (!paymentDetailsResponse.hasPspReference() || paymentDetailsResponse.getStatus() == PaymentDetailsResponse.Response.PaymentStatus.PENDING);

        ecomClient.requestRefund(paymentId, amountString);
    }

    public static class SendPaymentCommand implements PaymentCommand {

        private static final EcomPaymentInput input = new EcomPaymentInput();

        public SendPaymentCommand() {
        }

        @Override
        public void execute() {
            ecomClient.sendPayment(input.getPaymentReference(), input.getAmount(), input.getCurrency(), input.getOrderId());
        }

        @Override
        public void gatherInput() {
            LineReader reader = LineReaderBuilder.builder().build();

            input.setAmount(Long.parseLong(reader.readLine("\nAmount: ")));

            reader = LineReaderBuilder.builder()
                    .completer(new EnumCompleter(CurrencyEnum.class))
                    .build();

            input.setCurrency(CurrencyEnum.valueOf(readInput(reader, "Currency [HKD]: ", "HKD").toUpperCase()));

            reader = LineReaderBuilder.builder().build();
            var orderId = input.getOrderId();
            input.setOrderId(readInput(reader, String.format("Order id [%s]: ", orderId), orderId));

            var paymentReference = input.getPaymentReference();
            input.setPaymentReference(readInput(reader, String.format("Payment reference [%s]: ", paymentReference), paymentReference));
        }
    }

    public static class GetPaymentsCommand implements Command {

        @Override
        public void execute() {
            ecomClient.getPayments();
        }
    }
}
