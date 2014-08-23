package com.google.bitcoin.params;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NetboxParams extends RegTestParams{
    
    private static NetboxParams instance;
    private int selfNode;
    // The number of the nodes to connect to
    private List<Integer> nodesToConnectTo;
    private Map<NodeConnection, Double> distances;
    // Base port for net in a box test. Nodes should accept connections on port 19001, 19002, etc.
    public final static int BASE_NETBOX_PORT = 19000;

    public static synchronized NetboxParams get() {
        if (instance == null) {
            instance = new NetboxParams();
        }
        return instance;
    }
    
    public int getSelfNode() {
        return selfNode;
    }
    
    /**
     * Nodes to connect to are calculated based on the number of nodes, how many connections each node should have and
     * which node is this.
     * The process is deterministic so every node gets the same graph, but tries to emulate a random graph
     */
    public List<Integer> getNodesToConnectTo() {
        return nodesToConnectTo;
    }
    
    public double getDistance(int node1, int node2) {
        if (distances.containsKey(new NodeConnection(node1, node2))) {
            return distances.get(new NodeConnection(node1, node2));
        } else if (distances.containsKey(new NodeConnection(node2, node1))) {
            return distances.get(new NodeConnection(node2, node1));            
        } else {
            throw new RuntimeException("There si no connection between " + node1 + " and " + node2);
        }
    }

    

    public void initialize(int netboxNodes, int netboxPeers, int selfPort) {
        selfNode = selfPort - BASE_NETBOX_PORT;

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

        Map<Integer,Point> nodePoints = new HashMap<Integer, Point>();
        for (int i = 0; i < netboxNodes; i++) {
            int x = (int) Math.round(random.nextDouble()*100);
            int y = (int) Math.round(random.nextDouble()*100);
            nodePoints.put(i+1, new Point(x, y));
        }
        
        distances = new HashMap<NodeConnection, Double>();
        for (NodeConnection connection : connections) {
            Point p1 = nodePoints.get(connection.from);
            Point p2 = nodePoints.get(connection.to);
            double distance = Math.sqrt(Math.pow(Math.abs(p1.x-p2.x), 2) + Math.pow(Math.abs(p1.y-p2.y), 2)); 
            distances.put(connection, distance);            
        }
        

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
    
    private static class Point {
        public int x;
        public int y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
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
            Point other = (Point) obj;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "Point [x=" + x + ", y=" + y + "]";
        }        
    }
    


}