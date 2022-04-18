package com.didichuxing.datachannel.arius.admin.common.bean.common;

import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * fitz
 */
@ApiModel(description = "返回结构")
@Data
public class BaseResult implements Serializable {

    private static final long serialVersionUID = 3472961240718956029L;


    @ApiModelProperty("异常信息")
    protected String            message;

    @ApiModelProperty("提示")
    protected String            tips;

    @ApiModelProperty("返回码，0表示成功；10000表示参数错误；10004表示重复；10005表示不存在；")
    protected Integer           code;

    public boolean success() {
        return getCode() != null && ResultType.SUCCESS.getCode() == getCode();
    }

    public boolean duplicate() {
        return getCode() != null && ResultType.DUPLICATION.getCode() == getCode();
    }

    public boolean failed() {
        return !success();
    }

    public boolean isPagine() {
        return false;
    }

}
