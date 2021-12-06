package com.wupol.myopia.oauth.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.oauth.domain.mapper.OrganizationMapper;
import com.wupol.myopia.oauth.domain.model.Organization;
import org.springframework.stereotype.Service;

/**
 * @Author wulizhou
 * @Date 2021-12-06
 */
@Service
public class OrganizationService extends BaseService<OrganizationMapper, Organization> {

    public Organization get(Integer orgId, Integer systemCode) {
        Organization org = new Organization();
        org.setOrgId(orgId).setSystemCode(systemCode);
        return getOne(new QueryWrapper<>(org));
    }

    public boolean getOrgStatus(Integer orgId, Integer systemCode) {
        Organization organization = get(orgId, systemCode);
        return organization.getStatus().equals(StatusConstant.ENABLE);
    }

}
