package com.didi.cloud.fastdump.common.bean.common;

import java.io.Serializable;

import com.didi.cloud.fastdump.common.content.ResultType;

import lombok.Data;

/**
 * 返回结构
 */
@Data
public class BaseResult implements Serializable {

    private static final long serialVersionUID = 3472961240718956029L;

    /**
     * 异常信息
     */
    protected String          message;

    /**
     * 提示
     */
    protected String          tips;

    /**
     * 返回码，0表示成功；10000表示参数错误；10004表示重复；10005表示不存在；
     */
    protected Integer         code;

    public boolean success() {
        return getCode() != null && ResultType.SUCCESS.getCode() == getCode();
    }

    public boolean failed() {
        return !success();
    }
}
