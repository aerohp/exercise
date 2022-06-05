#!/bin/bash

function usage {
       printf "Usage:\n"
       printf " -h                               Display this help message.\n"
       printf " -b                               Build docker image after VideoStoreClient/VideoStoreService projects built.\n"
       printf " -r                               Run docker-compose to run all dockers of backend service.\n"
       printf " -d                               Run docker-compose in detach mode.\n"
       exit 0
}


while getopts brdh flag
do
    case "${flag}" in
        b) build_docker_image=y;;
        r) run_docker_compose=y;;
        d) detach=-d;;
        h) usage;;
        *) printf "Invalid Option: $1.\n"
           usage;;
    esac
done

echo "build_docker_image = $build_docker_image"
echo "run_docker_compose = $run_docker_compose"
echo "detach = $detach"

echo "***************************************************"
echo "*         Build VideoStoreClient Project          *"
echo "***************************************************"

cd VideoStoreClient
mvn clean compile assembly:single
cd -

if [ ! -d "bin" ]; then
  mkdir ./bin
fi
cp ./VideoStoreClient/target/VideoStoreClient-*.jar ./bin/VideoStoreClient.jar

echo "***************************************************"
echo "*         Build VideoStoreService Project         *"
echo "***************************************************"

cd VideoStoreService
mvn clean package

if [ "$build_docker_image" = "y" ]; then
    echo "***************************************************"
    echo "*         Build VideoStoreService Docker          *"
    echo "***************************************************"
    docker build -t enhanced-videostore-web-app .
fi
cd -

if [ "$run_docker_compose" = "y" ]; then
    echo "***************************************************"
    echo "*                Run Docker Compose               *"
    echo "***************************************************"

    cd docker-compose
    docker-compose -f stack.yml up $detach
fi
cd -  