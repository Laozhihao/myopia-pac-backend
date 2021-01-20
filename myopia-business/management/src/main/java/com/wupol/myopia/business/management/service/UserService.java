package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
     * @param userDTO 用户数据
     * @param currentUserOrgId  当前用户所属部门ID
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    public UserDTO addUser(UserDTO userDTO, Integer currentUserOrgId) {
        List<Integer> orgIds = govDeptService.getAllSubordinateDepartmentIdByPid(currentUserOrgId);
        if (!orgIds.contains(userDTO.getOrgId())) {
            throw new BusinessException("无效部门ID");
        }
        userDTO.setPassword(PasswordGenerator.getManagementUserPwd())
                .setUsername(userDTO.getPhone())
                .setCreateUserId(CurrentUserUtil.getCurrentUser().getId())
                .setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode());
        return oauthService.addUser(userDTO);
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
