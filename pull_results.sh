#!/bin/bash

# Pulls performance test results from the device. Only the results which have changed are downloaded.

PHONE_RESULTS_DIR="jello";
LOCAL_RESULTS_DIR=".tests";

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

echo -n "Getting trace files from the device... ";

echo "cd /sdcard && cd $PHONE_RESULTS_DIR && md5sum *.trace > sums.tmp; exit;" | adb shell > /dev/null;


adb pull /sdcard/$PHONE_RESULTS_DIR/sums.tmp phone_sums.tmp > /dev/null 2>&1;

if [ ! -e sums.tmp ]; then
	echo "FAILED (maybe there are none)";
	exit 1
fi

lines=`awk ' END { print NR } ' phone_sums.tmp`
echo "$lines file(s)";

# Get a list of local files and their md5s

md5sum *.trace > local_sums.tmp 2> /dev/null

lines=`awk ' END { print NR } ' local_sums.tmp`
echo "$lines local trace file(s) found";

# Save the list of changed files

cat local_sums.tmp phone_sums.tmp | uniq -u | uniq -f 1 > changed.tmp

lines=`awk ' END { print NR } ' changed.tmp`
echo "$lines file(s) changed";


# Download changed files

if [ $lines != "0" ]; then
	awk "//{print \$2}" changed.tmp > new.tmp
fi;

rm local_sums.tmp phone_sums.tmp changed.tmp

if [ $lines != "0" ]; then
	for file in `cat new.tmp`; do
		echo -n "Downloading file $file... "
		adb pull /sdcard/$PHONE_RESULTS_DIR/$file . > /dev/null 2>&1;
		if [ $? -eq 0 ]; then
			echo -en "\\033[1;32m";
			echo "OK";
			echo -en "\\033[0;37m";
		else
			echo -en "\\033[1;31m";
			echo "FAILED";
			echo -en "\\033[0;37m";
		fi;
	done;
fi;
