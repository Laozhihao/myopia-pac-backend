package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.domain.dto.Permission;
import org.springframework.beans.factory.annotation.Autowired;
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
    private OauthServiceClient oauthServiceClient;

    @GetMapping("/list")
    public Object getPermissionList(Permission param) {
        return oauthServiceClient.getPermissionList(param);
    }

    @PostMapping()
    public Object addPermission(@RequestBody Permission param) {
        return oauthServiceClient.addPermission(param);
    }

    @PutMapping()
    public Object modifyPermission(@RequestBody Permission param) {
        return oauthServiceClient.modifyPermission(param);
    }

    @DeleteMapping("/{permissionId}")
    public Object deletePermission(@PathVariable("permissionId") Integer permissionId) {
        return oauthServiceClient.deletePermission(permissionId);
    }

}
