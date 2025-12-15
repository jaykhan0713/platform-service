plugins {
    java
    jacoco
    `jvm-test-suite`

    //spring
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.0.0"

    //third party
    id("com.gorylenko.gradle-git-properties") version "2.5.4"
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // tracing for micrometer with OpenTelemetry
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

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

        // coverage config
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
                    "**/*Config.class",
                    "**/*Properties.class",
                    "**/Starter.class",

                    // OpenAPI contract surface (DTOs, error models, annotations)
                    "**/com/jay/template/api/**"
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
    dependsOn(tasks.named("functionalTest"))
}

//functional test
testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-test")
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