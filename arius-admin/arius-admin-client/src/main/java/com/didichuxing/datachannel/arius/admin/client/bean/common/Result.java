package com.didichuxing.datachannel.arius.admin.client.bean.common;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Service服务执行的结果
 * @author d06679
 * @date 2019/3/22
 */
@ApiModel(description = "返回结构")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 3472961240718956029L;

    @ApiModelProperty("内容")
    private T                 data;

    @ApiModelProperty("异常信息")
    private String            message;

    @ApiModelProperty("提示")
    private String            tips;

    @ApiModelProperty("返回码，0表示成功；10000表示参数错误；10004表示重复；10005表示不存在；")
    private Integer           code;

    public Result() {
    }

    public boolean success() {
        return getCode() != null && ResultType.SUCCESS.getCode() == getCode();
    }

    public boolean duplicate() {
        return getCode() != null && ResultType.DUPLICATION.getCode() == getCode();
    }

    public boolean failed() {
        return !success();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static Result build(ResultType resultType) {
        Result result = new Result();
        result.setCode(resultType.getCode());
        result.setMessage(resultType.getMessage());
        return result;
    }

    public static Result build(int code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }

    public static Result buildSucc() {
        Result result = new Result();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(ResultType.SUCCESS.getMessage());
        return result;
    }

    public static Result buildSucWithTips(String tips) {
        Result result = new Result();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(ResultType.SUCCESS.getMessage());
        result.setTips(tips);
        return result;
    }

    public static Result buildFail(String failMsg) {
        Result result = new Result();
        result.setCode(ResultType.FAIL.getCode());
        result.setMessage(failMsg);
        return result;
    }

    public static Result buildFail() {
        Result result = new Result();
        result.setCode(ResultType.FAIL.getCode());
        result.setMessage(ResultType.FAIL.getMessage());
        return result;
    }

    public static Result build(boolean succ) {
        if (succ) {
            return buildSucc();
        }
        return buildFail();
    }

    public static Result buildWithTips(boolean succ, String tips) {
        if (succ) {
            return buildSucWithTips(tips);
        }
        return buildFail();
    }

    public static Result buildParamIllegal(String msg) {
        Result result = new Result();
        result.setCode(ResultType.ILLEGAL_PARAMS.getCode());
        result.setMessage(ResultType.ILLEGAL_PARAMS.getMessage() + ":" + msg + "，请检查后再提交！");
        return result;
    }

    public static Result buildDuplicate(String msg) {
        Result result = new Result();
        result.setCode(ResultType.DUPLICATION.getCode());
        result.setMessage(msg);
        return result;
    }

    public static Result buildNotExist(String msg) {
        Result result = new Result();
        result.setCode(ResultType.NOT_EXIST.getCode());
        result.setMessage(msg);
        return result;
    }

    public static Result buildOpForBidden(String msg) {
        Result result = new Result();
        result.setCode(ResultType.OPERATE_FORBIDDEN_ERROR.getCode());
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> build(boolean succ, T data) {
        Result<T> result = new Result<>();
        if (succ) {
            result.setCode(ResultType.SUCCESS.getCode());
            result.setMessage(ResultType.SUCCESS.getMessage());
            result.setData(data);
        } else {
            result.setCode(ResultType.FAIL.getCode());
            result.setMessage(ResultType.FAIL.getMessage());
        }
        return result;
    }

    public static <T> Result<T> buildSucc(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(ResultType.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static Result buildSucc(String msg) {
        Result result = new Result();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> buildSucc(T data, String msg) {
        Result result = new Result();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setData(data);
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> buildSuccWithTips(T data, String tips) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(ResultType.SUCCESS.getMessage());
        result.setData(data);
        result.setTips(tips);
        return result;
    }

    public static <T> Result<T> buildFrom(Result result) {
        Result<T> resultT = new Result<>();
        resultT.setCode(result.getCode());
        resultT.setMessage(result.getMessage());
        return resultT;
    }
}
