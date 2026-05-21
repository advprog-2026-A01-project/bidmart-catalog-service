FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src
COPY config config
RUN chmod +x gradlew && \
    ./gradlew dependencies --configuration compileClasspath -x test -x generateProto 2>/dev/null || true && \
    find /root/.gradle -name "*.exe" -exec chmod +x {} \; && \
    ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8082
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]