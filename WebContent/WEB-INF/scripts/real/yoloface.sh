#!/bin/sh

. ~/.venv/bin/activate
dir=$(dirname $0)
exec python3 "$dir/yoloface.py" "$@"
