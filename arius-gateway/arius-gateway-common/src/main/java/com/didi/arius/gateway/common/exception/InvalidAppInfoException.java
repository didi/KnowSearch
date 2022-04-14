package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public class InvalidAppInfoException extends QueryException {
    public InvalidAppInfoException(String msg) {
        super(msg);
    }

    public InvalidAppInfoException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidAppInfoException(StreamInput in) throws IOException{
        super(in);
    }
}
