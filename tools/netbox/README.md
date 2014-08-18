# Summary
Netbox has utilities to start N nodes in regtest mode.

To start the netbox:  
`./netbox-start.sh` [NODES] [PEERS] [MINERS
OPTIONS
  NODES The number of nodes to start. Default=4
  PEERS The number of peers each node should have a connection to. Default=2
  MINERS The number of nodes who are miners. Default=2

To stop the netbox:  
`./netbox-stop.sh`
 
# Utilities
* `dumpX.sh` NODE: Display the wallet for that node
* `dump-stalesX.sh` NODE: Display the number of stales for that node
