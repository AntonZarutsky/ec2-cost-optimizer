#!/usr/bin/env bash


export image_version=$(./version.sh)
export image_name="..."

export mint_bucket="..."
export scalyr_key="..."
export ssl_certificate_id="..."
export appd="Feed"

echo "Deploying ${docker_image}"
echo "senza create live.yaml ${image_version} ${image_version} ${mint_bucket} ${scalyr_key} ${ssl_certificate_id} ${appd}"
senza create live.yaml ${image_version} ${image_version} ${mint_bucket} ${scalyr_key} ${ssl_certificate_id} ${appd}
