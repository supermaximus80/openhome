#!/bin/bash

numProcs=$(ps aux | grep java | grep stressTest.properties | wc -l)
echo Total number of XMPP processes = $numProcs

numConnections=$(netstat -a | grep xmpp-client | wc -l)
echo Total number of XMPP connections = $numConnections

