package com.didi.arius.gateway.elasticsearch.client.model.exception;

public class ExceptionFactory {

    private ExceptionFactory() {

    }

    public static Throwable translate(Throwable t) {
        if(ESIndexNotFoundException.check(t)) {
            return new ESIndexNotFoundException(t);
        }

        if(ESIndexTemplateMissingException.check(t)) {
            return new ESIndexTemplateMissingException(t);
        }

        if(ESAlreadyExistsException.check(t)) {
            return new ESAlreadyExistsException(t);
        }

        return t;
    }
}
