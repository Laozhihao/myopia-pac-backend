package com.wupol.myopia.business.parent.service;

import com.wupol.myopia.business.parent.domain.model.Parent;
import com.wupol.myopia.business.parent.domain.mapper.ParentMapper;
import com.wupol.myopia.base.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @Author HaoHao
 * @Date 2021-02-26
 */
@Service
public class ParentService extends BaseService<ParentMapper, Parent> {

    /**
     * 根据openId获取患者
     *
     * @param openId 用户的唯一标识
     * @return com.wupol.myopia.business.parent.domain.model.Parent
     **/
    public Parent getPatientByOpenId(String openId) throws IOException {
        if (StringUtils.isEmpty(openId)) {
            return null;
        }
        return findOne(new Parent().setOpenId(openId));
    }

}
