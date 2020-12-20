package com.wupol.myopia.base.exception;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.Set;

/**
 * 全局拦截处理Controller层抛出的异常
 *
 * @Author HaoHao
 * @Date 2020/12/20
 **/
@RestControllerAdvice
public class ControllerExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    /**
     * 拦截业务异常（自定义异常，继承RuntimeException），并返回业务异常信息
     * @param ex BusinessErrorException
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult handleBusinessError(BusinessException ex) {
        return ApiResult.failure(ex.getCode(), ex.getMessage());
    }

    /**
     * 空指针异常
     * @param ex NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult handleTypeMismatchException(NullPointerException ex) {
        logger.error("空指针异常，{}", ex.getMessage());
        return ApiResult.failure(ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    /**
     * 缺少请求参数异常
     * @param ex HttpMessageNotReadableException
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult handleHttpMessageNotReadableException(MissingServletRequestParameterException ex) {
        logger.error("缺少请求参数，{}", ex.getMessage());
        return ApiResult.failure("缺少必要的请求参数");
    }

    /**
     * 接口数据验证异常
     * @param ex MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        logger.error("请求参数不正确，{}", ex.getMessage());
        return ApiResult.failure(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 使用@NotEmpty、@NotNull等注释的参数验证失败时引发的异常
     * @param ex MethodArgumentNotValidException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult handleConstraintViolationException(ConstraintViolationException ex){
        logger.error("请求参数不正确，{}", ex.getMessage());
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        StringBuilder strBuilder = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            strBuilder.append(violation.getMessage());
            break;
        }
        return ApiResult.failure(strBuilder.toString());
    }

    /**
     * 没有访问权限 Need Spring Security Supported
     * @param ex AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ApiResult accessDeniedExceptionHandler(AccessDeniedException ex) {
        logger.error("没有访问权限，{}", ex.getMessage());
        return ApiResult.failure(HttpStatus.UNAUTHORIZED.value(), "没有访问权限");
    }

    /**
     * 传输数据格式有误
     * @param ex HttpMessageNotReadableException
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
        logger.error("传输数据格式有误，{}", ex.getMessage());
        return ApiResult.failure("传输数据格式有误");
    }

    /**
     * 索引冲突
     * @param ex DuplicateKeyException
     */
    @ExceptionHandler(value = {DuplicateKeyException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult duplicateErrorHandler(HttpServletRequest req, Exception ex) {
        logger.error("接口: [URI]:{} 唯一索引冲突", req.getRequestURL(), ex);
        return ApiResult.failure("录入了重复的数据，请检查数据的唯一约束");
    }

    /**
     * 系统异常 预期以外异常
     * @param ex Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult handleUnexpectedServer(Exception ex) {
        logger.error("系统异常：", ex);
        return ApiResult.failure(ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

}
