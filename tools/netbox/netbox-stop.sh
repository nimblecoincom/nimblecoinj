#!/bin/bash
if [ "$1" != "" ]; then
    NODES=$1
else
    NODES=4
fi
echo "NODES is $NODES"
for i in $(seq 1 $NODES)
do
   PID=`ps -f | grep 'nimblecoinj-tools' | grep -v 'grep' -m 1 | cut -c 7-11`
   kill -9 $PID
done
