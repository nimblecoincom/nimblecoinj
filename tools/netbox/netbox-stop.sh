#!/bin/bash
if [ "$1" != "" ]; then
    N=$1
else
    N=4
fi
echo "N is $N"
for i in $(seq 1 $N)
do
   PID=`ps -f | grep 'nimblecoinj-tools' | grep -v 'grep' -m 1 | cut -c 7-11`
   kill -9 $PID
done
