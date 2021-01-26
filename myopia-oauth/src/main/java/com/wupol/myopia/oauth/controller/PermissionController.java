package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public Object getPermissionList() {
        return permissionService.getAllPermissionTree();
    }

    @PostMapping()
    public Permission addPermission(@RequestBody @Valid Permission param) {
        permissionService.save(param);
        return param;
    }

    @PutMapping()
    public Permission modifyPermission(@RequestBody @Valid Permission param) {
        permissionService.updateById(param);
        return param;
    }

    @DeleteMapping("/{permissionId}")
    public Object deletePermission(@PathVariable("permissionId") Integer permissionId) {
        return permissionService.removeById(permissionId);
    }
}
