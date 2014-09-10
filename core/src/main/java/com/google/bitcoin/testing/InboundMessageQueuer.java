package com.google.bitcoin.testing;

import com.google.bitcoin.core.*;
import com.google.common.util.concurrent.SettableFuture;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * An extension of {@link com.google.bitcoin.core.PeerSocketHandler} that keeps inbound messages in a queue for later processing
 */
public abstract class InboundMessageQueuer extends PeerSocketHandler {
    public final BlockingQueue<Message> inboundMessages = new ArrayBlockingQueue<Message>(1000);
    public final Map<Long, SettableFuture<Void>> mapPingFutures = new HashMap<Long, SettableFuture<Void>>();

    public Peer peer;
    public BloomFilter lastReceivedFilter;

    protected InboundMessageQueuer(NetworkParameters params) {
        super(params, new InetSocketAddress("127.0.0.1", 2000));
    }

    public Message nextMessage() {
        return inboundMessages.poll();
    }

    public Message nextMessageBlocking() throws InterruptedException {
        return inboundMessages.take();
    }

    @Override
    protected void processLowPriorityMessage(Message m) throws Exception {
        if (m instanceof Ping) {
            SettableFuture<Void> future = mapPingFutures.get(((Ping) m).getNonce());
            if (future != null) {
                future.set(null);
                return;
            }
        }
        if (m instanceof BloomFilter) {
            lastReceivedFilter = (BloomFilter) m;
        }
        inboundMessages.offer(m);
    }
    
    @Override
    protected void processHighPriorityMessage(long nodeId, Message message) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected long getSelfNodeId() {
        throw new UnsupportedOperationException();
    }
}
