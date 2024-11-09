#!/bin/bash

export PATH=/usr/bin:/usr/sbin:${PATH}

test -f /tmp/wpa_supplicant.conf || exit 1
sudo cp -f /tmp/wpa_supplicant.conf /etc/wpa_supplicant/wpa_supplicant.conf

sudo chown root:root /etc/wpa_supplicant/wpa_supplicant.conf
sudo chmod a-rwx,g-rwx,u+rw /etc/wpa_supplicant/wpa_supplicant.conf
sudo /usr/bin/systemctl restart wpa_supplicant

sudo ifdown  wlx90de80695700
sudo ifup  wlx90de80695700

sudo /home/pi/iptablenat
sudo netfilter-persistent save