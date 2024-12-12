# Kody Payments API

This guide provides an overview of using the Kody Payments API and its reference documentation.

- [Client Libraries](#client-libraries)
- [Java Installation](#java-installation)
- [Authenticate to Payments API](#authenticate-to-payments-api)
- [Payments API Reference](#payments-api-reference)
- [API data reference and Demo code](#api-data-reference-and-demo-code)
- [More sample code](#more-sample-code)

## Client Libraries

Kody provides client libraries for many popular languages to access the APIs. If your desired programming language is supported by the client libraries, we recommend that you use this option.

Available languages:
- Java : https://github.com/KodyPay/kody-clientsdk-java/
- Java 6: https://github.com/KodyPay/kody-clientsdk-java6/
- Python: https://github.com/KodyPay/kody-clientsdk-python/
- PHP: https://github.com/KodyPay/kody-clientsdk-php/
- .Net: https://github.com/KodyPay/kody-clientsdk-dotnet/

The advantages of using the Kody Client library instead of a REST API are:
- Maintained by Kody.
- Built-in authentication and increased security.
- Built-in retries.
- Idiomatic for each language.
- Quicker development.
- Backwards compatibility with new versions.

If your coding language is not listed, please let the Kody team know and we will be able to create it for you.

## Java Installation
### Requirements
- Java client supports JDK 17 and above
- Gradle (optional), recommended way to install the SDK

Install the Kody Java Client SDK using the following gradle snippet:

```kts
dependencies {
    implementation("com.kodypay.grpc:kody-clientsdk-java:0.0.6")
}
```
The library can also be downloaded from [here](https://central.sonatype.com/artifact/com.kodypay.api/kody-clientsdk-java/overview).

### Import in code

````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;
````

## Authenticate to Payments API

The client library uses a combination of a `Store ID` and an `API key`.
These will be shared with you during the technical integration onboarding or by your Kody contact.

During development, you will have access to a **test Store** and **test API key**, and when the integration is ready for live access, the production credentials will be shared securely with you and associated with a live store that was onboarded on Kody.

The test and live API calls are always compatible, only changing credentials and the service hostname is required to enable the integration in production.

### Host names

- Development and test: `https://grpc-staging.kodypay.com`
- Live: `https://grpc.kodypay.com`

### API Authentication

Every client library request authenticates with the server using a `storeId` and an `API Key`. The `storeId` is used as a request parameter and the `API Key` is configured when initialising the client.

Example:
````java
String storeId = "UUID of assigned store"; // STORE_ID
PaymentClient paymentClient = new PaymentClient(HOSTNAME, API_KEY);

TerminalsRequest terminalsRequest = TerminalsRequest.newBuilder()
        .setStoreId(storeId)
        .build();

List<Terminal> response = paymentClient.getTerminals(terminalsRequest);
````

Replace the `STORE_ID`, `API_KEY` and `HOSTNAME` with the details provided by the Kody team.
Note: it is recommended that you store the `API_KEY` in a secured storage, and insert it into your code via an environment variable.

## Payments API Reference

Kody supports the following channels to accept payments via API (using the Java 6 library).

1. [**Terminal**](#terminal---in-person-payments) - In-person payments

Each of these channels have their own collection of services that are available as method calls on the client library:
- `PaymentClient`

### Terminal - In-person payments

The Kody Payments API Terminal service has the following methods:

- [Get List of Terminals](#get-list-of-Terminals): `PaymentClient.getTerminals` - returns all the terminals of the store and their online status
- [Create Terminal Payment](#create-terminal-payment):`PaymentClient.sendTerminalPayment` - initiate a terminal payment
- [Cancel terminal payment](#cancel-terminal-payment): `PaymentClient.cancelPayment` - cancel an active terminal payment
- [Get Payment Details](#get-terminal-payment-details) `PaymentClient.getDetails` - get the payment details
- [Refund terminal payment](#refund-terminal-payment): `PaymentClient.requestRefund` - refund a terminal payment

Follow the links for these methods to see the sample code and the data specification.

## API data reference and Demo code

Every request to the client library requires authentication and the identifier of the store. See more [authentication](#authenticate-to-payments-api).

### Get list of Terminals

This is a simple and read only method, that returns a list of all terminals assigned to the store, and their online status.
You can use this request frequently, and it is a good way to check if your API code is configured properly for authentication.

The terminals request requires the following parameters:
- `storeId` - the ID of your assigned store

#### TerminalsRequest : Terminal Request
````java
public class TerminalRequest {
  private String storeId; // STORE_ID
}
````

#### TerminalsResponse : Terminal Response
```java
public class Terminal {
    private String terminalId;  // Terminal serial number
    private boolean online;     // Online status
}

public class TerminalsResponse {
    private List<Terminal> terminals;  // List of Terminal objects
}
```

#### Java Demo
````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;

String storeId = "UUID of assigned store"; // STORE_ID
PaymentClient paymentClient = new PaymentClient(HOSTNAME, APIKEY);

TerminalsRequest terminalsRequest = TerminalsRequest.newBuilder()
        .setStoreId(storeId)
        .build();

List<Terminal> response = paymentClient.getTerminals(terminalsRequest);
````

### Create terminal payment

Send a payment initiation request to a terminal.
This request will either make the terminal immediately display the card acquiring screen, or display a tip screen to the user after which it will go to the card acquiring screen.

A test terminal might have multiple apps on the OS screen. Launch the terminal app called `[S] Payments`.

The terminal must be in the mode: `Wait for Orders` which can be launched from the terminal app menu.
A store that has the feature `Wait for Orders` enabled will always launch the `Wait for Orders` screen automatically.
This screen can be closed (by tapping the `X` icon) to access other terminal features, but payments from API will not work until the `Wait for Orders` screen is started.

#### PayRequest - Payment Request
```java
public class PayRequest {
    private String amount = null;
    private Boolean showTips = null;
    private PaymentMethod paymentMethod = null;
}

public class PaymentMethod {
  private PaymentMethodType paymentMethodType = null;
}

public enum PaymentMethodType {
  CARD("CARD"),
  ALIPAY("ALIPAY"),
  WECHAT("WECHAT")
}
```

Request parameters:
- `storeId` - the UUID of your assigned store
- `terminalId` - the serial number of the terminal that will process the payment request. This number is returned by the [list of terminals request](#get-list-of-terminals), or can be found on the back label of the hardware.
- `amount` - amount as a 2.dp decimal number, such as `"1.00"`
- `showTips` - (optional) whether to show (true) or hide (false) the tip options. Default is (false)
- `payment_method` - (optional) Settings to enable going straight to QR scanning
    - `payment_method_type` - Payment method type: CARD (default), ALIPAY, WECHAT


#### PayResponse : Payment Response

````java
public enum PaymentStatus {
    PENDING("PENDING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED")
}

public class PayResponse {
    private PaymentStatus status = null;            // Status of payment
    private String failureReason = null;            // Optional, reason for failure
    private Map<String, Object> receiptJson = null; // Optional, json blob for receipt data
    private String orderId = null;                  // Unique order ID generated by Kody
    private OffsetDateTime dateCreated = null;      // Timestamp when the response was created
    private String extPaymentRef = null;            // Optional, external payment reference
    private OffsetDateTime datePaid = null;         // Optional, timestamp for date paid
    private String totalAmount = null;              // Optional, total amount
    private String saleAmount = null;               // Optional, sale amount
    private String tipsAmount = null;               // Optional, tips amount
}
````

#### Java Demo
````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;

PaymentClient paymentClient = new PaymentClient(HOSTNAME, APIKEY);

String storeId = "UUID of assigned store";
String terminalId = "Terminal serial number";
String amount = "1.00";
boolean showTips = false;
PaymentMethodType paymentMethodType = PaymentMethodType.CARD;

PayRequest payRequest = PayRequest.newBuilder()
        .setStoreId(storeId)
        .setAmount(amount)
        .setTerminalId(terminalId)
        .setShowTips(showTips)
        .setPaymentMethod(PaymentMethod.newBuilder().setPaymentMethodType(paymentMethodType).build())
        .build();

PayResponse response = paymentClient.sendPayment(payRequest);

// Note: the response will be returned with a PENDING payment status
// the payment details should be retrieved by the client until the payment either completes (SUCCESS/FAILED) or is CANCELLED (see below).
````

### Get Terminal Payment Details

The payment details request requires the following parameters:
- `storeId` - the ID of your assigned store
- `orderId` - the Order ID returned in the initial payment response, a unique UUID value for each payment.

#### PaymentDetailsRequest: Payment Details Request
````java
public class PaymentDetailsRequest {
    private String storeId;
    private String orderId;
}
````
- PayResponse : [Get Payment Detail Response](#payresponse--payment-response)

#### Java Demo
````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;

PaymentClient paymentClient = new PaymentClient(HOSTNAME, APIKEY);

String storeId = "UUID of assigned store";
String orderId = "UUID of order generated by Kody";

PaymentDetailsRequest paymentDetailsRequest = PaymentDetailsRequest.newBuilder()
        .setStoreId(storeId)
        .setOrderId(orderId)
        .build();

PayResponse response = paymentClient.getDetails(paymentDetailsRequest);
````

### Cancel Terminal Payment
Cancel an ongoing terminal payment, before it has been paid on the terminal.

#### CancelRequest - Cancel Payment Request
````java
public class CancelRequest {
    private String amount = null;
    private String orderId = null;
}
````

The cancel payment request requires the following parameters:

- `storeId` - the ID of your assigned store
- `terminalId` - the serial number of the terminal that is processing the payment request
- `amount` - the amount sent in the original payment request, used to find the payment request
- `orderId` - the Order ID returned in the initial payment response, a unique UUID value for each payment

#### CancelResponse : Cancel Payment Response
````java
public enum PaymentStatus {
    PENDING("PENDING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED")
}

public class CancelResponse {
    private PaymentStatus status;   //Cancellation status
}
````

#### Java Demo
````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;

PaymentClient paymentClient = new PaymentClient(HOSTNAME, APIKEY);

String storeId = "UUID of assigned store"; //STORE_ID
String terminalId = "Terminal serial number";
String amount = "1.00";
String orderId = "UUID of order generated by Kody";

CancelRequest cancelRequest = CancelRequest.newBuilder()
        .setStoreId(storeId)
        .setAmount(amount)
        .setTerminalId(terminalId)
        .setOrderId(orderId)
        .build();

PaymentStatus response = paymentClient.cancelPayment(cancelRequest);
````

### Refund Terminal Payment

#### RefundRequest - Refund Payment Request
```java
public class RefundRequest {
  private String storeId;
  private String orderId;
  private String amount;
}
```

The refund payment request requires the following parameters:

- `storeId` - the ID of your assigned store
- `orderId` - the Order ID returned in the initial payment response, a unique UUID value for each payment
- `amount` - the amount to refund, should be less than or equal to the original payment amount

#### RefundResponse : Refund Payment Response
````java
public enum RefundStatus {
    PENDING("PENDING"),
    REQUESTED("REQUESTED"),
    FAILED("FAILED")
}

public class RefundResponse {
    private RefundStatus status;             // Refund status
    private String orderId;                  // The order ID sent in the request
    private String failureReason;            // If the refund fails, this will show the reason why
    private OffsetDateTime dateCreated;      // Date when the refund was requested
    private String totalPaidAmount;          // How much was paid for the order
    private String totalAmountRequested;     // The total amount of refunds requested for this order
    private String totalAmountRefunded;      // The total amount of refunds applied to this order
    private String remainingAmount;          // The amount remaining for this order
    private String paymentTransactionId;     // The ID of the payment that needs refunding
}
````

#### Java Demo
````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;

PaymentClient paymentClient = new PaymentClient(HOSTNAME, APIKEY);

String storeId = "UUID of assigned store"; //STORE_ID
String orderId = "UUID of order generated by Kody";
String amount = "1.00";

RefundRequest refundRequest = RefundRequest.newBuilder()
        .setStoreId(storeId)
        .setAmount(amount)
        .setOrderId(orderId)
        .build();

RefundResponse response = paymentClient.requestTerminalRefund(refundRequest);
````

1. [**Ecom**](#ecom---online-payments) - In-person payments

Each of these channels have their own collection of services that are available as method calls on the client library:
- `PaymentClient`

### Ecom - Online payments

The Kody Payments API Ecom service has the following methods:

- [Create Online Payment](#create-online-payment):`PaymentClient.sendOnlinePayment` - initiate an online payment
- [Get Payments](#get-online-payments) `PaymentClient.getPayments` - get online payments
- [Refund online payment](#refund-online-payment): `PaymentClient.refundOnlinePayment` - refund an online payment

Follow the links for these methods to see the sample code and the data specification.

## API data reference and Demo code

Every request to the client library requires authentication and the identifier of the store. See more [authentication](#authenticate-to-payments-api).

### Create online payment

Send a payment initiation request for an online payment. Returns an object with a URL to display an online payment page to the shopper.

#### PayRequest - Payment Request
```java
public class PaymentInitiationRequest {
    private String storeId = "UUID of assigned store";
    private String orderId = "a unique reference for the payment";
    private String paymentReference = "a unique reference for the payment";
    private String amount = "1.00";
    private String currency = "GBP";
    private String returnUrl = "returnUrl";
    private ExpirySettings expirySettings;
}

public class ExpirySettings {
    private bool showTimer;
    private int expiringSeconds;
}
```

Request parameters:
- `storeId` - the ID of your assigned store
- `paymentReference` - a unique reference for the payment, sent from the client and returned by the server
- `amount` - the amount to request for the online payment, formatted as a 2.dp decimal number, such as `1.00`
- `currency` - the currency for this payment in 3 character ISO format, such as `GBP`
- `orderId` - a unique order ID for this payment, sent from the client and returned by the server
- `returnUrl` - where the payment form will redirect to after the payment has completed, the return url will have additional query parameters appended to indicate the status of the payment request.
- `expiry` - (optional) setting expiry settings
  - `showTimer` - (optional) flag to show countdown timer in payment page
  - `expiringSeconds` - (optional) how long the payment form will wait until the payment expires and the page will redirect to the return url


#### PayResponse : Payment Response

````java
public enum ErrorType {
  UNKNOWN("UNKNOWN"),
  DUPLICATE_ATTEMPT("DUPLICATE_ATTEMPT"),
  INVALID_REQUEST("INVALID_REQUEST"),
  CANCELLED("CANCELLED")
}

public class Error {
  private ErrorType type;       //Enum for the error type
  private String message;       //Error message
}

public class PaymentInitiationResponse {
    private Response response;
    private Error error;
}

public class Response {
  private String paymentId;     // Unique identifier created by Kody
  private String paymentUrl;    // The URL to send to the user from your application 
}
````

#### Java Demo
````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;

PaymentClient paymentClient = new PaymentClient(HOSTNAME, APIKEY);

String storeId = "UUID of assigned store";
String orderId = "a unique reference for the payment";
String paymentReference = "a unique reference for the payment";
String amount = "1.00";
String currency = "GBP";
String returnUrl = "returnUrl";
ExpirySettings expirySettings = PaymentInitiationRequest.ExpirySettings.newBuilder()
        .setShowTimer(true)
        .setExpiringSeconds(1800)
        .build();

PaymentInitiationRequest paymentInitiationRequest = PaymentInitiationRequest.newBuilder()
        .setStoreId(storeId)
        .setPaymentReference(paymentReference)
        .setAmount(amount)
        .setCurrency(currency)
        .setOrderId(orderId)
        .setReturnUrl(returnUrl)
        .setExpiry(expirySettings)
        .build();

PaymentInitiationResponse response = paymentClient.sendOnlinePayment(paymentInitiationRequest);
````

### Get Online Payments

The payment details request requires the following parameters:
- `storeId` - the ID of your assigned store
- `pageCursor` - set pagination settings
  - `page` - set an offset of the results. Default value is 0
  - `pageSize` - the number of results to be returned per page. Default value is 0

#### GetPaymentsRequest: Get Payments Request
````java
public class PaymentDetailsRequest {
    private String storeId;
    private PageCursor pageCursor;
}

public class PageCursor {
  private int page;
  private int pageSize;
}
````
#### PaymentDetails: Payment Details
````java
public class PaymentDetails {
  private String paymentId;                     // Unique identifier created by Kody
  private String paymentReference;              // payment reference
  private String orderId;                       // Unique order ID generated by Kody
  private String orderMetadata;                 // order related metadata
  private PaymentStatus status;                 // Status of payment
  private String paymentDataJson;               // Optional, json blob for payment data
  private OffsetDateTime dateCreated;           // Timestamp when the payment was created
  private OffsetDateTime datePaid;              // Timestamp when the payment was paid
}
````

#### Java Demo
````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;

PaymentClient paymentClient = new PaymentClient(HOSTNAME, APIKEY);

String storeId = "UUID of assigned store";
PageCursor pageCursor = PageCursor.newBuilder()
        .setPageSize(1)
        .build();

GetPaymentsRequest getPaymentsRequest = GetPaymentsRequest.newBuilder()
        .setStoreId(storeId)
        .setPageCursor(pageCursor)
        .build();

List<PaymentDetails> response = paymentClient.getPayments(getPaymentsRequest);
````

### Refund Online Payment

#### RefundRequest - Refund Payment Request
````java
public class RefundRequest {
  private String storeId;
  private String paymentId;
  private String amount;
}
````

The refund payment request requires the following parameters:

- `storeId` - the ID of your assigned store
- `paymentId` - unique identifier created by Kody
- `amount` - amount as a 2.dp decimal number, such as `"1.00"`

#### RefundResponse : Refund Payment Response
````java
public enum RefundStatus {
    PENDING("PENDING"),
    REQUESTED("REQUESTED"),
    FAILED("FAILED")
}

public class RefundResponse {
    private RefundStatus status;             // Refund status
    private String paymentId;                // Unique identifier created by Kody
    private String failureReason;            // If the refund fails, this will show the reason why
    private OffsetDateTime dateCreated;      // Date when the refund was requested
    private String totalPaidAmount;          // How much was paid for the order
    private String totalAmountRequested;     // The total amount of refunds requested for this order
    private String totalAmountRefunded;      // The total amount of refunds applied to this order
    private String remainingAmount;          // The amount remaining for this order
    private String paymentTransactionId;     // The ID of the payment that needs refunding
}
````

#### Java Demo
````java
import common.PaymentClient;
import com.kodypay.grpc.pay.v1.*;

PaymentClient paymentClient = new PaymentClient(HOSTNAME, APIKEY);

String storeId = "UUID of assigned store"; //STORE_ID
String paymentId = "Unique identifier created by Kody";
String amount = "1.00";

RefundRequest refundRequest = RefundRequest.newBuilder()
        .setStoreId(storeId)
        .setPaymentId(paymentId)
        .setAmount(amount)
        .build();

RefundResponse response = paymentClient.requestOnlineRefund(refundRequest);
````

## More sample code

- Java : https://github.com/KodyPay/kody-clientsdk-java/tree/main/samples/src/main/java/terminal
- Java6 : https://github.com/KodyPay/kody-clientsdk-java6/tree/main/samples/src/main/java/terminal
- Python: https://github.com/KodyPay/kody-clientsdk-python/tree/main/versions/3_12/samples/terminal
- PHP: https://github.com/KodyPay/kody-clientsdk-php/tree/main/samples/php8/pos
- .Net: https://github.com/KodyPay/kody-clientsdk-dotnet/tree/main/samples/ListTerminals,https://github.com/KodyPay/kody-clientsdk-dotnet/tree/main/samples/TerminalPayment 
