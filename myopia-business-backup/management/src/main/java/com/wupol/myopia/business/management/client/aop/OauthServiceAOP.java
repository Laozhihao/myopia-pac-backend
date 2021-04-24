package com.wupol.myopia.business.management.client.aop;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 对Oauth请求的转发和后处理
 * @author Chikong
 * @date 2021-01-18
 */
@Aspect
@Component
public class OauthServiceAOP {
    @Autowired
    private OauthServiceClient client;

    @Pointcut("@annotation(com.wupol.myopia.business.management.client.aop.annotation.OauthRequest)")
    public void request(){}

    @Around("request()")
    public Object doAroundAdvice(ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        // 方法名
        String methodName = methodSignature.getName();
        // 参数类型
        Class[] parameterTypes =  methodSignature.getParameterTypes();
        // 参数值
        Object[] args = point.getArgs();
        Method method = client.getClass().getMethod(methodName, parameterTypes);
        // 调用
        ApiResult result = (ApiResult) method.invoke(client, args);
        return getData(result);
    }

    /** 统一判断请求状态，如失败，抛异常 */
    private <T> T getData(ApiResult<T> result) throws BusinessException {
        if (!result.isSuccess()) {
            throw new BusinessException(result.getMessage(), result.getCode());
        }
        return result.getData();
    }
}
