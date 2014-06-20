rm data/regtest*
./simple-wallet-tool create --mode=FULL --net=REGTEST --wallet=data/regtest1.wallet --chain=data/regtest1.chain --debuglog 
./simple-wallet-tool add-key --mode=FULL --net=REGTEST --wallet=data/regtest1.wallet --chain=data/regtest1.chain --debuglog 
./simple-wallet-tool create --mode=FULL --net=REGTEST --wallet=data/regtest2.wallet --chain=data/regtest2.chain --debuglog 
./simple-wallet-tool add-key --mode=FULL --net=REGTEST --wallet=data/regtest2.wallet --chain=data/regtest2.chain --debuglog 
./simple-wallet-tool create --mode=FULL --net=REGTEST --wallet=data/regtest3.wallet --chain=data/regtest3.chain --debuglog 
./simple-wallet-tool add-key --mode=FULL --net=REGTEST --wallet=data/regtest3.wallet --chain=data/regtest3.chain --debuglog 
./simple-wallet-tool create --mode=FULL --net=REGTEST --wallet=data/regtest4.wallet --chain=data/regtest4.chain --debuglog 
./simple-wallet-tool add-key --mode=FULL --net=REGTEST --wallet=data/regtest4.wallet --chain=data/regtest4.chain --debuglog 
