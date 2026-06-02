#!/bin/bash

build_and_push_image() {
  tag=$1
  svc_name=${tag%%.*}
  path=${svc_name}
  version=${tag#"${svc_name}."}
  if echo "$svc_name" | grep -q "dhforce-ai"; then
    path="${svc_name}"
  else
    path="${svc_name}-bootstrap"
  fi
  tag=$(basename "$svc_name")
  docker build -t "$IMG_PREFIX/${tag}:${version}" -f "${path}/Dockerfile" .
  docker push "$IMG_PREFIX/${tag}:${version}"
  docker tag "$IMG_PREFIX/${tag}:${version}" "$IMG_PREFIX/${tag}:latest"
  docker push "$IMG_PREFIX/${tag}:latest"
}

tag=$1

build_and_push_image "${tag}"
