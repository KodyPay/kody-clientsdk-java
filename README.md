# Kody API - Java SDK

This guide provides an overview of using the Kody API and its reference documentation.

- [Client Libraries](#client-libraries)
- [Java Installation](#java-installation)
- [Authentication](#authentication)
- [Documentation](#documentation)
- [Sample code](#sample-code)

## Client Libraries

Kody provides client libraries for many popular languages to access the APIs. If your desired programming language is supported by the client libraries, we recommend that you use this option.

Available languages:
- Java: https://github.com/KodyPay/kody-clientsdk-java/
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
- Java client supports JDK 1.8 and above
- Gradle (optional), recommended way to install the SDK

Install the Kody Java Client SDK using the following gradle snippet:

```kts
dependencies {
    implementation("com.kodypay.grpc:kody-clientsdk-java:1.7.30")
}
```
The library can also be downloaded from [here](https://central.sonatype.com/artifact/com.kodypay.grpc/kody-clientsdk-java).

### Import in code

````java
import com.kodypay.grpc.pay.v1.*;
// Or
import com.kodypay.grpc.ecom.v1.*;
// Or
import com.kodypay.grpc.ordering.v1.*;
````

## Authentication

The client library uses a combination of a `Store ID` and an `API key`.
These will be shared with you during the technical integration onboarding or by your Kody contact.

During development, you will have access to a **test Store** and **test API key**, and when the integration is ready for live access, the production credentials will be shared securely with you and associated with a live store that was onboarded on Kody.

The test and live API calls are always compatible, only changing credentials and the service hostname is required to enable the integration in production.

### Host names

- Development and test: `https://grpc-staging.kodypay.com`
- Live: `https://grpc.kodypay.com`

## Documentation

For complete API documentation, examples, and integration guides, please visit:
📚 https://api-docs.kody.com

## Sample code

- Java: https://github.com/KodyPay/kody-clientsdk-java/tree/main/samples
- Python: https://github.com/KodyPay/kody-clientsdk-python/tree/main/versions/3_12/samples
- PHP: https://github.com/KodyPay/kody-clientsdk-php/tree/main/samples
- .Net: https://github.com/KodyPay/kody-clientsdk-dotnet/tree/main/samples
