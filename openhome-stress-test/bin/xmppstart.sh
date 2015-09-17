#!/bin/bash
numProcs=$(ps aux | grep java | grep stressTest.properties | wc -l)
if [ $numProcs -gt 0 ];
then
	echo "Load test process already running"
	exit 1
fi

echo "starting test batch 1"
cd arist;
nohup ../bin/start-stress-test.sh >& test.log &
sleep 2
echo "starting test batch 2"
cd ../arist2;
nohup ../bin/start-stress-test.sh >& test.log &
sleep 2
echo "starting test batch 3"
cd ../arist3;
nohup ../bin/start-stress-test.sh >& test.log &
sleep 2




