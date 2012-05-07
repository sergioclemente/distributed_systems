#!/usr/bin/perl

# Simple script to start a Node Manager that uses a compiled lib.jar

main();

sub main {
    
    $classpath = "src/:jars/plume.jar:jars/lib.jar:classes/";
    
    $args = join " ", @ARGV;

    exec("java -cp $classpath edu.washington.cs.cse490h.lib.MessageLayer $args");
}

