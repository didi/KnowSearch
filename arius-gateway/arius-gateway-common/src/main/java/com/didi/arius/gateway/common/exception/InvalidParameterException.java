package com.didi.arius.gateway.common.exception;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

/**
* @author weizijun
* @date：2016年8月22日
* 
*/
public class InvalidParameterException extends QueryException {
    public InvalidParameterException(String msg) {
        super(msg);
    }

    public InvalidParameterException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidParameterException(StreamInput in) throws IOException{
        super(in);
    }
}
