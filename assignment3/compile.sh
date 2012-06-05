#!/bin/bash

$JAVA_HOME/javac -Xlint:unchecked -cp ./jars/plume.jar:./jars/lib.jar:./jars/gson-2.1.jar src/node/reliable/*.java src/node/rpc/*.java src/node/rpc/paxos/*.java src/node/storage/*.java src/paxos/*.java src/settings/*.java src/util/*.java -d classes/

exit
