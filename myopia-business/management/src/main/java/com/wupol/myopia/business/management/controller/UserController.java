package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author HaoHao
 * @Date 2020-12-25
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/user")
public class UserController {

    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private UserService userService;

    /**
     * 分页获取用户列表
     *
     * @param queryParam     查询参数
     * @param current   当前页码
     * @param size      每页条数
     * @return java.lang.Object
     **/
    @GetMapping("/list")
    public IPage<UserDTO> getUserListPage(UserDTO queryParam,
                                          @RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        // TODO: 支持根据日期查询
        return userService.getUserListPage(queryParam, current, size);
    }

    /**
     * 新增用户
     * TODO: 参数判空校验
     * @param user 用户数据
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    @PostMapping()
    public UserDTO addUser(@RequestBody UserDTO user) {
        return userService.addUser(user);
    }

    @PutMapping()
    public Object modifyUser(@RequestBody UserDTO user) {
        return oauthServiceClient.modifyUser(user);
    }

    @PutMapping("/password/{userId}")
    public Object resetPwd(@PathVariable("userId") Integer userId) {
        return oauthServiceClient.resetPwd(userId);
    }
}
