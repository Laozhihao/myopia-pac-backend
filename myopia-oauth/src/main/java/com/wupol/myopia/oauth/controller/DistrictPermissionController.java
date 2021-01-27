package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.service.DistrictPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/districtPermission")
public class DistrictPermissionController {

    @Autowired
    private DistrictPermissionService districtPermissionService;

    /**
     * 根据模板类型获取模板权限-树结构
     *
     * @param templateType 模板类型
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/{templateType}")
    public List<Permission> getPermissionTemplate(@PathVariable Integer templateType) {
        Assert.notNull(templateType, "模板类型不能为空");
        return districtPermissionService.selectTemplatePermissionTree(templateType);
    }

    /**
     * 更新模板权限
     *
     * @param templateType 模板类型
     * @param permissionIds 权限集
     * @return boolean
     **/
    @PutMapping("/{templateType}")
    public boolean updatePermissionTemplate(@PathVariable Integer templateType, @RequestBody List<Integer> permissionIds) throws IOException {
        Assert.notNull(templateType, "模板类型不能为空");
        Assert.notNull(permissionIds, "模板权限不能为空");
        return districtPermissionService.updatePermissionTemplate(templateType, permissionIds);
    }

}
