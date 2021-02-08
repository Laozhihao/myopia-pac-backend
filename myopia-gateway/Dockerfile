# syntax = docker/dockerfile:experimental

# app builder
FROM maven:3.6-jdk-8-alpine AS app-builder
WORKDIR /build

ARG AWS_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

COPY . .
COPY myopia-gateway/setting/env/* /build/myopia-gateway/src/main/resources/

RUN --mount=type=cache,target=/root/.m2 \
    mvn validate package -U -Dmaven.javadoc.skip=true -DskipTests=true

# application
FROM openjdk:8-jre-alpine
WORKDIR /app
EXPOSE 8020

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --no-cache ttf-dejavu

COPY --from=app-builder /build/myopia-gateway/target/myopia-gateway.jar /app/
CMD ["java", "-jar", "/app/myopia-gateway.jar"]
