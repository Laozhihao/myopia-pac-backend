package com.wupol.myopia.oauth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.dto.UserDTO;
import com.wupol.myopia.oauth.domain.model.User;
import com.wupol.myopia.oauth.domain.model.UserWithRole;
import com.wupol.myopia.oauth.service.UserService;
import com.wupol.myopia.oauth.validator.UserValidatorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author HaoHao
 * @Date 2020-12-25
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表 - 分页
     *
     * @param queryParam 查询参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.oauth.domain.model.UserWithRole>
     **/
    @GetMapping("/page")
    public IPage<UserWithRole> getUserListPage(UserDTO queryParam) {
        return userService.getUserListPage(queryParam);
    }

    /**
     * 新增用户
     * TODO: 参数判空校验
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @PostMapping()
    public User addUser(@RequestBody UserDTO userDTO) throws IOException {
        return userService.addUser(userDTO);
    }

    /**
     * 修改用户
     *
     * @param user 用户数据
     * @return java.lang.Object
     **/
    @PutMapping()
    public UserWithRole updateUser(@RequestBody UserDTO user) throws Exception {
        return userService.updateUser(user);
    }

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @return java.lang.Object
     **/
    @PutMapping("/password/{userId}")
    public User resetPwd(@PathVariable Integer userId, String password) {
        return userService.resetPwd(userId, password);
    }

    /**
     * 管理端创建医院端、学校端、筛查端的管理员
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.oauth.domain.model.User
     **/
    @PostMapping("/admin")
    public User addAdminUser(@RequestBody @Validated(value = UserValidatorGroup.class) UserDTO userDTO) throws IOException {
        return userService.addAdminUser(userDTO);
    }

    /**
     * 批量新增筛查人员
     *
     * @param userList 用户数据集合
     * @return java.util.List<java.lang.Integer>
     **/
    @PostMapping("/screening/batch")
    public List<User> addScreeningUserBatch(@RequestBody List<UserDTO> userList) {
        return userService.addScreeningUserBatch(userList);
    }

    /**
     * 根据用户ID集批量获取用户
     *
     * @param userIds 用户ID集合
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/batch/id")
    public List<User> getUserBatchByIds(@RequestParam("userIds") List<Integer> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        return userService.listByIds(userIds);
    }

    /**
     * 根据手机号码集批量获取用户
     *
     * @param phones 手机号码集合
     * @param systemCode 系统编号
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/batch/phone")
    public List<User> getUserBatchByPhones(@RequestParam("userIds") List<String> phones, @RequestParam("systemCode") Integer systemCode) {
        if (CollectionUtils.isEmpty(phones)) {
            return new ArrayList<>();
        }
        return userService.getUserBatchByPhones(phones, systemCode);
    }

    /**
     * 获取用户明细
     *
     * @param userId 用户ID
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/{userId}")
    public User getUserDetailByUserId(@PathVariable("userId") Integer userId) {
        return userService.getById(userId);
    }

    /**
     * 获取用户列表（仅支持按名称模糊查询）
     *
     * @param queryParam 搜索参数
     * @return java.util.List<com.wupol.myopia.oauth.domain.model.User>
     **/
    @GetMapping("/list")
    public List<User> getUserListByNameLike(UserDTO queryParam) {
        return userService.getUserListByNameLike(queryParam.getRealName());
    }

    /**
     * 统计
     *
     * @param queryParam 查询条件
     * @return java.lang.Integer
     **/
    @GetMapping("/count")
    public Integer count(UserDTO queryParam) throws IOException {
        return userService.count(queryParam);
    }
}
