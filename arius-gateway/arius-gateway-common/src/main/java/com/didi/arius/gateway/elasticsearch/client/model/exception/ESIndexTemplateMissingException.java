package com.didi.arius.gateway.elasticsearch.client.model.exception;

public class ESIndexTemplateMissingException extends RuntimeException {
    private final Throwable t;

    public ESIndexTemplateMissingException(Throwable t) {
        this.t = t;
    }

    @Override
    public String getMessage() {
        return t.getMessage();
    }

    public static boolean check(Throwable t) {
        String str = t.getMessage();
        boolean res = false;
        if(str!=null && str.contains("index_template_missing_exception")) {
            res = true;
        }
        return res;
    }
}
