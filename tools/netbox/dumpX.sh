#!/bin/bash
if [ "$1" != "" ]; then
	./simple-wallet-tool dump --mode=FULL --net=REGTEST --wallet=data/regtest$1.wallet --chain=data/regtest$1.chain --debuglog
else
    echo 'Usage: dumpX.sh node'
fi

