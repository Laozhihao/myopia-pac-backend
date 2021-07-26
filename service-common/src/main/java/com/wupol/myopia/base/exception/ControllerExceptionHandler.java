package com.wupol.myopia.base.exception;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.nio.file.AccessDeniedException;
import java.util.List;
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
    public ApiResult<Object> handleBusinessError(HttpServletRequest req, BusinessException ex) {
        logger.error("【业务异常】接口:{}，异常信息：{}", req.getRequestURL(), ex.getMessage(), ex);
        return ApiResult.failure(ex.getCode(), ex.getMessage());
    }

    /**
     * 通过Assert抛出的数据校验异常
     * @param ex ValidationException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult<Object> handleIllegalArgumentExceptionException(IllegalArgumentException ex) {
        logger.error("数据校验异常，{}", ex.getMessage(), ex);
        return ApiResult.failure(ex.getMessage());
    }

    /**
     * 数据校验异常
     * @param ex ValidationException
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult<Object> handleTypeMismatchException(ValidationException ex) {
        logger.error("数据校验异常，{}", ex.getMessage(), ex);
        return ApiResult.failure(ex.getMessage());
    }

    /**
     * 空指针异常
     * @param ex NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> handleTypeMismatchException(NullPointerException ex) {
        logger.error("空指针异常", ex);
        return ApiResult.failure(ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    /**
     * 缺少请求参数异常
     * @param ex HttpMessageNotReadableException
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult<Object> handleHttpMessageNotReadableException(MissingServletRequestParameterException ex) {
        logger.error("缺少请求参数，{}", ex.getMessage(), ex);
        return ApiResult.failure("缺少必要的请求参数");
    }

    /**
     * 接口数据验证异常
     * @param ex MethodArgumentNotValidException
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        logger.error("请求参数不正确，{}", ex.getMessage(), ex);
        return ApiResult.failure(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 使用@NotEmpty、@NotNull等注释的参数验证失败时引发的异常
     * @param ex ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult<Object> handleConstraintViolationException(ConstraintViolationException ex){
        logger.error("请求参数不正确，{}", ex.getMessage(), ex);
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        return ApiResult.failure(violations.stream().map(ConstraintViolation::getMessage).findFirst().orElse("请求参数不正确"));
    }


    /**
     * 使用@NotEmpty、@NotNull等注释的参数验证失败时引发的异常
     * @param ex BindException
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult<Object> handleBindException(BindException ex){
        logger.error("请求参数不正确，{}", ex.getMessage(), ex);
        List<ObjectError> allErrors = ex.getAllErrors();
        return ApiResult.failure(CollectionUtils.isEmpty(allErrors) ? "请求参数错误": allErrors.get(0).getDefaultMessage());
    }

    /**
     * 没有访问权限 Need Spring Security Supported
     * @param ex AccessDeniedException
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ApiResult<Object> accessDeniedExceptionHandler(AccessDeniedException ex) {
        logger.error("没有访问权限，{}", ex.getMessage(), ex);
        return ApiResult.failure(HttpStatus.UNAUTHORIZED.value(), "没有访问权限");
    }

    /**
     * 传输数据格式有误
     * @param ex HttpMessageNotReadableException
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult<Object> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
        logger.error("传输数据格式有误，{}", ex.getMessage(), ex);
        return ApiResult.failure("传输数据格式有误");
    }

    /**
     * 索引冲突
     * @param ex DuplicateKeyException
     */
    @ExceptionHandler(value = {DuplicateKeyException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiResult<Object> duplicateErrorHandler(HttpServletRequest req, Exception ex) {
        logger.error("接口: [URI]:{} 唯一索引冲突", req.getRequestURL(), ex);
        return ApiResult.failure("录入了重复的数据，请检查数据的唯一约束");
    }

    /**
     * 系统异常 预期以外异常
     * @param ex Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Object> handleUnexpectedServer(HttpServletRequest req, Exception ex) {
        logger.error("【系统异常】接口:{}，异常信息：{}", req.getRequestURL(), ex.getMessage(), ex);
        return ApiResult.failure(ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

}
