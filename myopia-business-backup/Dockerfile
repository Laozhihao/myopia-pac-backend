# syntax = docker/dockerfile:experimental

# app builder
FROM maven:3.6-jdk-8-alpine AS app-builder
WORKDIR /build

ARG AWS_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

COPY . .
COPY myopia-business/bootstrap/config/environment/* /build/myopia-business/bootstrap/src/main/resources/

RUN --mount=type=cache,target=/root/.m2 \
    mvn validate package -U -Dmaven.javadoc.skip=true -DskipTests=true

# application
FROM elfolink/alpine-java-wkhtmltopdf:3.12
WORKDIR /app
EXPOSE 8080

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --no-cache ttf-dejavu

# install fonts
RUN wget -O /usr/share/fonts/simsun.ttf http://mirror.vistel.cn/fonts/simsun.ttf
RUN wget -O /usr/share/fonts/simsun.ttc http://mirror.vistel.cn/fonts/simsun.ttc

COPY --from=app-builder /build/myopia-business/bootstrap/target/myopia-business.jar /app/
CMD ["java", "-jar", "/app/myopia-business.jar"]
