package com.wtc.handler;

import com.wtc.constant.MessageConstant;
import com.wtc.exception.BaseException;
import com.wtc.result.Result;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<Object> exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获数据库异常
     * 
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<Object> exceptionHandler(PSQLException ex) {
        ServerErrorMessage serverErrorMessage = ex.getServerErrorMessage();
        String detail = serverErrorMessage.getDetail();
        if (serverErrorMessage.getSQLState().equals("23505")) {
            Pattern pattern = Pattern.compile("\\((.*?)\\)=\\((.*?)\\)");
            Matcher matcher = pattern.matcher(detail);
            if (matcher.find()) {
                String value = matcher.group(2);
                String msg = String.format("%s %s", value, MessageConstant.ALREADY_EXISTS);
                return Result.error(msg);
            }
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
