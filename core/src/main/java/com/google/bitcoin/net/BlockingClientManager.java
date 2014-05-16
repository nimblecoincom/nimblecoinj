/*
 * Copyright 2013 Google Inc.
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

package com.google.bitcoin.net;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.net.SocketFactory;

import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;

/**
 * <p>A thin wrapper around a set of {@link BlockingClient}s.</p>
 *
 * <p>Generally, using {@link NioClient} and {@link NioClientManager} should be preferred over {@link BlockingClient}
 * and {@link BlockingClientManager} as they scale significantly better, unless you wish to connect over a proxy or use
 * some other network settings that cannot be set using NIO.</p>
 */
public class BlockingClientManager extends AbstractIdleService implements ClientConnectionManager {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BlockingClientManager.class);

    private final SocketFactory socketFactory;
    private final Set<BlockingClient> clients = Collections.synchronizedSet(new HashSet<BlockingClient>());
    private int connectTimeoutMillis = 1000;
    private ServerSocket serverSocket;
    private volatile boolean vServerCloseRequested = false;

    
    public BlockingClientManager() {
        socketFactory = SocketFactory.getDefault();
    }

    /**
     * Creates a blocking client manager that will obtain sockets from the given factory. Useful for customising how
     * bitcoinj connects to the P2P network.
     */
    public BlockingClientManager(SocketFactory socketFactory) {
        this.socketFactory = checkNotNull(socketFactory);
    }

    @Override
    public void openConnection(SocketAddress serverAddress, StreamParser parser) {
        if (!isRunning())
            throw new IllegalStateException();
        try {
            new BlockingClient(serverAddress, parser, connectTimeoutMillis, socketFactory, clients);
        } catch (IOException e) {
            throw new RuntimeException(e); // This should only happen if we are, eg, out of system resources
        }
    }

    /** Sets the number of milliseconds to wait before giving up on a connect attempt */
    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    @Override
    protected void startUp() throws Exception { }

    @Override
    protected void shutDown() throws Exception {
        synchronized (clients) {
            for (BlockingClient client : clients)
                client.closeConnection();
        }
        if (serverSocket!=null) {
            vServerCloseRequested = true;
            serverSocket.close();            
        }
    }

    @Override
    public int getConnectedClientCount() {
        return clients.size();
    }

    @Override
    public void closeConnections(int n) {
        if (!isRunning())
            throw new IllegalStateException();
        synchronized (clients) {
            Iterator<BlockingClient> it = clients.iterator();
            while (n-- > 0 && it.hasNext())
                it.next().closeConnection();
        }
    }

    @Override
    public void acceptConnections(int serverPort, final StreamParserFactory parserFactory) {        
        if (!isRunning())
            throw new IllegalStateException();
        try {
            serverSocket = new ServerSocket(serverPort);
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            Socket socket = serverSocket.accept();
                            new BlockingClient(socket, parserFactory, clients);                            
                        }
                    } catch (Exception e) {
                        if (!vServerCloseRequested)
                            log.error("Error trying to accept new connection from server socket: " + serverSocket, e);
                    } finally {
                        try {
                            serverSocket.close();
                        } catch (IOException e1) {
                            // At this point there isn't much we can do, and we can probably assume the channel is closed
                        }
                    }
                }
            };
            t.setName("BlockingClientManager server socket thread");
            t.setDaemon(true);
            t.start();            
        } catch (IOException e) {
            throw new RuntimeException(e); // This should only happen if we are, eg, out of system resources
        }

        
    }
}
