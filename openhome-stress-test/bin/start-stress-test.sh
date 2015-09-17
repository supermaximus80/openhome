#!/bin/sh

# Required directory layout
# bin/start-stress-test.sh
# lib/*.jar (including openhome-stress-test.jar)
# config/stressTest.properties
# To start test, goto testdir (containing stressTest.properties) and type ../bin/start-stress-test.sh >& stress-test.log &
#


icdate=`date +%F.%H-%M-%S`

SharedLib="lib"

current_dir=$(pwd)
echo ${current_dir}

cd ../${SharedLib}

AllJars=( `ls -1 *.jar` )

#echo $AllJars

function CreateCP()
{
        for file in ${AllJars[*]}
        do
                if [ -f "${file}" ];then
                        #echo "${file}"
                        if [ "$CP" ];then
                                CP="${CP}:${file}"
                        else
                                CP="${file}"

                        fi
                fi
        done
        CP="${CP}:${current_dir}:."
}



CreateCP

CMD="com.icontrol.openhomestresstest.StressTestSchedular ${current_dir}/stressTest.properties"

java -Xms500m -Xmx1200m -XX:PermSize=100m -Xss128k -XX:MaxPermSize=200m -XX:+HeapDumpOnOutOfMemoryError -cp $CP $CMD


