#!/bin/bash

# Opens traceview for the selected trace files since last change (see pull_results.sh)


LOCAL_RESULTS_DIR="test_results";

if [ ! -d $LOCAL_RESULTS_DIR ] || [ ! -e $LOCAL_RESULTS_DIR/new.tmp ]; then
	echo "No trace files";
fi

which traceview > /dev/null;

if [ $? -eq 1 ]; then
	echo "'traceview' not in PATH";
	exit 1;
fi

TRACEVIEW_BIN=`readlink -f \`which traceview\``

cd $LOCAL_RESULTS_DIR;

echo "New trace files:";

awk '// { print NR,$0}' new.tmp

echo -n "Select trace file: ";
read line;

filename=`head -$line new.tmp | tail -1;`

echo "Launching traceview..."
$TRACEVIEW_BIN $filename &
