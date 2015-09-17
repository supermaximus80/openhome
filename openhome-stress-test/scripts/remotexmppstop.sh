#!/bin/bash
if [ $# -lt 3 ];
then
	echo "$0 : Usage $0 username startIndex endIndex"
	exit 1
fi
username=$1
start=$2
end=$3

#loadtest2-9
echo starting load test from $start to $end
hostnamebase="loadtest"
for ((j=$start;j<=$end;j++))
do
echo Connecting to $hostnamebase$j
ssh -t -o StrictHostKeyChecking=no $username@$hostnamebase$j "sudo nohup ~/bin/xmppstop.sh"
done





