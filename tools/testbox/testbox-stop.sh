#!/bin/bash
for i in {1..4}
do
   PID=`ps -f | grep 'nimblecoinj-tools' | grep -v 'grep' -m 1 | cut -c 7-11`
   kill -9 $PID
done
