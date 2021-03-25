# syntax = docker/dockerfile:experimental

# app builder
FROM maven:3.6-jdk-8-alpine AS app-builder
WORKDIR /build

ARG AWS_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

COPY ./pom.xml /build/pom.xml
COPY ./base-service/pom.xml /build/base-service/pom.xml
COPY ./myopia-business/pom.xml /build/myopia-business/pom.xml
COPY ./myopia-business/bootstrap/pom.xml /build/myopia-business/bootstrap/pom.xml
COPY ./myopia-business/common/pom.xml /build/myopia-business/common/pom.xml
COPY ./myopia-business/hospital/pom.xml /build/myopia-business/hospital/pom.xml
COPY ./myopia-business/management/pom.xml /build/myopia-business/management/pom.xml
COPY ./myopia-business/parent/pom.xml /build/myopia-business/parent/pom.xml
COPY ./myopia-business/school/pom.xml /build/myopia-business/school/pom.xml
COPY ./myopia-business/screening/pom.xml /build/myopia-business/screening/pom.xml

COPY ./myopia-device/pom.xml /build/myopia-device/pom.xml
COPY ./myopia-gateway/pom.xml /build/myopia-gateway/pom.xml
COPY ./myopia-oauth/pom.xml /build/myopia-oauth/pom.xml

#RUN --mount=type=cache,target=/root/.m2 \
#    mvn dependency:resolve dependency:resolve-plugins dependency:go-offline clean
#RUN mvn dependency:resolve dependency:resolve-plugins dependency:go-offline clean

COPY . .
COPY myopia-business/bootstrap/setting/env/* /build/myopia-business/bootstrap/src/main/resources/

RUN --mount=type=cache,target=/root/.m2 \
    mvn validate package -U -Dmaven.javadoc.skip=true -DskipTests=true

# application
#FROM openjdk:8-jre-alpine
FROM elfolink/alpine-java-wkhtmltopdf
WORKDIR /app
EXPOSE 8020

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --no-cache ttf-dejavu

# install wkhtmltopdf
#RUN apk add --no-cache wkhtmltopdf
# install fonts
#RUN wget -O /usr/share/fonts/simsun.ttf http://mirror.vistel.cn/fonts/simsun.ttf
#RUN wget -O /usr/share/fonts/simsun.ttc http://mirror.vistel.cn/fonts/simsun.ttc

COPY --from=app-builder /build/myopia-business/bootstrap/target/myopia-business.jar /app/
CMD ["java", "-jar", "/app/myopia-business.jar"]
