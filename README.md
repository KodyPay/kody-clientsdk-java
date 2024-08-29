# Kody Java gRPC Client

## Description

The Kody Java gRPC Client is an SDK generated from protobuf protocols to facilitate communication with the Kody Payments
Gateway. This library provides a simple and efficient way to integrate Kody payment functionalities into your Java
applications.

## Requirements

- Java 21 or later
- Github account
- Gradle

## Installation

### Gradle

Add the following configuration to your `build.gradle` file:

```kts
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/KodyPay/kody-clientsdk-java-dev/")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.kodypay.api.grpc:kody-clientsdk-java:0.0.1")
}
```

To authenticate with GitHub Packages, you need to create a personal access token with the `read:packages` scope and set it as an environment variable:

```bash
export GITHUB_USERNAME=<your-github-username>
export GITHUB_TOKEN=<your-github-token>
```

Refer to the [GitHub documentation](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens) for more information about the token.

## Samples

Go to the [samples](https://github.com/KodyPay/kody-clientsdk-java/tree/main/samples) directory for examples on how to use the Kody Java gRPC Client.


## License

This project is licensed under the MIT License.
