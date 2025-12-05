plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("org.sonarqube") version "6.3.1.5724"
    jacoco
}

group = "com.jay.showcase"
version = "0.0.1-SNAPSHOT"
description = "service-template"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //logback
    implementation(platform("com.eoniantech.build:logback-contrib-bom:0.1.5"))
    implementation("ch.qos.logback.contrib:logback-json-classic")
    implementation("ch.qos.logback.contrib:logback-jackson")

    // micrometer tracing with brave
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// JUnit
tasks.withType<Test> {
    useJUnitPlatform()
}

//Spring boot
springBoot {
    mainClass.set("com.jay.showcase.Starter")
    buildInfo()
}

//Sonar
sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "jaykhan0713")
        property("sonar.projectKey", "jaykhan0713_service-template")

        // coverage config
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
        property("sonar.java.binaries", "build/classes/java/main")
        property("sonar.java.test.binaries", "build/classes/java/test")
        property("sonar.junit.reportPaths", "build/test-results/test")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/test/jacocoTestReport.xml"
        )
    }
}

//Jacoco
jacoco {
    toolVersion = "0.8.14"
}

tasks.test {
    useJUnitPlatform()

    finalizedBy(tasks.jacocoTestReport) // run report after tests
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)  // for local
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/Starter.class"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.75.toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("jacocoTestCoverageVerification")
}