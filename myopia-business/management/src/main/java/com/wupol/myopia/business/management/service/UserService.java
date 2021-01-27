package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class UserService {

    @Autowired
    private OauthService oauthService;
    @Autowired
    private GovDeptService govDeptService;

    /**
     * 分页获取用户列表
     *
     * @param param     查询参数
     * @param current   当前页码
     * @param size      每页条数
     * @param currentUserOrgId  当前用户所属部门ID
     * @return java.util.ArrayList<com.wupol.myopia.business.management.domain.dto.User>
     **/
    public IPage<UserDTO> getUserListPage(UserDTOQuery param, Integer current, Integer size, Integer currentUserOrgId) {
        // 默认获取自己所属部门及其下面所有部门的用户，如果搜索条件中部门ID不为空，则优先获取指定部门的用户
        if (Objects.isNull(param.getOrgId())) {
            List<Integer> orgIds = govDeptService.getAllSubordinateDepartmentIdByPid(currentUserOrgId);
            param.setOrgIds(orgIds);
        }
        param.setCurrent(current).setSize(size);
        Page<UserDTO> userPage = oauthService.getUserListPage(param);
        List<UserDTO> users = JSONObject.parseArray(JSONObject.toJSONString(userPage.getRecords()), UserDTO.class);
        if (CollectionUtils.isEmpty(users)) {
            return userPage;
        }
        List<Integer> ids = users.stream().map(UserDTO::getOrgId).collect(Collectors.toList());
        Map<Integer, String> nameMap = govDeptService.listByIds(ids).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        users.forEach(x -> x.setOrgName(nameMap.get(x.getOrgId())));
        return userPage.setRecords(users);
    }

    /**
     * 新增用户
     *
     * @param user 用户数据
     * @param currentUser  当前登录用户
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    public UserDTO addUser(UserDTO user, CurrentUser currentUser) {
        // 参数校验
        validateAndInitUserData(user, currentUser);
        // 新增用户并绑定角色
        user.setPassword(PasswordGenerator.getManagementUserPwd())
                .setUsername(user.getPhone())
                .setCreateUserId(currentUser.getId())
                .setSystemCode(currentUser.getSystemCode());
        return oauthService.addUser(user);
    }

    /**
     * 更新用户
     *
     * @param user 用户数据
     * @param currentUser  当前登录用户
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    public UserDTO updateUser(UserDTO user, CurrentUser currentUser) {
        // 参数校验
        validateAndInitUserData(user, currentUser);
        // 更新用户
        user.setUsername(user.getPhone()).setSystemCode(currentUser.getSystemCode());
        return oauthService.modifyUser(user);
    }

    /**
     * 校验并初始化用户数据
     *
     * @param user 用户数据
     * @param currentUser 当前登录用户
     * @return void
     **/
    public void validateAndInitUserData(UserDTO user, CurrentUser currentUser) {
        // 参数校验
        if (currentUser.isPlatformAdminUser()) {
            Assert.notNull(user.getUserType(), "用户类型不能为空");
            if (UserType.NOT_PLATFORM_ADMIN.getType().equals(user.getUserType())) {
                // 创建非平台管理员用户
                Assert.notNull(user.getOrgId(), "所属部门ID不能为空");
                Assert.notNull(user.getIsLeader(), "是否为领导不能为空");
            } else {
                // 创建平台管理员用户
                user.setOrgId(currentUser.getOrgId()).setIsLeader(null);
            }
        } else {
            // 非平台管理员创建的用户默认绑定到其所属部门下
            Assert.notNull(user.getIsLeader(), "是否为领导不能为空");
            user.setOrgId(currentUser.getOrgId());
        }
    }

    /**
     * 根据id批量获取用户
     * @param userIds 用户id列
     * @return  Map<用户id，用户>
     */
    public Map<Integer, UserDTO> getUserMapByIds(Set<Integer> userIds) {
        return getUserMapByIds(new ArrayList<>(userIds));
    }
    /**
     * 根据id批量获取用户
     * @param userIds 用户id列
     * @return  Map<用户id，用户>
     */
    public Map<Integer, UserDTO> getUserMapByIds(List<Integer> userIds) {
        return oauthService.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(UserDTO::getId, Function.identity()));
    }

}
