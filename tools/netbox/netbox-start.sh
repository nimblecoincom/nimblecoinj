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
DEBUG_PORT=20000
rm data/*
rm logs/*
for i in $(seq 1 $NODES)
do
 ./simple-wallet-tool create --mode=FULL --net=NETBOX --wallet=data/netbox$i.wallet --chain=data/netbox$i.chain --debuglog 
 ./simple-wallet-tool add-key --mode=FULL --net=NETBOX --wallet=data/netbox$i.wallet --chain=data/netbox$i.chain --debuglog 
done
for i in $(seq 1 $NODES)
do
  PORT=$((PORT + 1))  
  DEBUG_PORT=$((DEBUG_PORT + 1))  
  if test $i -le $MINERS
  then
    MINER_PARAMS='--miner --miner-emulate='$MINERS
  else
    MINER_PARAMS=''
  fi
#  if test $i -eq 5
#  then
#    DEBUG_PARAMS='-agentlib:jdwp=transport=dt_socket,address=localhost:'$DEBUG_PORT',server=n,suspend=n'
#  else
    DEBUG_PARAMS='-agentlib:jdwp=transport=dt_socket,address='$DEBUG_PORT',server=y,suspend=n'
#  fi 
  java -Xdebug $DEBUG_PARAMS -Djava.util.logging.config.file=file-logging.properties -server -jar ../target/nimblecoinj-tools-*.jar sync --mode=FULL --net=NETBOX --wallet=data/netbox$i.wallet --chain=data/netbox$i.chain --debuglog --server --server-port=$PORT --accept-udp --netbox-nodes=$NODES --netbox-peers=$PEERS $MINER_PARAMS --waitfor=EVER &
  sleep 0.3
done
