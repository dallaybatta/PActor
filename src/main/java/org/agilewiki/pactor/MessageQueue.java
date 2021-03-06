package org.agilewiki.pactor;

import org.agilewiki.pactor.ExceptionHandler;
import org.agilewiki.pactor.Message;
import org.agilewiki.pactor.MessageSource;
import org.agilewiki.pactor.Request;
import org.agilewiki.pactor.ResponseProcessor;

/**
 * A concurrent message queue, used in the Mailbox.
 *
 * @author monster
 */
public interface MessageQueue {
    /** Creates a new Message instance. */
    Message createMessage(final MessageSource source, final Message old,
            final Request<?> _request, final ExceptionHandler handler,
            final ResponseProcessor<?> rp);

    /** Is the queue empty? */
    boolean isNonEmpty();

    /**
     * Inserts the specified message into this queue.
     * Always returns true.
     */
    boolean add(final Message e);

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     * @return the head of this queue, or null if this queue is empty
     */
    Message poll();
}
