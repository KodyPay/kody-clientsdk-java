plugins {
    id("java")
}

description = "Kody Java gRPC Client Sample"

val kodyClientVersion = "1.6.14"
val protobufVersion = "4.27.3"
val grpcVersion = "1.66.0"
val log4jVersion = "2.23.0"
val apacheCommonsVersion = "3.16.0"
val jLineVersion = "3.26.3"

repositories {
    mavenCentral()
    google()
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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