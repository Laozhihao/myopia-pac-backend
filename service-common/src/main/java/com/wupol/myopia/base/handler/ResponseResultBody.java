package com.wupol.myopia.base.handler;

import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * 加了该注解的类或方法的接口，其返回数据会包装到ApiResult的data中
 *
 * @Author HaoHao
 * @Date 2020/12/20
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ResponseBody
public @interface ResponseResultBody {

}