import com.google.protobuf.gradle.*
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.4"
    id("nebula.release") version "19.0.10"
    id("com.vanniktech.maven.publish") version "0.28.0"
}

description = "Kody Java gRPC Client"
group = "com.kodypay.grpc"

val protobufVersion = "4.27.3"
val grpcVersion = "1.66.0"
val annotationsVersion = "6.0.53"

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}


dependencies {
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    compileOnly("org.apache.tomcat:annotations-api:$annotationsVersion")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                java {}
            }
            task.plugins {
                id("grpc") {
                    outputSubDir = "java"
                }
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "kody-clientsdk-java"
        description = "Kody Java gRPC Client"
        url = "https://github.com/KodyPay/kody-clientsdk-java"
        inceptionYear = "2024"
        licenses{
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "kodypay"
                name = "Kody"
            }
        }
        scm {
            url = "https://github.com/KodyPay/kody-clientsdk-java"
            connection = "scm:git:git://github.com/KodyPay/kody-clientsdk-java.git"
            developerConnection = "scm:git:ssh://github.com/KodyPay/kody-clientsdk-java.git"
        }

        signAllPublications()
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    }
}

tasks.withType<PublishToMavenRepository> {
    mustRunAfter(rootProject.tasks.findByName("release"))
    rootProject.tasks.findByName("postRelease")?.dependsOn(this)
}