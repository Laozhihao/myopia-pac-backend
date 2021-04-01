#!/bin/sh -l

cp ./myopia-gateway/Dockerfile ./Dockerfile

DOCKER_BUILDKIT=1 docker build -t 'wupol/myopia-gateway:1.0' \
        --build-arg AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
        --build-arg AWS_REGION=$AWS_REGION \
        --build-arg AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
        .

docker stop myopia-gateway
docker rm myopia-gateway

{
    docker rmi $(docker images --filter "dangling=true" -q --no-trunc)
} || {
    echo 'Done :)'
}

docker run --name myopia-gateway -p 8000:8000 -d wupol/myopia-gateway:1.0