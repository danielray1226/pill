#!/bin/bash

/usr/bin/sudo /usr/sbin/ip -f inet addr show wlx90de80695700 | sed -En -e 's/.*inet ([0-9.]+).*/\1/p'
