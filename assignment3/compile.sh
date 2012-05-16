#!/bin/bash

$JAVA_HOME/javac -cp ./jars/plume.jar:./jars/lib.jar:./jars/gson-2.1.jar src/node/facebook/*.java src/node/reliable/*.java src/node/rpc/*.java src/node/twophasecommit/*.java src/settings/*.java src/util/*.java -d classes/

exit
