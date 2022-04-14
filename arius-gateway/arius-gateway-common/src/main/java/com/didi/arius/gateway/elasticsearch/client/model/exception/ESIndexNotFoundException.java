package com.didi.arius.gateway.elasticsearch.client.model.exception;

public class ESIndexNotFoundException extends RuntimeException {
    private final Throwable t;

    @Override
    public String getMessage() {
        return t.getMessage();
    }

    public ESIndexNotFoundException(Throwable t) {
        this.t = t;
    }

    public static boolean check(Throwable t) {
        String str = t.getMessage();
        boolean res = false;
        if(str!=null && str.contains("index_not_found_exception")) {
            res = true;
        }
        return res;
    }
}
