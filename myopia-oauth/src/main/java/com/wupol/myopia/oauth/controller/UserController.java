package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author HaoHao
 * @Date 2020/12/20
 **/
@ResponseResultBody
@Log4j2
@Validated
@RestController
public class UserController {
    @Value("${oauth.nickname}")
    private  String nickname;
    @Value("${oauth.age}")
    private  Integer age;

    @GetMapping("hello")
    public Object helloWorld(@NotBlank(message = "key不能为空") String key) {
        log.info("[oauth] hello world!");
        Map<String, Object> map = new HashMap<>(4);
        map.put("nickname", nickname);
        map.put("age", age);
        map.put("key", key);
        return map;
    }

    @GetMapping("login")
    public Object login(@NotBlank(message = "data不能为空") String data) throws InterruptedException {
        log.info("[oauth] login...");
        Thread.sleep(5000L);
        Map<String, Object> map = new HashMap<>(3);
        map.put("data", data);
        map.put("msg", "接收到Feign请求");
        return map;
    }
}
