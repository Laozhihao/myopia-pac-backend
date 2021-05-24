# syntax = docker/dockerfile:experimental

# app builder
FROM maven:3.6-jdk-8-alpine AS app-builder
WORKDIR /build

ARG AWS_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

COPY . .
COPY ./myopia-oauth/oauth-core/config/environment/* /build/myopia-oauth/oauth-core/src/main/resources/

RUN --mount=type=cache,target=/root/.m2 \
    mvn validate package -U -Dmaven.javadoc.skip=true -DskipTests=true

# application
FROM openjdk:8-jre-alpine
WORKDIR /app
EXPOSE 8080

COPY --from=app-builder /build/myopia-oauth/oauth-core/target/myopia-oauth.jar /app/
CMD ["java", "-jar", "/app/myopia-oauth.jar"]
