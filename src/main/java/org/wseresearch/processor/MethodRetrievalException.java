package org.wseresearch.processor;

public class MethodRetrievalException extends Exception {
    public MethodRetrievalException(String message) {
        super(message);
    }
}

class SourceCodeNotExistentException extends MethodRetrievalException {
    public SourceCodeNotExistentException(String message) {
        super(message);
    }
}