#! /bin/sh

set -x
docker run -p6379:6379 -d redis
