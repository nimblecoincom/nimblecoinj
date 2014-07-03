java -Djava.util.logging.config.file=testbox-node1-logging.properties -server -jar ../target/nimblecoinj-tools-*.jar none --mode=FULL --net=REGTEST --wallet=data/regtest1.wallet --chain=data/regtest1.chain --debuglog --server --txgen-rate=1 --waitfor=EVER &
java -Djava.util.logging.config.file=testbox-node2-logging.properties -server -jar ../target/nimblecoinj-tools-*.jar sync --mode=FULL --net=REGTEST --wallet=data/regtest2.wallet --chain=data/regtest2.chain --debuglog --miner --waitfor=EVER &
java -Djava.util.logging.config.file=testbox-node3-logging.properties -server -jar ../target/nimblecoinj-tools-*.jar sync --mode=FULL --net=REGTEST --wallet=data/regtest3.wallet --chain=data/regtest3.chain --debuglog --miner --waitfor=EVER &
java -Djava.util.logging.config.file=testbox-node4-logging.properties -server -jar ../target/nimblecoinj-tools-*.jar sync --mode=FULL --net=REGTEST --wallet=data/regtest4.wallet --chain=data/regtest4.chain --debuglog --miner --waitfor=EVER &
