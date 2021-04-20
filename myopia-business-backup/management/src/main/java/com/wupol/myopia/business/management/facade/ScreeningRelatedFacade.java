package com.wupol.myopia.business.management.facade;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.interfaces.HasCreatorNameLikeAndCreateUserIds;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    @Autowired
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
        UserDTOQuery userDTOQuery = new UserDTOQuery();
        userDTOQuery.setRealName(query.getCreatorNameLike()).setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode());
        List<Integer> queryCreatorIds = oauthServiceClient.getUserList(userDTOQuery).getData().stream().map(UserDTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(queryCreatorIds)) {
            // 可以直接返回空
            return true;
        }
        query.setCreateUserIds(queryCreatorIds);
        return false;
    }
}
