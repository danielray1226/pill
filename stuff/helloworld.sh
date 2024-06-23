#!/bin/bash

while true
do
	read command 
	command="$command $(date)"
	echo $command
done	
