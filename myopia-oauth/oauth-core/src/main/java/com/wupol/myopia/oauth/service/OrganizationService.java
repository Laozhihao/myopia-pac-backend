package com.wupol.myopia.oauth.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
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

    public Organization get(Integer orgId, Integer systemCode, Integer userType) {
        // 筛查或医院端，统一转化为筛查管理端或医院管理端查询
        if (SystemCode.SCREENING_CLIENT.getCode().equals(systemCode) && UserType.OTHER.getType().equals(userType)) {
            systemCode = SystemCode.MANAGEMENT_CLIENT.getCode();
            userType = UserType.SCREENING_ORGANIZATION_ADMIN.getType();
        } else if (SystemCode.HOSPITAL_CLIENT.getCode().equals(systemCode) && UserType.OTHER.getType().equals(userType)) {
            systemCode = SystemCode.MANAGEMENT_CLIENT.getCode();
            userType = UserType.HOSPITAL_ADMIN.getType();
        } else if (SystemCode.PARENT_CLIENT.getCode().equals(systemCode)) {
            // 家长端，返回一个id为-1，并状态为启用的机构
            return new Organization(-1, systemCode, userType, StatusConstant.ENABLE);
        }
        Organization org = new Organization();
        org.setOrgId(orgId).setSystemCode(systemCode).setUserType(userType);
        return getOne(new QueryWrapper<>(org));
    }

    public boolean getOrgStatus(Integer orgId, Integer systemCode, Integer userType) {
        Organization organization = get(orgId, systemCode, userType);
        return organization.getStatus().equals(StatusConstant.ENABLE);
    }

}
