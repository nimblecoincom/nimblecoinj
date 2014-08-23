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

import com.google.bitcoin.params.NetboxParams;

/**
 * Discovery method for net in a box tests
 * @author Oscar Guindzberg
 *
 */
public class NetboxDiscovery implements PeerDiscovery {

    public NetboxDiscovery() {
    }
    

    @Override
    public InetSocketAddress[] getPeers(long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
        try {
            NetboxParams params = NetboxParams.get();
            List<Integer> nodesToConnectTo = params.getNodesToConnectTo();
            InetSocketAddress[] addresses = new InetSocketAddress[nodesToConnectTo.size()];
            int arrayIndex = 0;
            for (Integer nodeToConnectTo : nodesToConnectTo) {
                int port = NetboxParams.BASE_NETBOX_PORT + nodeToConnectTo;
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
