plugins {
    java
    id("net.serenity-bdd.serenity-gradle-plugin") version "5.3.9"
    id("io.gatling.gradle") version "3.14.3"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

dependencies {
    testImplementation(platform("net.serenity-bdd:serenity-bom:5.3.10"))
    testImplementation("net.serenity-bdd:serenity-core")
    testImplementation("net.serenity-bdd:serenity-screenplay")
    testImplementation("net.serenity-bdd:serenity-screenplay-webdriver")
    testImplementation("net.serenity-bdd:serenity-cucumber")
    testImplementation("io.github.bonigarcia:webdrivermanager:6.1.0")
    testImplementation("net.serenity-bdd:serenity-rest-assured")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.junit.platform:junit-platform-suite:1.14.3")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.34.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.14.3")
    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    include("**/*Suite.class")
    outputs.upToDateWhen { false }
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags", "not @wip"))
    System.getProperty("channel")?.let { systemProperty("channel", it) }
    doFirst { delete(layout.projectDirectory.dir("target/site/serenity")) }
    finalizedBy("aggregate")
}
