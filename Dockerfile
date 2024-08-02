FROM eclipse-temurin:21.0.4_7-jdk-alpine AS builder
WORKDIR /opt/app
COPY gradle/ gradle
COPY build.gradle settings.gradle gradlew ./
COPY ./src ./src
RUN ./gradlew clean bootJar


FROM eclipse-temurin:21.0.4_7-jre-alpine AS final
WORKDIR /opt/app
COPY --from=builder /opt/app/build/libs/*.jar /opt/app/*.jar
ENTRYPOINT ["java", "-jar", "/opt/app/*.jar"]
