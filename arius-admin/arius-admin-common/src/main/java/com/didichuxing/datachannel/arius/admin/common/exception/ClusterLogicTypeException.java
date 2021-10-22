package com.didichuxing.datachannel.arius.admin.common.exception;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

/**
 * Created by linyunan on 2021-06-02
 */
public class ClusterLogicTypeException extends BaseRunTimeException {

    public ClusterLogicTypeException(String message) {
        super(message, ResultType.CLUSTER_LOGIC_TYPE_ERROR);
    }
}
