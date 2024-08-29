# Kody Java gRPC Client Sample Project
This project demonstrates how to use the Kody Java gRPC client to communicate with Kody payments gateway.

## Requirements
- Java 21 or later
- Github account
- Gradle

## Installation

### Environment Variables
To authenticate with GitHub Packages, you need to create a personal access token with the `read:packages` scope and set it as an environment variable:

```bash
export GITHUB_USERNAME=<your-github-username>
export GITHUB_TOKEN=<your-github-token>
```

### Gradle
Build the samples subproject with gradle:

```bash
../gradlew :samples:build   
```

### Configuration

Update the `config.properties` file with your `apiKey` and `storeId`.

```properties
address=https://grpc.kodypay.com
apiKey=Put your API key here
storeId=Use your Kody store ID
```

## Running the Example
Below are the available examples you can find in the `samples` subproject:
- Online payments
  - `EcomBlockingJavaClient` 
  - `EcomNonBlockingJavaClient` 
- Terminal payment
  - `TerminalBlockingJavaClient` 
  - `TerminalNonBlockingJavaClient` 

## Troubleshooting

If you encounter issues, ensure:

- Both main project and subproject `samples` build successfully
- Your `config.properties` is correctly filled out.
- Make sure you have the correct `GITHUB_TOKEN` environment variable set.
- Contact Kody support or tech team
