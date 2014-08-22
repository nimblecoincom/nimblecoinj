package com.google.bitcoin.net.discovery;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Discovery methos for net in a box tests
 * Nodes to connect to are calulated based on the number of nodes, how many connections each node should have and
 * which node is this.
 * The process is deterministic so every node gets the same graph, but tries to emulate a random graph
 * @author Oscar Guindzberg
 *
 */
public class NetboxDiscovery implements PeerDiscovery {

    // The number of the nodes to connect to
    List<Integer> nodesToConnectTo;
    // Base port for net in a box test. Nodes should accept connections on port 19001, 19002, etc.
    private final static int BASE_NETBOX_PORT = 19000;

    public NetboxDiscovery(int netboxNodes, int netboxPeers, int selfPort) {
        int selfNode = selfPort - BASE_NETBOX_PORT;

        // First lets build the entire graph 
        
        // Random with harcoded seed to generate always the same result.
        Random random = new Random(1);
        // The graph of connections (not just from/to this node, all the connections in the network) 
        List<NodeConnection> connections = new ArrayList<NodeConnection>();
        //key=node number, value=number of connections from/to the node
        Map<Integer, Integer> numberOfConnectionsPerNode = new HashMap<Integer, Integer>();
        //A list of nodes candidates for a new connection
        List<Integer> nodesWithNotEnoughConnections = new ArrayList<Integer>();
        

        do {         
            // Initialize numberOfConnectionsPerNode with 0
            for (int i = 0; i < netboxNodes; i++) {
                numberOfConnectionsPerNode.put(i+1, 0);
            }
            // Populate nodesWithNotEnoughConnections with information from already decided connections
            for (NodeConnection connection : connections) {
                numberOfConnectionsPerNode.put(connection.from, numberOfConnectionsPerNode.get(connection.from)+1);
                numberOfConnectionsPerNode.put(connection.to, numberOfConnectionsPerNode.get(connection.to)+1);            
            }
            // Add items to nodesWithNotEnoughConnections based on numberOfConnectionsPerNode
            nodesWithNotEnoughConnections = new ArrayList<Integer>();
            for (int i = 0; i < netboxNodes; i++) {
                if (numberOfConnectionsPerNode.get(i+1)<netboxPeers) {
                    nodesWithNotEnoughConnections.add(i+1);                
                }
            }
            if (nodesWithNotEnoughConnections.size()>1) {
                int node1=1, node2=1;
                // Iterate until I find a new valid connection
                while (node1==node2 || connections.contains(new NodeConnection(node1, node2))) {
                    node1 = nodesWithNotEnoughConnections.get((int) Math.floor(random.nextDouble()*nodesWithNotEnoughConnections.size()));
                    node2 = nodesWithNotEnoughConnections.get((int) Math.floor(random.nextDouble()*nodesWithNotEnoughConnections.size()));            
                }
                connections.add(new NodeConnection(node1, node2));            
            }
        } while (nodesWithNotEnoughConnections.size()>1);

        // Now select the nodes this nodes have to connect to         
        nodesToConnectTo = new ArrayList<Integer>();
        for (NodeConnection connection : connections) {
            if (connection.from == selfNode) {
                nodesToConnectTo.add(connection.to);
            }
        }
    }
    
    /**
     * A pair of nodes that should connect
     */
    private static class NodeConnection {
        public int from;
        public int to;
        public NodeConnection(int from, int to) {
            super();
            this.from = from;
            this.to = to;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + from;
            result = prime * result + to;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NodeConnection other = (NodeConnection) obj;
            if (from != other.from)
                return false;
            if (to != other.to)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "NodeConnection [from=" + from + ", to=" + to + "]";
        }
    }
    
    

    @Override
    public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
        try {
            InetSocketAddress[] addresses = new InetSocketAddress[nodesToConnectTo.size()];
            int arrayIndex = 0;
            for (Integer nodeToConnectTo : nodesToConnectTo) {
                int port = BASE_NETBOX_PORT + nodeToConnectTo;
                addresses[arrayIndex++] = new InetSocketAddress(InetAddress.getLocalHost(), port);                    
            }
            return addresses;
        } catch (UnknownHostException e) {
            throw new PeerDiscoveryException(e);
        }
    }

    @Override
    public void shutdown() {
    }

}
