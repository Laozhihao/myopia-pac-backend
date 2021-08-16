package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取全量权限资源
     *
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.Permission>
     **/
    @GetMapping("/list")
    public List<Permission> getPermissionList() {
        return permissionService.getAllPermissionTree();
    }

    /**
     * 新增
     *
     * @param param 权限资源数据
     * @return com.wupol.myopia.oauth.domain.model.Permission
     **/
    @PostMapping()
    public Permission addPermission(@RequestBody @Valid Permission param) {
        permissionService.validateParam(param);
        permissionService.save(param);
        return param;
    }

    @PutMapping()
    public Permission modifyPermission(@RequestBody @Valid Permission param) {
        if (!StringUtils.isEmpty(param.getMenuBtnName())) {
            List<Permission> exist = permissionService.findByList(new Permission().setMenuBtnName(param.getMenuBtnName()));
            Assert.isTrue(CollectionUtils.isEmpty(exist) || exist.get(0).getId().equals(param.getId()), "该页面或按钮name已经存在");
        }
        permissionService.updateById(param);
        return param;
    }

    @DeleteMapping("/{permissionId}")
    public boolean deletePermission(@PathVariable("permissionId") Integer permissionId) {
        return permissionService.removeById(permissionId);
    }
}
