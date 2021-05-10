package com.wupol.myopia.base.handler;

import com.wupol.myopia.base.domain.ApiResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.Annotation;

/**
 * 接口返回数据统一包装处理
 *
 * @Author HaoHao
 * @Date 2020/12/20
 **/
@Log4j2
@RestControllerAdvice(basePackages = { "com.wupol.myopia" })
public class ResponseBodyHandler implements ResponseBodyAdvice<Object> {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = ResponseResultBody.class;

    /**
     * 判断类或者方法是否使用了 @ResponseResultBody 注解
     *
     * @param returnType    返回类型
     * @param converterType 信息转换器
     * @return boolean
     **/
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ANNOTATION_TYPE) || returnType.hasMethodAnnotation(ANNOTATION_TYPE);
    }

    /**
     * 当类或者方法使用了 @ResponseResultBody 注解，就会调用这个方法
     *
     * @param body                  接口响应数据
     * @param returnType            返回类型
     * @param selectedContentType   媒体类型
     * @param selectedConverterType 转换器类型
     * @param request               请求上下文
     * @param response              响应上下文
     * @return java.lang.Object
     **/
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResult || body instanceof FileSystemResource) {
            return body;
        }
        return ApiResult.success(body);
    }

}