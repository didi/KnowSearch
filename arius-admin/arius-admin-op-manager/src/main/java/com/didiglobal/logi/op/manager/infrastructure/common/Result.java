package com.didiglobal.logi.op.manager.infrastructure.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 非分页统一的返回规范
 *
 * @author cjm
 */
@Data
@ApiModel(description = "统一返回格式")
public class Result<T> extends BaseResult {

    @ApiModelProperty(value = "返回数据")
    protected T data;

    public boolean isSuccess() {
        return getCode() != null && ResultCode.SUCCESS.getCode().equals(getCode());
    }

    public boolean failed() {
        return !isSuccess();
    }

    public Result() {
    }

    private Result(Integer code) {
        this.code = code;
    }

    private Result(Integer code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public static <T> Result<T> build(boolean succ) {
        if (succ) {
            return success();
        }
        return fail();
    }

    public static <T> Result<T> success(T data) {
        Result<T> ret = new Result<>(ResultCode.SUCCESS.getCode());
        ret.setMessage(ResultCode.SUCCESS.getMessage());
        ret.setData(data);
        return ret;
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> ret = new Result<>(resultCode.getCode());
        ret.setMessage(resultCode.getMessage());
        return ret;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> ret = new Result<>(code);
        ret.setMessage(msg);
        return ret;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> ret = new Result<>(ResultCode.COMMON_FAIL.getCode());
        ret.setMessage(msg);
        return ret;
    }

    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.COMMON_FAIL.getCode());
        result.setMessage(ResultCode.COMMON_FAIL.getMessage());
        return result;
    }


    public static <T> Result<T> buildSuccess(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> buildFrom(Result<? extends Object> result) {
        Result<T> resultT = new Result<>();
        resultT.setCode(result.getCode());
        resultT.setMessage(result.getMessage());
        return resultT;
    }

    public static <T> Result<T> buildParamIllegal(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.PARAM_NOT_VALID.getCode());
        result.setMessage(ResultCode.PARAM_NOT_VALID.getMessage() + ":" + msg + "，请检查后再提交！");
        return result;
    }
}
