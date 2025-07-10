plugins {
    java
    alias(libs.plugins.quarkus)
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.platform.bom))
    implementation("io.quarkus:quarkus-vertx")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-arc")
//    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-reactive-routes")
    implementation("io.vertx:vertx-web-client")
    implementation("io.vertx:vertx-auth-jwt")

    implementation(libs.commons.codes)

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.quarkus:quarkus-junit5")
}

group = "net.ximatai.muyun"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
