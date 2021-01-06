package com.wupol.myopia.base.util;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 当前登录用户信息处理工具
 *
 * @Author HaoHao
 * @Date 2020/12/27
 **/
@Component
public class CurrentUserUtil {

    /**
     * 获取当前登录用户
     *
     * @return com.wupol.myopia.base.domain.CurrentUser
     **/
    public static CurrentUser getCurrentUser(){
        // 从Header中获取用户信息
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            throw new BusinessException("获取当前登录的用户为空");
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String payload = request.getHeader(AuthConstants.JWT_PAYLOAD_KEY);
        if (StringUtils.isBlank(payload)) {
            throw new BusinessException("请登录");
        }
        String user = JSONObject.parseObject(payload).getString(AuthConstants.JWT_USER_KEY);
        return JSONObject.parseObject(user, CurrentUser.class);
    }

    public static CurrentUser getLegalCurrentUser() {
        CurrentUser currentUser = getCurrentUser();
        if (null == currentUser || null == currentUser.getOrgId() || null == currentUser.getId()) {
            throw new BusinessException("请登录");
        }
        return currentUser;
    }
}
