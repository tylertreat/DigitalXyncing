package com.digitalxyncing.communication.impl;

import com.digitalxyncing.communication.ChannelListener;
import com.digitalxyncing.communication.Endpoint;
import com.digitalxyncing.communication.MessageHandlerFactory;
import com.digitalxyncing.communication.Peer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract implementation of {@link ChannelListener}.
 */
public abstract class AbstractChannelListener<T extends Serializable> extends Thread implements ChannelListener<T> {

    protected final Endpoint<T> endpoint;
    protected final ExecutorService threadPool;
    protected final MessageHandlerFactory<T> messageHandlerFactory;
    protected Set<Peer> peers;
    protected volatile boolean terminate;

    /**
     * Creates a new {@code AbstractChannelListener} instance.
     *
     * @param endpoint              the {@link Endpoint} this {@code AbstractChannelListener} belongs to
     * @param messageHandlerFactory the {@link MessageHandlerFactory} to use to construct
     *                              {@link com.digitalxyncing.communication.MessageHandler} instances
     */
    public AbstractChannelListener(Endpoint<T> endpoint, MessageHandlerFactory<T> messageHandlerFactory) {
        this(endpoint, 1, messageHandlerFactory);
    }

    /**
     * Creates a new {@code AbstractChannelListener} instance.
     *
     * @param endpoint              the {@link Endpoint} this {@code AbstractChannelListener} belongs to
     * @param threadMultiplier      the multiplier used to allocate the thread pool size, which is equal to
     *                              {@code number of CPUs * threadMultiplier}.
     * @param messageHandlerFactory the {@link MessageHandlerFactory} to use to construct
     *                              {@link com.digitalxyncing.communication.MessageHandler} instances
     */
    public AbstractChannelListener(Endpoint<T> endpoint, int threadMultiplier, MessageHandlerFactory<T> messageHandlerFactory) {
        this.endpoint = endpoint;
        this.messageHandlerFactory = messageHandlerFactory;
        // Message handling operations are probably CPU-bound, so allocate 1 thread per core by default
        this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * threadMultiplier);
        this.peers = new HashSet<Peer>();
    }

    @Override
    public final void run() {
        listen();
    }

    @Override
    public final void terminate() {
        terminate = true;
    }

}