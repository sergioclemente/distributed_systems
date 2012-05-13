#!/bin/bash

$JAVA_HOME/java -cp src/:jars/plume.jar:jars/lib.jar:jars/gson-2.1.jar:classes/ edu.washington.cs.cse490h.lib.MessageLayer -s -n node.facebook.FacebookRPCNode 

exit
