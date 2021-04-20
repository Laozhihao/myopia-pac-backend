package com.wupol.myopia.business.core.parent.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.parent.domain.mapper.ParentMapper;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021-02-26
 */
@Service
public class ParentService extends BaseService<ParentMapper, Parent> {

    @Autowired
    private OauthService oauthService;

    /**
     * 根据openId获取家长
     *
     * @param openId 用户的唯一标识
     * @return com.wupol.myopia.business.parent.domain.model.Parent
     **/
    public Parent getParentByOpenId(String openId) throws IOException {
        if (StringUtils.isEmpty(openId)) {
            return null;
        }
        return findOne(new Parent().setOpenId(openId));
    }

    /**
     * 根据用户ID获取家长
     *
     * @param userId 用户ID
     * @return com.wupol.myopia.business.parent.domain.model.Parent
     **/
    public Parent getParentByUserId(Integer userId) throws IOException {
        Assert.notNull(userId, "用户ID不能为空");
        Parent parent = findOne(new Parent().setUserId(userId));
        if (Objects.isNull(parent)) {
            return null;
        }
        UserDTO userDTO = oauthService.getUserDetailByUserId(userId);
        return Objects.isNull(userDTO) ? parent : parent.setPhone(userDTO.getPhone());
    }

}
