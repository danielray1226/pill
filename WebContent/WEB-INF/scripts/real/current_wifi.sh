#!/bin/bash

/usr/bin/sudo /usr/sbin/iw dev wlx90de80695700 link | grep SSID | sed 's/.*SSID: //g'