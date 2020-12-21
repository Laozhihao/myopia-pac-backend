package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.base.handler.ResponseResultBody;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author HaoHao
 * @Date 2020/12/19
 **/
@ResponseResultBody
@Log4j2
@RefreshScope
@RestController
@RequestMapping("business/sample")
public class StudentController {

    @Value("${business.nickname}")
    private  String nickname;
    @Value("${business.age}")
    private  Integer age;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @RequestMapping("/test-config")
    public Object testLocalConfig() {
        log.info("[business] test config...");
        Map<String, Object> map = new HashMap<>(3);
        map.put("nickname", nickname);
        map.put("age", age);
        return map;
    }

    @RequestMapping("/test-feign")
    public ApiResult testFeign() {
        log.info("[business] test feign...");
        return oauthServiceClient.login("go...");
    }

    @RequestMapping("/test-fallback")
    public Object testFallback() {
        log.info("[business] test fallback...");
        int i = 1/0;
        return null;
    }
}
