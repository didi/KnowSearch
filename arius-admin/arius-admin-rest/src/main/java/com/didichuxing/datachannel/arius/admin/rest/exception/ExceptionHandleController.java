package com.didichuxing.datachannel.arius.admin.rest.exception;

import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.ADMIN_OPERATE_ERROR;
import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.ES_OPERATE_ERROR;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.didichuxing.datachannel.arius.admin.biz.project.LoginManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ResultWorkOrder;
import com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.*;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.exception.KfSecurityException;

/**
 *
 * Created by d06679 on 2019/3/13.
 */
@RestControllerAdvice(basePackages = { "com.didichuxing.datachannel.arius.admin.rest.controller" }, basePackageClasses = { LoginManager.class })
public class ExceptionHandleController implements ThrowsAdvice {

    private static final ILog LOGGER = LogFactory.getLog(ExceptionHandleController.class);

    @ExceptionHandler(WorkOrderOperateException.class)
    public ResponseEntity<ResultWorkOrder> handleWorkOrderOperateException(WorkOrderOperateException e) {
        return new ResponseEntity<>(new ResultWorkOrder(e.getMessage(), ADMIN_OPERATE_ERROR.getCode()),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ESOperateException.class)
    public Result<Object> handleESOperateException(ESOperateException e) {
        LOGGER.warn(
            "class=ExceptionHandleController||method=handleESOperateException||msg=arius es rest process error.", e);
        Result<Object> result = Result.build(ES_OPERATE_ERROR);
        StringBuilder stringBuilder = new StringBuilder(e.getMessage());
        Throwable throwable = e.getCause();
        while (throwable != null) {
            stringBuilder.append(":").append(throwable.getMessage());
            throwable = throwable.getCause();
        }
        result.setMessage(stringBuilder.toString());

        return result;
    }

    @ExceptionHandler(BaseException.class)
    public Result<Object> handleBaseException(BaseException e) {
        return handlerExceptionWithResult(e, e.getResultType());
    }

    @ExceptionHandler(AdminOperateException.class)
    public Result<Object> handleBaseRunTimeException(AdminOperateException e) {
        return handlerExceptionWithResult(e, e.getResultType());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        LOGGER.warn(
            "class=ExceptionHandleController||method=handleIllegalArgumentException||msg=Illegal Argument error ", e);
        Result<Object> result = Result.build(ResultType.ILLEGAL_PARAMS);
        result.setMessage(e.getMessage());
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        LOGGER.warn("class=ExceptionHandleController||method=handleException||arius admin process error||errMsg={}",
            e.getMessage(), e);
        LOGGER.warn("class=ExceptionHandleController||method=handleException||arius admin process error||errStack={}",
            e.getStackTrace());
        Result<Object> result = Result.build(ResultType.FAIL);
        if (StringUtils.isNotBlank(e.getMessage())) {
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ExceptionHandler(KfSecurityException.class)
    public Result<Object> handleLogiSecurityException(KfSecurityException e) {
        LOGGER.warn(
            "class=ExceptionHandleController||method=handleLogiSecurityException||arius admin process error||errMsg={}",
            e.getMessage(), e);
        LOGGER.warn(
            "class=ExceptionHandleController||method=handleLogiSecurityException||arius admin process error||errStack={}",
            e.getStackTrace());
        Result<Object> result = Result.build(ResultType.FAIL);
        if (StringUtils.isNotBlank(e.getMessage())) {
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ExceptionHandler(OperateForbiddenException.class)
    public Result<Object> handleOperateForbiddenException(OperateForbiddenException e) {
        LOGGER.warn(
            "class=ExceptionHandleController||method=handleOperateForbiddenException||arius admin process error||errMsg={}",
            e.getMessage(), e);
        LOGGER.warn(
            "class=ExceptionHandleController||method=handleOperateForbiddenException||arius admin process error||errStack={}",
            e.getStackTrace());
        Result<Object> result = Result.build(ResultType.FAIL);
        if (StringUtils.isNotBlank(e.getMessage())) {
            result.setMessage(e.getMessage());
        }
        return result;
    }

    private Result<Object> handlerExceptionWithResult(Exception e, ResultType resultType) {
        LOGGER.warn(
            "class=ExceptionHandleController||method=handlerExceptionWithResult||arius admin rest process error.", e);
        Result<Object> result = Result.build(resultType);
        if (StringUtils.isNotBlank(e.getMessage())) {
            result.setMessage(e.getMessage());
        }
        return result;
    }
}