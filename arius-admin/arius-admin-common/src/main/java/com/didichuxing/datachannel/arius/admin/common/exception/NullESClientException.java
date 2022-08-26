package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

/**
 * > 该类用于在 Elasticsearch 客户端为空时抛出异常
 *
 * @author shizeying
 * @date 2022/08/26
 */
public class NullESClientException extends NullPointerException {
    private final ResultType resultType;
    
    
    public NullESClientException(String cluster) {
        super( ResultType.ES_CLIENT_NUL_ERROR.getCode()+"-"+String.format(ResultType.ES_CLIENT_NUL_ERROR.getMessage(), "cluster"));
        this.resultType = ResultType.ES_CLIENT_NUL_ERROR;
        
    }
    
    public ResultType getResultType() {
        return resultType;
    }
}