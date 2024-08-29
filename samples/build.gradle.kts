plugins {
    id("java")
}

description = "Kody Java gRPC Client Sample"

val protobufVersion by extra("4.27.3")
val grpcVersion by extra("1.66.0")
val log4jVersion by extra("2.23.0")
val apacheCommonsVersion by extra("3.16.0")

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/KodyPay/kody-clientsdk-java/")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}


dependencies {
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    implementation("org.apache.commons:commons-lang3:$apacheCommonsVersion")
    implementation("com.kodypay.api.grpc:kody-clientsdk-java:0.0.1")
}

