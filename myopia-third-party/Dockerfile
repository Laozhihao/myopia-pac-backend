# syntax = docker/dockerfile:experimental

# app builder
FROM maven:3.6-jdk-8-alpine AS app-builder
WORKDIR /build

ARG AWS_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

COPY . .
COPY ./myopia-third-party/third-party-server/config/environment/* /build/myopia-third-party/third-party-server/src/main/resources/

RUN --mount=type=cache,target=/root/.m2 \
    mvn validate package -U -Dmaven.javadoc.skip=true -DskipTests=true

# application
FROM openjdk:8-jre-alpine
WORKDIR /app
EXPOSE 8080

COPY --from=app-builder /build/myopia-third-party/third-party-server/target/myopia-third-party.jar /app/
CMD ["java", "-jar", "/app/myopia-third-party.jar"]
