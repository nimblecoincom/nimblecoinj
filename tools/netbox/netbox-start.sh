#!/bin/bash
if [ "$1" != "" ]; then
    NODES=$1
else
    NODES=4
fi
echo "NODES is $NODES"
if [ "$2" != "" ]; then
    PEERS=$2
else
    PEERS=2
fi
echo "PEERS is $PEERS"
if [ "$3" != "" ]; then
    MINERS=$3
else
    MINERS=2
fi
echo "MINERS is $MINERS"
PORT=19000
for i in $(seq 1 $NODES)
do
  PORT=$((PORT + 1))  
  if test $i -le $MINERS
  then
    java -Djava.util.logging.config.file=file-logging.properties -server -jar ../target/nimblecoinj-tools-*.jar sync --mode=FULL --net=REGTEST --wallet=data/regtest$i.wallet --chain=data/regtest$i.chain --debuglog --server --server-port=$PORT --netbox --netbox-nodes=$NODES --netbox-peers=$PEERS --miner --miner-emulate=$MINERS --waitfor=EVER &
  else
    java -Djava.util.logging.config.file=file-logging.properties -server -jar ../target/nimblecoinj-tools-*.jar sync --mode=FULL --net=REGTEST --wallet=data/regtest$i.wallet --chain=data/regtest$i.chain --debuglog --server --server-port=$PORT --netbox --netbox-nodes=$NODES --netbox-peers=$PEERS --waitfor=EVER &
  fi
done
