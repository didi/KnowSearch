package com.didi.arius.gateway.elasticsearch.client.model.exception;

public class ESAlreadyExistsException extends RuntimeException {
    private final Throwable t;

    public ESAlreadyExistsException(Throwable t) {
        this.t = t;
    }

    @Override
    public String getMessage() {
        return t.getMessage();
    }

    public static boolean check(Throwable t) {
        String str = t.getMessage();
        boolean res = false;
        if(str!=null && (str.contains("index_already_exists_exception") ||
                         str.contains("resource_already_exists_exception"))) {
            res = true;
        }
        return res;
    }
}
