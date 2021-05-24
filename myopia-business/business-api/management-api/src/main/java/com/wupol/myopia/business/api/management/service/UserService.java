package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.api.management.domain.dto.UserQueryDTO;
import com.wupol.myopia.business.api.management.domain.vo.UserVO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.dto.GovDeptDTO;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class UserService {

    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;

    /**
     * 分页获取用户列表
     *
     * @param param     查询参数
     * @param current   当前页码
     * @param size      每页条数
     * @param currentUser  当前用户
     * @return java.util.ArrayList<com.wupol.myopia.business.management.domain.dto.User>
     **/
    public IPage<UserVO> getUserListPage(UserQueryDTO param, Integer current, Integer size, CurrentUser currentUser) {
        // 非平台管理员，只能看到自己部门下的用户
        if (!currentUser.isPlatformAdminUser()) {
            param.setOrgId(currentUser.getOrgId());
        }
        // 默认获取自己所属部门及其下面所有部门的用户，如果搜索条件中部门ID不为空，则优先获取指定部门的用户
        param.setCurrent(current).setSize(size).setSystemCode(currentUser.getSystemCode());
        // 根据部门名称模糊查询
        if (!StringUtils.isEmpty(param.getOrgName())) {
            List<GovDept> govDeptList = govDeptService.getGovDeptList(new GovDept().setName(param.getOrgName()));
            if (CollectionUtils.isEmpty(govDeptList)) {
                return new Page<>(current, size);
            }
            param.setOrgIds(govDeptList.stream().map(GovDept::getId).collect(Collectors.toList()));
        }
        // 调用远程服务获取用户数据
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(param, userDTO);
        Page<User> userPage = oauthServiceClient.getUserListPage(userDTO);
        List<UserVO> users = JSON.parseArray(JSON.toJSONString(userPage.getRecords()), UserVO.class);
        if (CollectionUtils.isEmpty(users)) {
            return new Page<>(current, size);
        }
        // 获取部门信息和行政区信息
        List<Integer> govDeptIds = users.stream().map(UserVO::getOrgId).distinct().collect(Collectors.toList());
        Map<Integer, GovDeptDTO> govDeptMap = govDeptService.getGovDeptMapByIds(govDeptIds);
        List<UserVO> userVOList = users.stream().map(userVO -> {
            GovDeptDTO govDeptVo = govDeptMap.get(userVO.getOrgId());
            if (Objects.isNull(govDeptVo)) {
                return userVO;
            }
            userVO.setOrgName(govDeptVo.getName());
            userVO.setDistrictDetail(districtService.getDistrictPositionDetail(govDeptVo.getDistrict()));
            return userVO;
        }).collect(Collectors.toList());
        return new Page<UserVO>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal()).setRecords(userVOList);
    }

    /**
     * 新增用户
     *
     * @param user 用户数据
     * @param currentUser  当前登录用户
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    public User addUser(UserQueryDTO user, CurrentUser currentUser) {
        // 参数校验
        validateAndInitUserData(user, currentUser);
        // 新增用户并绑定角色
        user.setPassword(PasswordGenerator.getManagementUserPwd())
                .setUsername(user.getPhone())
                .setCreateUserId(currentUser.getId())
                .setSystemCode(currentUser.getSystemCode());
        return oauthServiceClient.addUser(user.convertToOauthUserDTO());
    }

    /**
     * 更新用户
     *
     * @param user 用户数据
     * @param currentUser  当前登录用户
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    public UserVO updateUser(UserQueryDTO user, CurrentUser currentUser) {
        // 参数校验
        validateAndInitUserData(user, currentUser);
        // 管理端用户默认手机号码作为用户账号
        if (!StringUtils.isEmpty(user.getPhone())) {
            user.setUsername(user.getPhone());
        }
        // 该接口不允许更新密码
        user.setSystemCode(currentUser.getSystemCode()).setPassword(null);
        User newUser = oauthServiceClient.updateUser(user.convertToOauthUserDTO());
        GovDept govDept = govDeptService.getById(newUser.getOrgId());
        District district = districtService.getById(govDept.getDistrictId());
        UserVO userVO = JSON.parseObject(JSON.toJSONString(user), UserVO.class);
        return userVO.setOrgName(govDept.getName()).setDistrictDetail(districtService.getDistrictPositionDetail(district));
    }

    /**
     * 校验并初始化用户数据
     *
     * @param user 用户数据
     * @param currentUser 当前登录用户
     * @return void
     **/
    private void validateAndInitUserData(UserQueryDTO user, CurrentUser currentUser) {
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
            // TODO：校验角色是否都与当前用户属于同一部门
        }
        user.setCreateUserId(currentUser.getId());
    }

    /**
     * 根据id批量获取用户
     * @param userIds 用户id列
     * @return  Map<用户id，用户>
     */
    public Map<Integer, User> getUserMapByIds(List<Integer> userIds) {
        return oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    /**
     * 校验权限
     *
     * @param currentUser 当前登录用户
     * @param operatedUserId 被操作用户ID
     * @return void
     **/
    public void validatePermission(CurrentUser currentUser, Integer operatedUserId) {
        User user = oauthServiceClient.getUserDetailByUserId(operatedUserId);
        Assert.notNull(user, "不存在该用户");
        if (!currentUser.isPlatformAdminUser()) {
            Assert.isTrue(user.getOrgId().equals(currentUser.getOrgId()), "没有操作权限，只能修改自己部门的用户");
            Assert.isTrue(!currentUser.getId().equals(user.getId()), "没有操作权限，不能更新自己信息");
        }
    }

    /**
     * 重置管理端用户密码
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    public UserVO resetPwd(Integer userId) {
        String pwd = PasswordGenerator.getManagementUserPwd();
        oauthServiceClient.resetPwd(userId, pwd);
        return new UserVO().setId(userId).setPassword(pwd);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    public User updateUserStatus(Integer userId, Integer status) {
        Assert.notNull(userId, "userId不能为空");
        Assert.notNull(status, "status不能为空");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId).setStatus(status);
        return oauthServiceClient.updateUser(userDTO);
    }

}
