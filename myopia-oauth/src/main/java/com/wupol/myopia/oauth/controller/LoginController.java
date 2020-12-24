package com.wupol.myopia.oauth.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.oauth.domain.dto.LoginDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * SSO 表单接口, 暂只用于EW + RC的账号密码/手机验证码登陆
     * <p>
     * 1) 必须是网页端发起此接口
     * <p>
     * 2) 必须明确自己的重定向回调地址， 必须匹配同源规则
     * <p>
     * 3）由于是定制化开发，错误信息将固定回调到/loginError?errorCode=-1
     */
    @PostMapping(value = "/login")
    public ApiResult login(@Validated LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        logger.info("request login {}", request.getParameterMap());
        return ApiResult.success();
    }
}
