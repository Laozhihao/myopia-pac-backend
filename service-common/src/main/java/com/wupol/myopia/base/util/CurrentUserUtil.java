package com.wupol.myopia.base.util;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.domain.ResultCode;
import com.wupol.myopia.base.exception.AccessException;
import com.wupol.myopia.base.exception.BusinessException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 当前登录用户信息处理工具
 *
 * @Author HaoHao
 * @Date 2020/12/27
 **/
@UtilityClass
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
            throw new AccessException("请登录");
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String payload = request.getHeader(AuthConstants.JWT_PAYLOAD_KEY);
        if (StringUtils.isBlank(payload)) {
            throw new AccessException("请登录");
        }
        String user = JSON.parseObject(payload).getString(AuthConstants.JWT_USER_INFO_KEY);
        CurrentUser currentUser = JSON.parseObject(user, CurrentUser.class);
        if (Objects.isNull(currentUser)) {
            throw new AccessException("请登录");
        }
        return currentUser;
    }

    public static void isNeedPlatformAdminUser(CurrentUser user) {
        if (!user.isPlatformAdminUser()) {
            throw new BusinessException(ResultCode.USER_ACCESS_UNAUTHORIZED.getMessage(), ResultCode.USER_ACCESS_UNAUTHORIZED.getCode());
        }
    }

    public static String getUserToken () {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            throw new AccessException("请登录");
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request.getHeader(AuthConstants.JWT_TOKEN);
    }

}
