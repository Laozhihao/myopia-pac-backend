package com.wupol.myopia.business.management.client;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.UserRequest;
import com.wupol.myopia.business.management.domain.dto.PermissionDTO;
import com.wupol.myopia.business.management.domain.dto.RoleDTO;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
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
            public ApiResult<Page<UserDTO>> getUserListPage(UserDTOQuery param) {
                return respData;
            }

            @Override
            public ApiResult<List<UserDTO>> getUserList(UserDTOQuery param) {
                return respData;
            }

            @Override
            public ApiResult<List<UserDTO>> getUserBatchByIds(List<Integer> userIds) {
                return respData;
            }

            @Override
            public ApiResult<UserDTO> addUser(UserDTO param) {
                return respData;
            }

            @Override
            public ApiResult<UserDTO> addAdminUser(UserDTO param) {
                return respData;
            }

            @Override
            public ApiResult<List<Integer>> addScreeningUserBatch(List<UserDTO> param) {
                return respData;
            }

            @Override
            public ApiResult<UserDTO> modifyUser(UserDTO param) {
                return respData;
            }

            @Override
            public ApiResult<UserDTO> resetPwd(Integer userId, String password) {
                return respData;
            }

            @Override
            public ApiResult<UserDTO> getUserDetailByUserId(Integer userId) {
                return respData;
            }

            @Override
            public ApiResult getRoleList(RoleDTO param) {
                return respData;
            }

            @Override
            public ApiResult addRole(RoleDTO param) {
                return respData;
            }

            @Override
            public ApiResult updateRole(RoleDTO param) {
                return respData;
            }

            @Override
            public ApiResult assignRolePermission(Integer roleId, List<Integer> permissionIds) {
                return respData;
            }

            @Override
            public ApiResult<RoleDTO> getRoleById(Integer roleId) {
                return respData;
            }

            @Override
            public ApiResult<List<PermissionDTO>> getRolePermissionTree(Integer roleId, Integer templateType) {
                return respData;
            }

            @Override
            public ApiResult<List<PermissionDTO>> getPermissionList(PermissionDTO param) {
                return respData;
            }

            @Override
            public ApiResult addPermission(PermissionDTO param) {
                return respData;
            }

            @Override
            public ApiResult modifyPermission(PermissionDTO param) {
                return respData;
            }

            @Override
            public ApiResult deletePermission(Integer permissionId) {
                return respData;
            }

            @Override
            public ApiResult<List<UserDTO>> getUserByIds(UserRequest request) {
                return respData;
            }

            @Override
            public ApiResult<List<PermissionDTO>> getPermissionTemplate(Integer templateType) {
                return respData;
            }

            @Override
            public ApiResult<List<Integer>> getPermissionTemplateIdList(Integer templateType) {
                return respData;
            }

            @Override
            public ApiResult<Boolean> updatePermissionTemplate(Integer templateType, List<Integer> permissionIds) {
                return respData;
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
