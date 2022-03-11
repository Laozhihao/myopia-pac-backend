package com.wupol.myopia.business.core.common.service;

import com.wupol.myopia.business.core.common.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2022/3/9 16:18
 */
@Service
@Slf4j
public class UserCommonService {

    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 通过用户Ids获取账号列表
     * @param orgId
     * @param userIds
     * @return
     */
    public List<OrgAccountListDTO> getAccountListByUserIds(Integer orgId, List<Integer> userIds) {
        List<OrgAccountListDTO> accountList = new LinkedList<>();
        List<User> userList = oauthServiceClient.getUserBatchByUserIds(userIds);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        // 组装账号列表信息
        userIds.forEach(userId -> {
            User user = userMap.get(userId);
            OrgAccountListDTO account = new OrgAccountListDTO();
            account.setUserId(userId);
            account.setOrgId(orgId);
            account.setUsername(user.getUsername());
            account.setStatus(user.getStatus());
            accountList.add(account);
        });
        return accountList;
    }

    /**
     * 更新用户状态
     * @param userId
     * @param status
     * @return
     */
    public Boolean updateAdminUserStatus(Integer userId, Integer status) {
        UserDTO user = new UserDTO();
        user.setId(userId);
        user.setStatus(status);
        oauthServiceClient.updateUser(user);
        return true;
    }

}
