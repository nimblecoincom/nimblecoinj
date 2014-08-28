#!/bin/bash
if [ "$2" != "" ]; then
    STALES_PERIOD='--stales-period='$2
else
    STALES_PERIOD=''
fi
if [ "$3" != "" ]; then
    STALES_MAX='--stales-max='$3
else
    STALES_MAX=''
fi

if [ "$1" != "" ]; then
    ./simple-wallet-tool dump-stales --mode=FULL --net=REGTEST --wallet=data/netbox$1.wallet --chain=data/netbox$1.chain --debuglog $STALES_PERIOD $STALES_MAX
else
    echo 'Usage: dump-stalesX.sh node [stales-period] [stales-max]'
fi
