package com.didichuxing.datachannel.arius.admin.common.bean.common;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Service服务执行的结果
 @author ohushenglin_v
 @date 2022-05-10
 */
@ApiModel(description = "返回结构")
public class Result<T> extends BaseResult {

    @ApiModelProperty("内容")
    protected T                 data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static <T> Result<T> build(ResultType resultType) {
        Result<T> result = new Result<>();
        result.setCode(resultType.getCode());
        result.setMessage(resultType.getMessage());
        return result;
    }

    public static <T> Result<T> build(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> buildSucc() {
        Result<T> result = new Result<>();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(ResultType.SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> buildSucWithTips(String tips) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(ResultType.SUCCESS.getMessage());
        result.setTips(tips);
        return result;
    }

    public static <T> Result<T> buildFail(String failMsg) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.FAIL.getCode());
        result.setMessage(failMsg);
        return result;
    }

    public static <T> Result<T> buildFail(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.FAIL.getCode());
        result.setMessage(ResultType.FAIL.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> buildFailWithMsg(T data, String failMsg) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.FAIL.getCode());
        result.setMessage(failMsg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> buildFail() {
        Result<T> result = new Result<>();
        result.setCode(ResultType.FAIL.getCode());
        result.setMessage(ResultType.FAIL.getMessage());
        return result;
    }

    public static Result<Boolean> buildBoolen(boolean succ){
        if (succ) {
            return buildSucc(succ);
        }
        return buildFail();
    }

    public static <T> Result<T> build(boolean succ) {
        if (succ) {
            return buildSucc();
        }
        return buildFail();
    }

    public static <T> Result<T> buildWithTips(boolean succ, String tips) {
        if (succ) {
            return buildSucWithTips(tips);
        }
        return buildFail();
    }

    public static <T> Result<T> buildParamIllegal(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.ILLEGAL_PARAMS.getCode());
        result.setMessage(ResultType.ILLEGAL_PARAMS.getMessage() + ":" + msg + "，请检查后再提交！");
        return result;
    }

    public static <T> Result<T> buildDuplicate(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.DUPLICATION.getCode());
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> buildNotExist(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.NOT_EXIST.getCode());
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> buildOpForBidden(String msg) {
        Result<T> result = new Result<>();
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

    public static <T> Result<T> buildWithMsg(boolean succ, String msg) {
        Result<T> result = new Result<>();
        if (succ) {
            result.setCode(ResultType.SUCCESS.getCode());
            result.setMessage(msg);
        } else {
            result.setCode(ResultType.FAIL.getCode());
            result.setMessage(msg);
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

    public static <T> Result<T> buildSuccWithMsg(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ResultType.SUCCESS.getCode());
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> buildSucc(T data, String msg) {
        Result<T> result = new Result<>();
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

    public static <T> Result<T> buildFrom(Result<? extends Object> result) {
        Result<T> resultT = new Result<>();
        resultT.setCode(result.getCode());
        resultT.setMessage(result.getMessage());
        return resultT;
    }
}
