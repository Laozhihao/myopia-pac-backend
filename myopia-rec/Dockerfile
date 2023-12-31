# syntax = docker/dockerfile:experimental

# app builder
FROM maven:3.6-jdk-8-alpine AS app-builder
WORKDIR /build

ARG AWS_REGION
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

COPY . .
COPY ./myopia-rec/rec-server/config/environment/* /build/myopia-rec/rec-server/src/main/resources/

RUN --mount=type=cache,target=/root/.m2 \
    mvn validate package -U -Dmaven.javadoc.skip=true -DskipTests=true

# application
FROM dops-registry.vistel.cn/devops/workbase-image/jre-8-wine-epic:3.1.2

WORKDIR /app
EXPOSE 8080

COPY --from=app-builder /build/myopia-rec/rec-server/target/myopia-rec.jar /app/
CMD ["java", "-jar", "/app/myopia-rec.jar"]
