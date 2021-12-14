package com.wupol.myopia.oauth.service;

import cn.hutool.json.JSONUtil;
import com.nimbusds.jose.JWSObject;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.AuthConstants;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.domain.model.Role;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/1/17
 **/
@Service
public class AuthService {

    /** 刷新token后，旧 access token 的有效期，单位：秒 */
    public static final int OLD_ACCESS_TOKEN_EXPIRES_TIME = 300;

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RoleService roleService;

    public List<Permission> cachePermissionAndToken(Integer userId, Integer systemCode, Integer userType, long expiresTime, String accessToken) {
        cacheUserAccessToken(userId, accessToken, expiresTime);
        return cacheUserPermission(userId, systemCode, userType, expiresTime);
    }

    /**
     * 缓存用户权限
     *
     * @param userId 用户名ID
     * @param systemCode 系统编号
     * @param expiresTime 缓存过期时间（秒）
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    private List<Permission> cacheUserPermission(Integer userId, Integer systemCode, Integer userType, long expiresTime) {
        // 只有管理端的用户需要缓存权限
        if (!SystemCode.MANAGEMENT_CLIENT.getCode().equals(systemCode)) {
            return new ArrayList<>();
        }
        // 通过角色获取权限
        List<Role> roles = roleService.getUsableRoleByUserId(userId, systemCode, userType);
        List<Permission> permissions = permissionService.getUsablePermissionByRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(permissions)) {
            throw new BusinessException("没有访问权限");
        }

        List<Object> apiPermissionPaths = getApiPermission(permissions);
        cacheUserPermission(userId, apiPermissionPaths, expiresTime);
        return permissions;
    }

    /**
     * 缓存用户权限
     *
     * @param userId 用户ID
     * @param permissionList 用户权限集合
     * @param expiresTime 过期时间（秒）
     * @return void
     **/
    private void cacheUserPermission(Integer userId, List<Object> permissionList, long expiresTime) {
        Assert.notNull(userId, "【缓存用户权限】用户ID为空！");
        redisUtil.lSet(String.format(RedisConstant.USER_PERMISSION_KEY, userId), permissionList, expiresTime);
    }

    /**
     * 延长权限缓存过期时间
     *
     * @param accessToken 令牌
     * @param refreshToken 刷新令牌
     * @param newExpiresTime 新过期时间
     * @return void
     **/
    public void delayPermissionAndTokenCache(String accessToken, String refreshToken, long newExpiresTime) throws ParseException {
        CurrentUser currentUser = parseToken(refreshToken);
        // 保留旧 accessToken 5分钟，以便新旧token正常过渡
        Object oldAccessToken = redisUtil.get(String.format(RedisConstant.USER_AUTHORIZATION_KEY, currentUser.getId()));
        redisUtil.set(String.format(RedisConstant.USER_AUTHORIZATION_OLD_KEY, currentUser.getId()), oldAccessToken, OLD_ACCESS_TOKEN_EXPIRES_TIME);
        // 缓存权限和新token
        cacheUserPermission(currentUser.getId(), currentUser.getSystemCode(), currentUser.getUserType(), newExpiresTime);
        cacheUserAccessToken(currentUser.getId(), accessToken, newExpiresTime);
    }

    /**
     * 获取用户api权限
     *
     * @param permissions 权限列表
     * @return java.util.List<java.lang.Object>
     **/
    private List<Object> getApiPermission(List<Permission> permissions) {
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .filter(x -> x.getIsPage().equals(AuthConstants.IS_API_PERMISSION) && !StringUtils.isEmpty(x.getApiUrl()))
                .map(Permission::getApiUrl)
                .collect(Collectors.toList());
    }

    /**
     * 缓存用户授权token
     *
     * @param userId
     * @param accessToken
     * @param expiresTime
     * @return void
     **/
    private void cacheUserAccessToken(Integer userId, String accessToken, long expiresTime) {
        Assert.notNull(userId, "【缓存用户accessToken】用户ID为空！");
        Assert.hasLength(accessToken, "【缓存用户accessToken】accessToken为空！");
        redisUtil.set(String.format(RedisConstant.USER_AUTHORIZATION_KEY, userId), accessToken, expiresTime);
    }

    /**
     * 解析token
     *
     * @param token refreshToken或者accessToken
     * @return com.wupol.myopia.base.domain.CurrentUser
     **/
    public CurrentUser parseToken(String token) throws ParseException {
        Assert.hasLength(token, "token为空！");
        JWSObject jwsObject = JWSObject.parse(token.replace(AuthConstants.JWT_TOKEN_PREFIX, Strings.EMPTY));
        CurrentUser currentUser = JSONUtil.parseObj(jwsObject.getPayload().toString()).get(AuthConstants.JWT_USER_INFO_KEY, CurrentUser.class);
        Assert.notNull(currentUser, "无效token！");
        return currentUser;
    }
}
