package com.google.bitcoin.net.discovery;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class NetboxDiscovery implements PeerDiscovery {

    private int netboxNodes;
    private int selfPort;
    private final static int BASE_NETBOX_PORT = 19000;

    public NetboxDiscovery(int netboxNodes, int selfPort) {
        this.netboxNodes = netboxNodes; 
        this.selfPort = selfPort;
    }

    @Override
    public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
        try {
            InetSocketAddress[] addresses = new InetSocketAddress[netboxNodes-1];
            int arrayIndex = 0;
            for (int i = 0; i < netboxNodes-1; ++i) {
                int port = BASE_NETBOX_PORT + i;
                if (port!=selfPort) {
                    addresses[arrayIndex++] = new InetSocketAddress(InetAddress.getLocalHost(), port);                    
                }                 
            }
            Collections.shuffle(Arrays.asList(addresses));
            return addresses;
        } catch (UnknownHostException e) {
            throw new PeerDiscoveryException(e);
        }
    }

    @Override
    public void shutdown() {
    }

}
