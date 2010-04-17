#!/bin/bash

# Lists tracefiles (see pull_results.sh)
# Name of the directory containing trace files is required


LOCAL_RESULTS_DIR=$1;

if [ ! -d $LOCAL_RESULTS_DIR ] || [ "`ls $LOCAL_RESULTS_DIR/*.trace  | wc -l`" = "0" ]; then
	echo "No trace files";
	exit 1;
fi

which traceview > /dev/null;

if [ $? -eq 1 ]; then
	echo "'traceview' not in PATH";
	exit 1;
fi

TRACEVIEW_BIN=`readlink -f \`which traceview\``

cd $LOCAL_RESULTS_DIR;

echo "Trace files:";

ls *.trace | awk '// { print NR,$0}'
