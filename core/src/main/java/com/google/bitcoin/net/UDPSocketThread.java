package com.google.bitcoin.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPSocketThread extends Thread {
    
    private static final Logger log = LoggerFactory.getLogger(UDPSocketThread.class);
    
    private DatagramSocket datagramSocket;
    private Set<BlockingClient> clients;
   
    private boolean stopSignal = false;
        
    public UDPSocketThread(DatagramSocket datagramSocket, Set<BlockingClient> clients) {
        super("UDP socket handler thread");
        this.datagramSocket=datagramSocket;
        this.clients = clients;
    }
    
    @Override
    public void run() {
        while (!stopSignal) {
            try {
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                datagramSocket.receive(packet);

                //String received = new String(packet.getData(), 0, packet.getLength());

                // long nodeId = Utils.readInt64(packet.getData(), packet.getOffset());
                
                for (BlockingClient blockingClient : clients) {
                    blockingClient.receiveBytesUDP(packet.getData(), packet.getOffset(), packet.getLength());
                }
                
            } catch (IOException e) {
                log.error("Exception processing UDP message", e);
            }
        }
        
    }

    public void doStop() {
        stopSignal = true;
        
    }

}
