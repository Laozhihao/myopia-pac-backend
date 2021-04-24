package com.wupol.myopia.oauth.sdk.client;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.UserRequest;
import com.wupol.myopia.oauth.sdk.domain.request.PermissionDTO;
import com.wupol.myopia.oauth.sdk.domain.request.RoleDTO;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.LoginInfo;
import com.wupol.myopia.oauth.sdk.domain.response.Permission;
import com.wupol.myopia.oauth.sdk.domain.response.Role;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 容错异常获取与降级处理
 *
 * @Author HaoHao
 * @Date 2020/12/30
 **/
@Component
public class OauthServiceFallbackFactory implements FallbackFactory<OauthServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(OauthServiceFallbackFactory.class);
    private static final String SYSTEM_ERROR_MESSAGE = "系统异常，请联系管理员";


    @SuppressWarnings("unchecked")
    @Override
    public OauthServiceClient create(Throwable throwable) {
        logger.error("【调用Oauth服务异常】{}", throwable.getMessage(), throwable);
        FeignException feignException = (FeignException)throwable;
        String message  = getMsgFromBodyWidthDefault(feignException.getMessage(), feignException.content());
        ApiResult respData = ApiResult.failure(feignException.status(), message);

        return new OauthServiceClient() {

            @Override
            public Page<User> getUserListPage(UserDTO param) {
                return null;
            }

            @Override
            public List<User> getUserList(UserDTO param) {
                return null;
            }

            @Override
            public List<User> getUserBatchByIds(List<Integer> userIds) {
                return null;
            }

            @Override
            public List<User> getUserBatchByPhones(List<String> phones, Integer systemCode) {
                return null;
            }

            @Override
            public List<User> getUserBatchByIdCards(List<String> idCards, Integer systemCode, Integer orgId) {
                return null;
            }

            @Override
            public User addUser(UserDTO param) {
                return null;
            }

            @Override
            public User addMultiSystemUser(UserDTO param) {
                return null;
            }

            @Override
            public List<User> addScreeningUserBatch(List<UserDTO> param) {
                return null;
            }

            @Override
            public User modifyUser(UserDTO param) {
                return null;
            }

            @Override
            public User resetPwd(Integer userId, String password) {
                return null;
            }

            @Override
            public User getUserDetailByUserId(Integer userId) {
                return null;
            }

            @Override
            public Integer count(UserDTO queryParam) {
                return null;
            }

            @Override
            public List<Role> getRoleList(RoleDTO param) {
                return null;
            }

            @Override
            public Page<Role> getRoleListByPage(RoleDTO param) {
                return null;
            }

            @Override
            public Role addRole(RoleDTO param) {
                return null;
            }

            @Override
            public Role updateRole(RoleDTO param) {
                return null;
            }

            @Override
            public Role assignRolePermission(Integer roleId, List<Integer> permissionIds) {
                return null;
            }

            @Override
            public Role getRoleById(Integer roleId) {
                return null;
            }

            @Override
            public List<Permission> getRolePermissionTree(Integer roleId, Integer templateType) {
                return null;
            }

            @Override
            public List<Permission> getPermissionList(PermissionDTO param) {
                return null;
            }

            @Override
            public Permission addPermission(PermissionDTO param) {
                return null;
            }

            @Override
            public Permission modifyPermission(PermissionDTO param) {
                return null;
            }

            @Override
            public boolean deletePermission(Integer permissionId) {
                return false;
            }

            @Override
            public List<User> getUserByIds(UserRequest request) {
                return null;
            }

            @Override
            public List<Permission> getPermissionTemplate(Integer templateType) {
                return null;
            }

            @Override
            public List<Integer> getPermissionTemplateIdList(Integer templateType) {
                return null;
            }

            @Override
            public Boolean updatePermissionTemplate(Integer templateType, List<Integer> permissionIds) {
                return null;
            }

            @Override
            public List<User> getUserBatchByOrgIds(List<Integer> orgIds, Integer systemCode) {
                return null;
            }

            @Override
            public LoginInfo login(String clientId, String clientSecret, String username, String password) {
                return null;
            }
        };
    }

    private static String getMsgFromBodyWidthDefault(String message, byte[] body) {
        try {
            ApiResult result = JSONObject.parseObject(body, ApiResult.class);
            return Objects.nonNull(result) && !StringUtils.isEmpty(result.getMessage()) ? result.getMessage()
                    : StringUtils.isEmpty(message) ? SYSTEM_ERROR_MESSAGE : message;
        } catch (Exception e) {
            return SYSTEM_ERROR_MESSAGE;
        }
    }
}
