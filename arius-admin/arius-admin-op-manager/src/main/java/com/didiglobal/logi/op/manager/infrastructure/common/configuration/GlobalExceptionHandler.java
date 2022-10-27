package com.didiglobal.logi.op.manager.infrastructure.common.configuration;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 处理器
 * 全局异常
 *
 * @author didi
 * @date 2022-09-06 16:40
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * sql异常
     *
     * @param e 异常
     * @return
     */
    @ExceptionHandler(value = BadSqlGrammarException.class)
    @ResponseBody
    public Result bizExceptionHandler(BadSqlGrammarException e) {
        LOGGER.error("发生sql异常！原因是：", e);
        return Result.fail(ResultCode.SQL_ERROR);
    }

    /**
     * 处理空指针的异常
     *
     * @param e 空指针异常
     * @return
     */
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public Result exceptionHandler(NullPointerException e) {
        LOGGER.error("发生空指针异常！原因是:", e);
        return Result.fail(ResultCode.NULL_POINT_ERROR);
    }

    /**
     * 处理其他异常
     *
     * @param e 异常
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result exceptionHandler(Exception e) {
        LOGGER.error("未知异常！原因是:", e);
        return Result.fail(ResultCode.COMMON_FAIL);
    }
}
