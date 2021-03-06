package org.agilewiki.pactor;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.agilewiki.pactor.impl.MailboxImpl;
import org.agilewiki.pactor.MessageQueue;
import org.agilewiki.pactor.MessageQueueFactory;
import org.agilewiki.pactor.impl.MessageQueueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The MailboxFactory is the factory as the name suggests for the MailBoxes to be used with the PActor. In addition to 
 * creation of the Mailboxes it also encapsulates the threads( threadpool) which would process the Requests added to 
 * the mailbox in asynchronous mode.
 * </p>
 */

public final class MailboxFactory {
    private static Logger LOG = LoggerFactory.getLogger(MailboxFactory.class);;
    private final ExecutorService executorService;
    private final MessageQueueFactory messageQueueFactory;
    /** Must also be thread-safe. */
    private final List<AutoCloseable> closables = new Vector<AutoCloseable>();
    private final AtomicBoolean shuttingDown = new AtomicBoolean();

    public MailboxFactory() {
        this(null, null);
    }

    public MailboxFactory(final ExecutorService executorService,
            final MessageQueueFactory messageQueueFactory) {
        this.executorService = (executorService == null) ? Executors
                .newCachedThreadPool() : executorService;
        this.messageQueueFactory = (messageQueueFactory == null) ? MessageQueueFactoryImpl.INTANCE
                : messageQueueFactory;
    }

    public Mailbox createMailbox() {
        return new MailboxImpl(this, messageQueueFactory.createMessageQueue());
    }

    public Mailbox createMailbox(final MessageQueue messageQueue) {
        return new MailboxImpl(this, messageQueue);
    }

    public void submit(final Runnable task) throws Exception {
        try {
            executorService.submit(task);
        } catch (final Exception e) {
            if (!isShuttingDown())
                throw e;
            else
                LOG.warn("Unable to process the request, possible mailbox shutdown had been called in the application");
        } catch (final Error e) {
            if (!isShuttingDown())
                throw e;
        }
    }

    public void addAutoClosable(final AutoCloseable closeable) {
        // Not perfect synchronization, but good enough, IMO
        if (!isShuttingDown()) {
            closables.add(closeable);
        } else {
            throw new IllegalStateException("Shuting down ...");
        }
    }

    public void shutdown() {
        if (shuttingDown.compareAndSet(false, true)) {
            executorService.shutdownNow();
            final Iterator<AutoCloseable> it = closables.iterator();
            while (it.hasNext()) {
                try {
                    it.next().close();
                } catch (final Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public boolean isShuttingDown() {
        return shuttingDown.get();
    }
}
