#!/bin/bash
numjava=$(ps aux | grep java | grep stressTest.properties | wc -l)
if [ $numjava -gt 0 ]
then
	for pid in $(ps aux | grep java | awk '/stressTest.properties/ {print $2}')
	do
		echo Stopping pid $pid
		kill -9 $pid
	done
else
	echo No XMPP LoadTest Java process found
fi
exit 0



