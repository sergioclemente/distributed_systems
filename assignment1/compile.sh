#!/bin/bash

javac -cp ./jars/plume.jar:./jars/lib.jar src/node/facebook/*.java src/node/reliable/*.java src/node/rpc/*.java src/node/storage/*.java src/storage/*.java src/util/*.java -d classes/

exit
