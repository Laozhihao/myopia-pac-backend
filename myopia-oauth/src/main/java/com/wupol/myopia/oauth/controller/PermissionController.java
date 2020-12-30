package com.wupol.myopia.oauth.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.service.PermissionService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/permission")
public class PermissionController extends BaseController<PermissionService, Permission> {

    /*@Autowired
    private PermissionService permissionService;

    @GetMapping("/list")
    public Object getPermissionList(Permission param) throws IOException {
        return permissionService.findByList(param);
    }

    @PostMapping()
    public Object addPermission(@RequestBody Permission param) {
        return permissionService.save(param);
    }

    @PutMapping()
    public Object modifyPermission(@RequestBody Permission param) {
        return permissionService.updateById(param);
    }

    @DeleteMapping("/{permissionId}")
    public Object deletePermission(@PathVariable("permissionId") Integer permissionId) {
        return permissionService.removeById(permissionId);
    }*/
}
