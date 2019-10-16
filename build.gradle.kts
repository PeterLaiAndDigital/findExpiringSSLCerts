import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    val kotlinVersion = "1.3.50"
    kotlin("jvm") version "1.3.50"

}

repositories {
    mavenCentral()
    jcenter()
}


application {
    mainClassName = "findExpiringSSLCerts.FindExpiringSSLCertKt"
}

dependencies {
    compile(kotlin("stdlib"))
    implementation ("software.amazon.awssdk:aws-sdk-java:2.9.19")
    implementation ("software.amazon.awssdk:kinesis:2.9.19")
    testImplementation ("junit:junit:4.11")
    implementation(kotlin("stdlib-jdk8"))
}


val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}