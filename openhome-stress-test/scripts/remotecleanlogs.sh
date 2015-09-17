#!/bin/bash
if [ $# -lt 1 ];
then
	echo "$0 : Usage $0 username"
	exit 1
fi
username=$1

#loadtest2-9
hostnamebase="loadtest"
for ((j=2;j<10;j++))
do
echo Deleting ~/logs/*.* from $hostnamebase$j
ssh -o StrictHostKeyChecking=no $username@$hostnamebase$j "rm -f ~/logs/*.*"
done

#jmeter1/2
hostnamebase="jmeter"
for ((j=1;j<3;j++))
do
echo Deleting ~/logs/*.* from $hostnamebase$j
ssh -o StrictHostKeyChecking=no $username@$hostnamebase$j "rm -f ~/logs/*.*"
done



