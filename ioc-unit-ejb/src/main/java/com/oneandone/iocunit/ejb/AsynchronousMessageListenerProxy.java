package com.oneandone.iocunit.ejb;

import javax.jms.Message;
import javax.jms.MessageListener;

import com.oneandone.iocunit.ejb.jms.JmsSingletonsIntf;

/**
 * Registered for each MessageListener, onMessage is used to register asynchronous calls in the Asynchronous Manager.
 *
 * @author aschoerk
 */
public class AsynchronousMessageListenerProxy implements MessageListener {
    private final MessageListener listener;
    private final AsynchronousManager asynchronousManager;
    private final JmsSingletonsIntf jmsSingletons;

    /**
     * Create a Proxy
     *
     * @param listener            the listener to be called asynchronously. Normally the Mdb should be behind this.
     * @param asynchronousManager The asynchronous Managed where the calls are registered so that they can be called by the
     *                            test code at a specific time.
     */
    public AsynchronousMessageListenerProxy(MessageListener listener, AsynchronousManager asynchronousManager, JmsSingletonsIntf jmsSingletons) {
        this.listener = listener;
        this.asynchronousManager = asynchronousManager;
        this.jmsSingletons = jmsSingletons;
    }

    /**
     * Registers the call to the listener.
     *
     * @param message the message to be registered.
     */
    @Override
    public void onMessage(final Message message) {
        asynchronousManager.addOneTimeHandler(new Runnable() {
            @Override
            public void run() {
                jmsSingletons.jms2OnMessage(listener, message);

            }
        });
    }
}
