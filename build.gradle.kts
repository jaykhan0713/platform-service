plugins {
    java
    jacoco
    `jvm-test-suite`

    //spring
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.0.0"

    //third party
    id("org.sonarqube") version "6.3.1.5724"
}

group = "com.jay.template"
version = "0.0.1-SNAPSHOT"
description = "service-template"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        // keep annotation processors to compile path, and not packaged on runtime classpath
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") // embedded tomcat servlet container
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

    //logback
    implementation(platform("com.eoniantech.build:logback-contrib-bom:0.1.5"))
    implementation("ch.qos.logback.contrib:logback-json-classic")
    implementation("ch.qos.logback.contrib:logback-jackson")

    //micrometer
    implementation("io.micrometer:micrometer-registry-prometheus")

    //OpenAPI
    implementation(platform("org.springdoc:springdoc-openapi-bom:3.0.0"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    //Resilience4j
    implementation(platform("io.github.resilience4j:resilience4j-bom:2.3.0"))
    // no boot4 r4j starter yet. Need for autoconfig of source (yaml) properties
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("io.github.resilience4j:resilience4j-micrometer")

    //IDE mapping such as yml configs with javadocs, generates meta-data json at build time.
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

/* This is an application, not a library.
 * Disable the default <service-name>-SNAPSHOT-"plain" jar (classes-only, non-runnable) so that
 * build/libs contains ONLY the Spring Boot executable jar produced by bootJar.
 * Explicility name to app.jar
 */
tasks.jar { enabled = false }
tasks.bootJar {
    enabled = true
    archiveFileName.set("app.jar")
}

// JUnit
tasks.withType<Test> { //test and functionalTest will use this runner
    useJUnitPlatform()
}

//Spring boot
springBoot {
    mainClass.set("com.jay.template.Starter")
    buildInfo()
}

//Sonar
sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.organization", "jaykhan0713")
        property("sonar.projectKey", "jaykhan0713_service-template")

        // coverage settings
        property("sonar.sources", "src/main/java")
        property("sonar.java.binaries", "build/classes/java/main")

        property("sonar.tests", "src/test/java,src/functionalTest/java")
        property("sonar.java.test.binaries", "build/classes/java/test,build/classes/java/functionalTest")

        property("sonar.junit.reportPaths", "build/test-results/test,build/test-results/functionalTest")
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
    finalizedBy(tasks.jacocoTestReport) // run report after tests
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)

    violationRules {
        rule {
            element = "CLASS"

            includes = listOf(
                "com.jay.template.app.*",
                "com.jay.template.infra.*",
                "com.jay.template.web.*",
                "com.jay.template.core.context.*"
            )

            excludes = listOf(
                //exclude any smoke test related package path.
                "com.jay.template.*.smoke.*",
                "com.jay.template.*.ping.*"
            )

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = 0.75.toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("jacocoTestCoverageVerification")
    dependsOn(tasks.named("functionalTest"))
}

//functional test
testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(project())

                implementation("org.springframework.boot:spring-boot-starter-web")

                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-resttestclient")

                implementation(platform("com.squareup.okhttp3:okhttp-bom:5.2.1"))
                implementation("com.squareup.okhttp3:mockwebserver")
            }

            targets.all {
                testTask.configure {
                    systemProperty("spring.profiles.active", "smoke")
                    shouldRunAfter(tasks.test) // only for <gradlew check> chain, runs after test
                }
            }
        }
    }
}