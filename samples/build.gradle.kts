plugins {
    id("java")
}

description = "Kody Java gRPC Client Sample"

val kodyClientVersion by extra("0.0.1")

val protobufVersion by extra("4.27.3")
val grpcVersion by extra("1.66.0")
val log4jVersion by extra("2.23.0")
val apacheCommonsVersion by extra("3.16.0")
val jLineVersion by extra("3.26.3")

repositories {
    mavenCentral()
    google()
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}


dependencies {
    implementation("io.grpc:grpc-core:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    implementation("org.apache.commons:commons-lang3:$apacheCommonsVersion")
    implementation("com.kodypay.grpc:kody-clientsdk-java:$kodyClientVersion")
    implementation("org.jline:jline:$jLineVersion")
}


tasks.jar {
    manifest {
        attributes["Main-Class"] = "cli.KodyDemo"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}