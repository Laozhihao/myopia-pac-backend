package com.wupol.myopia.base.annotation;

import java.lang.annotation.*;

/**
 * 访问限制注解
 *
 * @author hang.yuan 2022/5/6 09:56
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitedAccess {

    /**
     * 一段时间内，最大访问频率，默认100
     */
    long frequency() default 300;

    /**
     * 一段时间，默认 60s
     */
    long second() default 60;
}
