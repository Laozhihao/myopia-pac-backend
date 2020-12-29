package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.model.GovDept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-23
 */
@Service
public class UserService {

    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private GovDeptService govDeptService;

    /**
     * 分页获取用户列表
     *
     * @param param     查询参数
     * @param current   当前页码
     * @param size      每页条数
     * @return java.util.ArrayList<com.wupol.myopia.business.management.domain.dto.User>
     **/
    public IPage<UserDTO> getUserListPage(UserDTO param, Integer current, Integer size) {
        List<Integer> orgIds = govDeptService.getCurrentUserAllSubordinateDepartmentId();
        param.setCurrent(current).setSize(size).setOrgIds(orgIds);
        ApiResult result = oauthServiceClient.getUserListPage(param);
        if (!result.isSuccess()) {
            throw new BusinessException("获取用户列表异常");
        }
        Page<UserDTO> userPage = JSONObject.parseObject(JSONObject.toJSONString(result.getData()), Page.class);
        List<UserDTO> users = JSONObject.parseArray(JSONObject.toJSONString(userPage.getRecords()), UserDTO.class);
        if (CollectionUtils.isEmpty(users)) {
            return userPage;
        }
        List<Integer> ids = users.stream().map(UserDTO::getOrgId).collect(Collectors.toList());
        Map<Integer, String> nameMap = govDeptService.listByIds(ids).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        users.forEach(x -> x.setOrgName(nameMap.get(x.getOrgId())));
        return userPage;
    }

    /**
     * 新增用户
     *
     * @param userDTO 用户数据
     * @return com.wupol.myopia.business.management.domain.dto.UserDTO
     **/
    public UserDTO addUser(UserDTO userDTO) {
        List<Integer> orgIds = govDeptService.getCurrentUserAllSubordinateDepartmentId();
        if (!orgIds.contains(userDTO.getOrgId())) {
            throw new BusinessException("无效部门ID");
        }
        userDTO.setPassword(PasswordGenerator.getManagementUserPwd())
                .setUsername(userDTO.getPhone())
                .setCreateUserId(CurrentUserUtil.getCurrentUser().getId())
                .setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode());
        ApiResult apiResult = oauthServiceClient.addUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("创建用户失败");
        }
        return userDTO;
    }
}
