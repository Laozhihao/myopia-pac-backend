package com.wupol.myopia.business.core.screening.flow.facade;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.business.common.utils.interfaces.HasCreatorNameLikeAndCreateUserIds;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 筛查相关代码
 *
 * @author Alix
 * @Date 2021-03-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningRelatedFacade {

    @Resource
    private OauthServiceClient oauthServiceClient;

    /**
     * 初始化CreateUserIds并返回查询结果是否为空
     *
     * @param query
     * @param <T>
     * @return
     */
    public <T extends HasCreatorNameLikeAndCreateUserIds> boolean initCreateUserIdsAndReturnIsEmpty(T query) {
        if (StringUtils.isBlank(query.getCreatorNameLike())) {
            return false;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setRealName(query.getCreatorNameLike()).setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode());
        List<Integer> queryCreatorIds = oauthServiceClient.getUserList(userDTO).stream().map(User::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queryCreatorIds)) {
            // 可以直接返回空
            return true;
        }
        query.setCreateUserIds(queryCreatorIds);
        return false;
    }
}
