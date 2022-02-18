package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Author wulizhou
 * @Date 2022-02-17
 */
@Service
public class OverviewService extends BaseService<OverviewMapper, Overview> {

    /**
     * 生成账号密码
     *
     * @param overview 总览机构
     * @param name     子账号名称
     * @return UsernameAndPasswordDto 账号密码
     */
    public UsernameAndPasswordDTO generateAccountAndPassword(Overview overview, String name) {
        String password = PasswordAndUsernameGenerator.getOverviewAdminPwd();
        String username = StringUtils.isBlank(name) ? PasswordAndUsernameGenerator.getOverviewAdminUserName(overview.getId()) : name;
        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(overview.getId())
                .setUsername(username)
                .setPassword(password)
                .setRealName(overview.getName())
                .setCreateUserId(overview.getCreateUserId())
                .setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode())
                .setUserType(UserType.OVERVIEW.getType());
        userDTO.setOrgConfigType(overview.getConfigType());
//        User user = oauthServiceClient.addMultiSystemUser(userDTO);
//        hospitalAdminService.saveAdmin(hospital.getCreateUserId(), hospital.getId(), user.getId(), hospital.getGovDeptId());
        return new UsernameAndPasswordDTO(username, password);
    }


    /**
     * 检查总览机构名称是否重复
     *
     * @param overviewName 总览机构名称
     * @param id           总览机构ID
     * @return 是否重复
     */
    public boolean checkOverviewName(String overviewName, Integer id) {
        return baseMapper.getByNameNeId(overviewName, id).size() > 0;
    }

    /**
     * 检验总览机构合作信息是否合法
     * @param overview
     */
    public void checkOverviewCooperation(Overview overview)  {
        if (!overview.checkCooperation()) {
            throw new BusinessException("合作信息非法，请确认");
        }
    }

}
