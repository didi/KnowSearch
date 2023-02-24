package com.didi.cloud.fastdump.core.factory;

import com.didi.cloud.fastdump.common.exception.BaseException;

/**
 * Created by linyunan on 2022/8/10
 */
public abstract class BaseClientFactory<Client> {
    protected abstract Client getClientByType(String clientType) throws BaseException;
}
