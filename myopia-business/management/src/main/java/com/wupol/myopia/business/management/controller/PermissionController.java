package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.PermissionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

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
    public Object getPermissionList(PermissionDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthService.getPermissionList(param);
    }

    /**
     * 新增权限资源 TODO：参数校验
     *
     * @param param 权限资源数据
     * @return java.lang.Object
     **/
    @PostMapping()
    public Object addPermission(@RequestBody PermissionDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        param.setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode());
        return oauthService.addPermission(param);
    }

    /**
     * 更新权限资源 TODO：参数校验
     *
     * @param param 权限资源数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public Object modifyPermission(@RequestBody PermissionDTO param) {
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthService.modifyPermission(param);
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
