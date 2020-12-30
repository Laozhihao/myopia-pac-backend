package com.wupol.myopia.business.management.client;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.business.management.domain.dto.Permission;
import com.wupol.myopia.business.management.domain.dto.Role;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020/12/14
 **/
@Log4j2
@Component
public class OauthServiceFallback implements OauthServiceClient {

    @Override
    public ApiResult getUserListPage(UserDTO param) {
        log.error("【远程调用oauth】获取用户列表异常");
        return ApiResult.failure("获取用户列表失败");
    }

    @Override
    public ApiResult<List<UserDTO>> getUserBatchByIds(List<Integer> userIds) {
        log.error("【远程调用oauth】批量获取用户异常");
        return ApiResult.failure("批量获取用户失败");
    }

    @Override
    public ApiResult<UserDTO> addUser(UserDTO param) {
        log.error("【远程调用oauth】新增用户异常");
        return ApiResult.failure("新增用户失败");
    }

    @Override
    public ApiResult<UserDTO> addAdminUser(UserDTO param) {
        log.error("【远程调用oauth】新增用户异常");
        return ApiResult.failure("新增管理员用户失败");
    }

    @Override
    public ApiResult<List<Integer>> addScreeningUserBatch(List<UserDTO> param) {
        log.error("【远程调用oauth】批量新增筛查人员异常");
        return ApiResult.failure("批量新增筛查人员失败");
    }

    @Override
    public ApiResult<UserDTO> modifyUser(UserDTO param) {
        return null;
    }

    @Override
    public ApiResult resetPwd(Integer param) {
        return null;
    }

    @Override
    public ApiResult getRoleList(Role param) {
        return null;
    }

    @Override
    public ApiResult addRole(Role param) {
        return null;
    }

    @Override
    public ApiResult modifyRole(Role param) {
        return null;
    }

    @Override
    public ApiResult assignRolePermission(Integer roleId) {
        return null;
    }

    @Override
    public ApiResult getRolePermissionTree(Integer roleId) {
        return null;
    }

    @Override
    public ApiResult getPermissionList(Permission param) {
        return null;
    }

    @Override
    public ApiResult addPermission(Permission param) {
        return null;
    }

    @Override
    public ApiResult modifyPermission(Permission param) {
        return null;
    }

    @Override
    public ApiResult deletePermission(Integer permissionId) {
        return null;
    }
}
