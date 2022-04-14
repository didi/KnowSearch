package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.exception;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusRunTimeException;

/**
 * @author d06679
 * @date 2019-06-25
 */
public class ClusterMetadataException extends AriusRunTimeException {
    public ClusterMetadataException(String message) {
        super(message, ResultType.ADMIN_META_ERROR);
    }
}
