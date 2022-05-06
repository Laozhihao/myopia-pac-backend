package com.wupol.myopia.base.annotation;

import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 访问限制切面
 *
 * @author hang.yuan 2022/5/6 10:00
 */
@Slf4j
@Aspect
@Component
public class LimitedAccessAspect {

    private final RedisUtil redisUtil;
    private static final String LIMITED_ACCESS_KEY="limitedAccess:%s:%s:%s";

    public LimitedAccessAspect(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 限制注解切面方法
     */
    @Pointcut("@annotation(limitedAccess)")
    public void limitedAccessPointcut(LimitedAccess limitedAccess){

    }

    @Around(value = "limitedAccessPointcut(limitedAccess)",argNames = "point,limitedAccess")
    public Object doAround(ProceedingJoinPoint point,LimitedAccess limitedAccess) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(requestAttributes)){
            String className = point.getTarget().getClass().getName();
            String methodName = point.getSignature().getName();
            HttpServletRequest request = requestAttributes.getRequest();
            String remoteAddr = request.getRemoteAddr();
            String key = String.format(LIMITED_ACCESS_KEY, className, methodName, remoteAddr);
            Object obj = redisUtil.get(key);
            if (Objects.nonNull(obj) && (int) obj > 0){
                if ((int) obj >= limitedAccess.frequency()){
                    log.warn("接口调用过于频繁 {}",key);
                    throw new BusinessException("接口调用过于频繁");
                }
                redisUtil.incr(key,1);
            }else {
                redisUtil.set(key,1,limitedAccess.second());
            }
        }

        return point.proceed();
    }

}
