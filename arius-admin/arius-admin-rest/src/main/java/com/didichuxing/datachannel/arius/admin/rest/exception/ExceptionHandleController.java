package com.didichuxing.datachannel.arius.admin.rest.exception;

import static com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType.ADMIN_OPERATE_ERROR;
import static com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType.ES_OPERATE_ERROR;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ResultWorkOrder;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.BaseException;
import com.didichuxing.datachannel.arius.admin.common.exception.BaseRunTimeException;
import com.didichuxing.datachannel.arius.admin.common.exception.WorkOrderOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 *
 * Created by d06679 on 2019/3/13.
 */
@RestControllerAdvice("com.didichuxing.datachannel.arius.admin.rest.controller")
public class ExceptionHandleController implements ThrowsAdvice {

    private static final ILog LOGGER = LogFactory.getLog(ExceptionHandleController.class);

    @ExceptionHandler(WorkOrderOperateException.class)
    public ResponseEntity<ResultWorkOrder> handleWorkOrderOperateException(WorkOrderOperateException e) {
        return new ResponseEntity<>(new ResultWorkOrder(e.getMessage(), ADMIN_OPERATE_ERROR.getCode()),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ESOperateException.class)
    public Result handleESOperateException(ESOperateException e) {
        LOGGER.warn("arius es rest process error.", e);
        Result result = Result.build(ES_OPERATE_ERROR);
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
    public Result handleBaseException(BaseException e) {
        return handlerExceptionWithResult(e, e.getResultType());
    }

    @ExceptionHandler(BaseRunTimeException.class)
    public Result handleBaseRunTimeException(BaseRunTimeException e) {
        return handlerExceptionWithResult(e, e.getResultType());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        LOGGER.warn("Illegal Argument error ", e);
        Result result = Result.build(ResultType.ILLEGAL_PARAMS);
        result.setMessage(e.getMessage());
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        LOGGER.warn("arius admin process error||errMsg={}", e.getMessage(), e);
        LOGGER.warn("arius admin process error||errStack={}", e.getStackTrace());
        Result result = Result.build(ResultType.FAIL);
        if (StringUtils.isNotBlank(e.getMessage())) {
            result.setMessage(e.getMessage());
        }
        return result;
    }

    private Result handlerExceptionWithResult(Exception e, ResultType resultType) {
        LOGGER.warn("arius admin rest process error.", e);
        Result result = Result.build(resultType);
        if (StringUtils.isNotBlank(e.getMessage())) {
            result.setMessage(e.getMessage());
        }
        return result;
    }
}
