package com.didichuxing.datachannel.arius.admin.common.bean.common;

import java.io.Serializable;

/**
 * Service服务执行的结果
 * @author d06679
 * @date 2019/3/22
 */
public class ResultWorkOrder implements Serializable {

    private static final long serialVersionUID = 3472961240718956029L;

    private String            errorMsg;
    private Integer           errCode;

    public ResultWorkOrder(String errorMsg, Integer errCode) {
        this.errorMsg = errorMsg;
        this.errCode = errCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public static <T> ResultWorkOrder build(Result<T> result) {
        return new ResultWorkOrder(result.getMessage(), result.getCode());
    }
}
