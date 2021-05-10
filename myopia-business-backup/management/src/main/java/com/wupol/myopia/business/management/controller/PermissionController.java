package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.PermissionDTO;
import com.wupol.myopia.business.management.validator.PermissionAddValidatorGroup;
import com.wupol.myopia.business.management.validator.PermissionUpdateValidatorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

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

    @Autowired
    private OauthService oauthService;

    /**
     * 获取权限资料列表
     *
     * @param param 查询参数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public List<PermissionDTO> getPermissionList(PermissionDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthService.getPermissionList(param);
    }

    /**
     * 新增权限资源
     *
     * @param param 权限资源数据
     * @return java.lang.Object
     **/
    @PostMapping()
    public PermissionDTO addPermission(@RequestBody @Validated(value = PermissionAddValidatorGroup.class) PermissionDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        Assert.isTrue(param.getIsPage() == 1 || !StringUtils.isEmpty(param.getApiUrl()), "功能接口url不能为空");
        // 非页面时，必为非菜单
        param.setIsMenu(param.getIsPage() == 0 ? 0 : param.getIsMenu());
        return oauthService.addPermission(param.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()));
    }

    /**
     * 更新权限资源
     *
     * @param param 权限资源数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public PermissionDTO updatePermission(@RequestBody @Validated(value = PermissionUpdateValidatorGroup.class) PermissionDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        Assert.isTrue(param.getIsPage() == 1 || !StringUtils.isEmpty(param.getApiUrl()), "功能接口url不能为空");
        // 非页面时，必为非菜单
        param.setIsMenu(param.getIsPage() == 0 ? 0 : param.getIsMenu());
        return oauthService.modifyPermission(param.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode()));
    }

    /**
     * 删除权限资源
     *
     * @param permissionId 权限资源ID
     * @return java.lang.Object
     **/
    @DeleteMapping("/{permissionId}")
    public Object deletePermission(@PathVariable("permissionId") Integer permissionId) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthService.deletePermission(permissionId);
    }

}
