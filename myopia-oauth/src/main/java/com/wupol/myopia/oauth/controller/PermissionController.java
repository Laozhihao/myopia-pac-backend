package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/permission")
public class PermissionController{

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
    public Object addPermission(@RequestBody @Valid Permission param) {
        return permissionService.save(param);
    }

    @PutMapping()
    public Object modifyPermission(@RequestBody @Valid Permission param) {
        return permissionService.updateById(param);
    }

    @DeleteMapping("/{permissionId}")
    public Object deletePermission(@PathVariable("permissionId") Integer permissionId) {
        return permissionService.removeById(permissionId);
    }
}
