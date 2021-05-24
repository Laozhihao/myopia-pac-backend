#!/bin/sh -l

cp ./myopia-business/Dockerfile ./Dockerfile

DOCKER_BUILDKIT=1 docker build -t 'wupol/myopia-business:1.0' \
        --build-arg AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
        --build-arg AWS_REGION=$AWS_REGION \
        --build-arg AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
        .

docker stop myopia-business
docker rm myopia-business

{
    docker rmi $(docker images --filter "dangling=true" -q --no-trunc)
} || {
    echo 'Done :)'
}

docker run --name myopia-business -p 8020:8020 --env AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID --env AWS_REGION=$AWS_REGION --env AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY -d wupol/myopia-business:1.0