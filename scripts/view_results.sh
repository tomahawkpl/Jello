#!/bin/bash

# Opens traceview for the selected trace files since last change (see pull_results.sh)
# Name of the directory containing trace files is required


LOCAL_RESULTS_DIR=$1;

if [ ! -d $LOCAL_RESULTS_DIR ] || [ ! -e $LOCAL_RESULTS_DIR/new.tmp ]; then
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

filename=`head -$2 new.tmp | tail -1;`

echo "Launching traceview..."
$TRACEVIEW_BIN $filename > /dev/null 2>&1 &
