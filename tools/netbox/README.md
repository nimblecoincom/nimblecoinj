# Summary
Testbox has utilities to start 4 nodes in regtest mode.
Node 1 accepts connections (all the other nodes will connect to it)
Nodes 2, 3 and 4 are miners and connect to node 1

# Usage
To initialize the testbox:  
`./reset-110.sh`

To start the testbox:  
`./testbox-start.sh`

To stop the testbox:  
`./testbox-stop.sh`
 
# Utilities
* `dump1.sh`, `dump2.sh`, etc: Display the wallet for that node
* `node1.sh`, `node2.sh`, etc: Start that node
* `reset-0.sh`, `reset-110.sh`, `reset-1058.sh`: Reset blockchain and wallets. Also sets node 1 to have mined 0, 110 and 1058 blocks
* `send*.sh`: Utilities to send coins
