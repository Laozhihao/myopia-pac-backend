package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.PermissionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 权限模板接口
 *
 * @Author HaoHao
 * @Date 2021/1/25
 **/
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/permission/template")
public class PermissionTemplateController {

    @Autowired
    private OauthService oauthService;

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/{templateType}")
    public List<PermissionDTO> getPermissionTemplate(@PathVariable Integer templateType) {
        Assert.notNull(templateType, "模板类型不能为空");
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthService.getPermissionTemplate(templateType);
    }

    /**
     * 更新模板权限
     *
     * @param templateType 模板类型
     * @param permissionIds 权限集
     * @return boolean
     **/
    @PutMapping("/{templateType}")
    public boolean updatePermissionTemplate(@PathVariable Integer templateType, @RequestBody List<Integer> permissionIds) {
        Assert.notNull(templateType, "模板类型不能为空");
        Assert.notNull(permissionIds, "模板权限不能为空");
        Assert.isTrue(CurrentUserUtil.getCurrentUser().isPlatformAdminUser(), "没有访问权限");
        return oauthService.updatePermissionTemplate(templateType, permissionIds);
    }
}
