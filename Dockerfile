# ---- builder ----
FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -DskipTests package

# ---- runtime ----
FROM eclipse-temurin:26-jre-alpine
RUN addgroup -S eta && adduser -S eta -G eta
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
USER eta
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
