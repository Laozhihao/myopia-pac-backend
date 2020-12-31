package com.wupol.myopia.business.management.client;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.management.domain.dto.Permission;
import com.wupol.myopia.business.management.domain.dto.Role;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
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

    @Override
    public OauthServiceClient create(Throwable throwable) {
        logger.error("【调用Oauth服务异常】{}", throwable.getMessage(), throwable);
        FeignException feignException = (FeignException)throwable;
        String message  = getMsgFromBodyWidthDefault(feignException.getMessage(), feignException.content());
        ApiResult respData = ApiResult.failure(feignException.status(), message);

        return new OauthServiceClient() {
            @Override
            public ApiResult getUserListPage(UserDTO param) {
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
            public ApiResult resetPwd(Integer userId) {
                return respData;
            }

            @Override
            public ApiResult getRoleList(Role param) {
                return respData;
            }

            @Override
            public ApiResult addRole(Role param) {
                return respData;
            }

            @Override
            public ApiResult modifyRole(Role param) {
                return respData;
            }

            @Override
            public ApiResult assignRolePermission(Integer roleId) {
                return respData;
            }

            @Override
            public ApiResult getRolePermissionTree(Integer roleId) {
                return respData;
            }

            @Override
            public ApiResult getPermissionList(Permission param) {
                return respData;
            }

            @Override
            public ApiResult addPermission(Permission param) {
                return respData;
            }

            @Override
            public ApiResult modifyPermission(Permission param) {
                return respData;
            }

            @Override
            public ApiResult deletePermission(Integer permissionId) {
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
