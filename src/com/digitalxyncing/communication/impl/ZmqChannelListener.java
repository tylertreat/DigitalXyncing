package com.digitalxyncing.communication.impl;

import com.digitalxyncing.communication.Endpoint;
import com.digitalxyncing.communication.MessageHandlerFactory;
import com.digitalxyncing.communication.Peer;
import org.zeromq.ZMQ;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Concrete implementation of {@link AbstractChannelListener} which relies on ZeroMQ for communication.
 */
public class ZmqChannelListener<T extends Serializable> extends AbstractChannelListener<T> {

    private int type;
    private ZMQ.Socket socket;

    /**
     * Creates a new {@code ZmqChannelListener} instance.
     *
     * @param endpoint              the {@link Endpoint} this {@code AbstractChannelListener} belongs to
     * @param address               the address to connect to
     * @param port                  the port to connect to
     * @param type                  the ZMQ socket type
     * @param messageHandlerFactory the {@link MessageHandlerFactory} to use to construct
     *                              {@link com.digitalxyncing.communication.MessageHandler} instances
     */
    public ZmqChannelListener(Endpoint<T> endpoint, String address, int port, int type, MessageHandlerFactory<T> messageHandlerFactory) {
        this(endpoint, type, messageHandlerFactory);
        Peer peer = new Peer(address, port);
        this.peers.add(peer);
    }

    /**
     * Creates a new {@code ZmqChannelListener} instance.
     *
     * @param endpoint              the {@link Endpoint} this {@code AbstractChannelListener} belongs to
     * @param type                  the ZMQ socket type
     * @param messageHandlerFactory the {@link MessageHandlerFactory} to use to construct
     *                              {@link com.digitalxyncing.communication.MessageHandler} instances
     */
    public ZmqChannelListener(Endpoint<T> endpoint, int type, MessageHandlerFactory<T> messageHandlerFactory) {
        super(endpoint, messageHandlerFactory);
        this.type = type;
    }

    @Override
    public void addPeer(String address, int port) {
        if (socket != null) {
            String connect = Endpoint.SCHEME + address + ':' + port;
            socket.connect(connect);
        }
        Peer peer = new Peer(address, port);
        this.peers.add(peer);
    }

    @Override
    public void listen() {
        terminate = false;
        ZMQ.Context context = ZMQ.context(1);
        socket = context.socket(type);
        for (Peer peer : peers) {
            String connect = Endpoint.SCHEME + peer.getAddress() + ':' + peer.getPort();
            System.out.println("Connecting to " + connect);
            socket.connect(connect);
        }
        if (type == ZMQ.SUB) {
            // Subscribe to ALL messages
            socket.subscribe("".getBytes());
        }
        while (!terminate) {
            // This is a blocking call which waits for incoming data
            // When data is received, it's sent to a handler which executes on a worker thread
            threadPool.execute(messageHandlerFactory.build(endpoint, socket.recv(0)));
        }
        shutdownAndAwaitTermination();
        socket.close();
        context.term();
    }

    private void shutdownAndAwaitTermination() {
        threadPool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            threadPool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
