package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.RegExpUtil;
import com.wupol.myopia.business.api.management.domain.dto.PermissionQueryDTO;
import com.wupol.myopia.business.api.management.validator.PermissionAddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.PermissionUpdateValidatorGroup;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Permission;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/permission")
public class PermissionController {

    @Resource
    private OauthServiceClient oauthServiceClient;

    /**
     * 获取权限资料列表
     *
     * @param param 查询参数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public List<Permission> getPermissionList(PermissionQueryDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthServiceClient.getPermissionList(param.convertToPermissionDTO());
    }

    /**
     * 新增权限资源
     *
     * @param param 权限资源数据
     * @return java.lang.Object
     **/
    @PostMapping()
    public Permission addPermission(@RequestBody @Validated(value = PermissionAddValidatorGroup.class) PermissionQueryDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        Assert.isTrue(param.getIsPage() == 1 || !StringUtils.isEmpty(param.getApiUrl()), "功能接口url不能为空");
        // 非页面时，必为非菜单
        param.setIsMenu(param.getIsPage() == 0 ? 0 : param.getIsMenu());
        param.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode());
        return oauthServiceClient.addPermission(param.convertToPermissionDTO());
    }

    /**
     * 更新权限资源
     *
     * @param param 权限资源数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public Permission updatePermission(@RequestBody @Validated(value = PermissionUpdateValidatorGroup.class) PermissionQueryDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        Assert.isTrue(param.getIsPage() == 1 || RegExpUtil.isApiUrl(param.getApiUrl()), "功能接口url参数格式错误");
        // 非页面时，必为非菜单
        param.setIsMenu(param.getIsPage() == 0 ? 0 : param.getIsMenu());
        param.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode());
        return oauthServiceClient.updatePermission(param.convertToPermissionDTO());
    }

    /**
     * 删除权限资源
     *
     * @param permissionId 权限资源ID
     * @return java.lang.Object
     **/
    @DeleteMapping("/{permissionId}")
    public boolean deletePermission(@PathVariable("permissionId") Integer permissionId) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthServiceClient.deletePermission(permissionId);
    }

}
