package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年9月6日
* 
*/
public class UnauthorizedException extends QueryException {
    public UnauthorizedException(String msg) {
        super(msg);
        addHeader("WWW-authenticate", "Basic realm=need the correct appid and password");
    }

    public UnauthorizedException(String msg, Throwable cause) {
        super(msg, cause);
        addHeader("WWW-authenticate", "Basic realm=need the correct appid and password");
    }

    public UnauthorizedException(StreamInput in) throws IOException{
        super(in);
        addHeader("WWW-authenticate", "Basic realm=need the correct appid and password");
    }
    
    @Override
    public RestStatus status() {
        return RestStatus.UNAUTHORIZED;
    }
}
