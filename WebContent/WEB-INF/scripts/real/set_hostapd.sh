#!/bin/bash

export PATH=/usr/bin:/usr/sbin:${PATH}

test -f /tmp/hostapd.conf || exit 1

sudo cp -f /tmp/hostapd.conf /etc/hostapd/hostapd.conf

sudo chown root:root /etc/hostapd/hostapd.conf
sudo chmod a-rwx,g-rwx,u+rw /etc/hostapd/hostapd.conf
sudo /usr/bin/systemctl restart hostapd
