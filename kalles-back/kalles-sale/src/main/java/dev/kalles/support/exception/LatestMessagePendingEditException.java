package dev.kalles.support.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class LatestMessagePendingEditException extends BusinessRuleViolationException {

    public LatestMessagePendingEditException() {
        super("SUPPORT_LATEST_MESSAGE_PENDING_EDIT", "You must edit your latest message before sending another one");
    }
}
