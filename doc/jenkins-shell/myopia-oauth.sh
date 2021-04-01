#!/bin/sh -l
cp ./myopia-oauth/Dockerfile ./Dockerfile

DOCKER_BUILDKIT=1 docker build -t 'wupol/myopia-oauth:1.0' \
        --build-arg AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
        --build-arg AWS_REGION=$AWS_REGION \
        --build-arg AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
        .

docker stop myopia-oauth
docker rm myopia-oauth

{
    docker rmi $(docker images --filter "dangling=true" -q --no-trunc)
} || {
    echo 'Done :)'
}

docker run --name myopia-oauth -p 8010:8010 -d wupol/myopia-oauth:1.0