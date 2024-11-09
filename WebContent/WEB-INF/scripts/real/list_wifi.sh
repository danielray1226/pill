#!/bin/bash

/usr/bin/sudo /usr/sbin/iwlist wlan0 scan | grep ESSID | grep -v 'ESSID:""' | sort -u | sed 's/.*ESSID:"\(.*\)"/\1/'