package dev.kalles.support.exception;

import dev.kalles.shared.exception.BusinessRuleViolationException;

public class MessageNotEditableException extends BusinessRuleViolationException {

    public MessageNotEditableException() {
        super("SUPPORT_MESSAGE_NOT_EDITABLE", "Only the latest message from the same author can be edited");
    }
}
