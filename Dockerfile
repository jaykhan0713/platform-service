# ---- build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Gradle wrapper FIRST so layers cache well
COPY gradlew gradlew.bat ./
COPY gradle/wrapper/ ./gradle/wrapper/

# Build scripts
COPY settings.gradle.kts build.gradle.kts ./

# Sources
COPY src ./src

RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar -x generateGitProperties

# ---- runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]