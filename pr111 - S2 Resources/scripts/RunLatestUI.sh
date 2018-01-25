#!/bin/bash

export rundir=/Share/Development/Export
export latest=`ls -1t $rundir/pr101_* | head -1`
cd $rundir
/usr/bin/java -jar $latest

