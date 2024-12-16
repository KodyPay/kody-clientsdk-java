# Kody Java gRPC Client Sample Project
This project demonstrates how to use the Kody Java gRPC client to communicate with Kody payments gateway.

## Requirements
- Java 17 or later
- Gradle

## Installation

The commands below assume that your current working directory is `samples`.

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
  - `EcomAsyncJavaClient` 
- Terminal payment
  - `TerminalJavaClient` 

For your convenience, there is also a CLI with limited capabilities, which you can run as
```shell
java -jar build/libs/samples-*
```


## Troubleshooting

If you encounter issues, ensure:

- Both main project and subproject `samples` build successfully
- Your `config.properties` is correctly filled out.
- Contact Kody support or tech team
