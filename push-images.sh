#!/bin/bash

#
# 推送Beta版本
#

version=1.0.0

usage() {
    echo """
    Usage: sh script.sh [all | demo ]
    """
    exit 1
}

# demo
function demo() {
  docker rmi -f registry.cn-beijing.aliyuncs.com/yunli-bigdata/eventbus-sdk-demo:${version}
  docker tag eventbus-sdk-demo:${version} registry.cn-beijing.aliyuncs.com/yunli-bigdata/eventbus-sdk-demo:${version}
  docker push registry.cn-beijing.aliyuncs.com/yunli-bigdata/eventbus-sdk-demo:${version}
}

case "$1" in
  "all")
    demo
    ;;
  "demo")
    demo
    ;;
  *)
    usage
    ;;
esac