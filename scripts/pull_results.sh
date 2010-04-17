#!/bin/bash

# Pulls performance test results from the device. Only the results which have changed are downloaded.
# Names of the directories containing trace files on the phone and locally are required

PHONE_RESULTS_DIR=$1;
LOCAL_RESULTS_DIR=$2;

if [ ! -d $LOCAL_RESULTS_DIR ]; then
	mkdir $LOCAL_RESULTS_DIR;
	if [ $? != 0 ]; then
		echo "Couldn't create local trace files directory, edit this script and set a writable location";
		exit 1;
	fi
fi

which adb > /dev/null;

if [ $? -eq 1 ]; then
	echo "'adb' not in PATH";
	exit 1;
fi


if [ "List of devices attached" = "`adb devices`" ] ; then
	echo "No devices/emulators available";
	exit 1;
fi


# Get a list of files and their md5s from the phone

cd $LOCAL_RESULTS_DIR;

rm *.trace 2> /dev/null

echo -n "Getting trace files from the device... ";

echo "cd $PHONE_RESULTS_DIR && ls *.trace > traces.tmp; exit;" | adb shell > /dev/null;


adb pull $PHONE_RESULTS_DIR/traces.tmp traces.tmp > /dev/null 2>&1;

if [ ! -e traces.tmp ]; then
	echo "FAILED (maybe there are none)";
	exit 1
fi

lines=`awk ' END { print NR } ' traces.tmp`
echo "$lines traces found on device";

if [ $lines != "0" ]; then
	for file in `cat traces.tmp`; do
		echo -n "Downloading file $file... "
		adb pull $PHONE_RESULTS_DIR/$file . > /dev/null 2>&1;
		if [ $? -eq 0 ]; then
			echo "OK";
		else
			echo "FAILED";
		fi;
	done;
fi;

rm traces.tmp

# Removing file on the device

echo "Removing trace files from the device...";
echo "cd $PHONE_RESULTS_DIR && rm *.trace; rm traces.tmp; exit;" | adb shell > /dev/null;
