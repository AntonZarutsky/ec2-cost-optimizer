#!/usr/bin/env bash

mvn clean install
./prepare-scm-source.sh

export image_version=$(./version.sh)
export image_name="...
export docker_image=${image_name}:${image_version}

echo "Building ${docker_image}"
docker build -t ${docker_image} .

echo "Pushing ${docker_image}"
docker push ${docker_image}