#!/bin/sh -l
cp ./myopia-rec/Dockerfile ./Dockerfile

DOCKER_BUILDKIT=1 docker build -t 'wupol/myopia-rec:1.0' \
        --build-arg AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
        --build-arg AWS_REGION=$AWS_REGION \
        --build-arg AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
        .

docker stop myopia-rec
docker rm myopia-rec

{
    docker rmi $(docker images --filter "dangling=true" -q --no-trunc)
} || {
    echo 'Done :)'
}

docker run --name myopia-rec -p 8040:8040 -d wupol/myopia-rec:1.0