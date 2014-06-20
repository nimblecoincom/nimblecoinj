ADDR2=`./simple-wallet-tool dump --mode=FULL --net=REGTEST --wallet=data/regtest2.wallet --chain=data/regtest2.chain | grep addr -m 1 | cut -c 8-41`
echo 'Address is'
echo $ADDR2
./simple-wallet-tool send --output=$ADDR2:$1  --mode=FULL --net=REGTEST --wallet=data/regtest1.wallet --chain=data/regtest1.chain --debuglog --server --waitfor=EVER

