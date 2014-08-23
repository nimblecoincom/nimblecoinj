/**
 * Copyright 2011 Micheal Swiggs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.bitcoin.net.discovery;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.bitcoin.params.NetboxParams;

public class NetboxDiscoveryTest {
    @Test
    public void getPeers1() throws Exception{
        NetboxDiscovery netboxDiscovery = new NetboxDiscovery();
        InetSocketAddress[] addresses;

        NetboxParams netboxParams = NetboxParams.get();
        netboxParams.initialize(10, 3, 19001);

        
        addresses = netboxDiscovery.getPeers(0, TimeUnit.SECONDS);
        assertEquals(1, addresses.length);
        assertEquals(new InetSocketAddress(InetAddress.getLocalHost(), 19006), addresses[0]);

        netboxParams.initialize(10, 3, 19002);
        addresses = netboxDiscovery.getPeers(0, TimeUnit.SECONDS);
        assertEquals(1, addresses.length);
        assertEquals(new InetSocketAddress(InetAddress.getLocalHost(), 19008), addresses[0]);

        netboxParams.initialize(10, 3, 19003);
        addresses = netboxDiscovery.getPeers(0, TimeUnit.SECONDS);
        assertEquals(2, addresses.length);
        assertEquals(new InetSocketAddress(InetAddress.getLocalHost(), 19004), addresses[0]);
        assertEquals(new InetSocketAddress(InetAddress.getLocalHost(), 19006), addresses[1]);

    }
    
}
