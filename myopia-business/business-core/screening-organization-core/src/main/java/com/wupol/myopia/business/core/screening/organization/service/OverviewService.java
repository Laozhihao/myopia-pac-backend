package com.wupol.myopia.business.core.screening.organization.service;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewRequestDTO;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2022-02-17
 */
@Service
public class OverviewService extends BaseService<OverviewMapper, Overview> {

    @Autowired
    private OauthServiceClient oauthServiceClient;

    @Autowired
    private OverviewAdminService overviewAdminService;

    @Autowired
    private OverviewHospitalService overviewHospitalService;

    @Autowired
    private OverviewScreeningOrganizationService overviewScreeningOrganizationService;

    /**
     * 保存总览机构
     *
     * @param overview 总览机构实体类
     * @return UsernameAndPasswordDto 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized UsernameAndPasswordDTO saveOverview(OverviewRequestDTO overview) {
        if (checkOverviewName(overview.getName(), null)) {
            throw new BusinessException("总览机构名字重复，请确认");
        }
        // 保存总览机构信息，总览机构-医院关系记录,生成总览机构-筛查机构
        super.save(overview);
        overviewHospitalService.batchSave(overview.getId(), overview.getHospitalIds());
        overviewScreeningOrganizationService.batchSave(overview.getId(), overview.getScreeningOrganizationIds());

        // oauth系统中增加总览机构状态信息
        oauthServiceClient.addOrganization(new Organization(overview.getId(), SystemCode.MANAGEMENT_CLIENT,
                UserType.OVERVIEW, overview.getStatus()));
        return generateAccountAndPassword(overview, StringUtils.EMPTY);
    }

    /**
     * 更新总览机构信息
     *
     * @param overview 总览机构实体类
     * @return 总览机构实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public OverviewDTO updateOverview(OverviewRequestDTO overview) {
        if (checkOverviewName(overview.getName(), overview.getId())) {
            throw new BusinessException("医院名字重复，请确认");
        }
        // 1.更新总览机构信息，总览机构-医院关系记录,生成总览机构-筛查机构
        super.updateById(overview);
        overviewHospitalService.updateBindInfo(overview.getId(), overview.getHospitalIds());
        overviewScreeningOrganizationService.updateBindInfo(overview.getId(), overview.getScreeningOrganizationIds());

        // 2.更新总览机构的账号权限及名称
        Overview oldOverview = super.getById(overview.getId());
        if (StringUtils.isNotBlank(overview.getName()) && (!overview.getName().equals(oldOverview.getName()))) {
            oauthServiceClient.updateUserRealName(overview.getName(), overview.getId(), SystemCode.MANAGEMENT_CLIENT.getCode(),
                    UserType.OVERVIEW.getType());
        }
        if (Objects.nonNull(overview.getConfigType()) && (!overview.getConfigType().equals(oldOverview.getConfigType()))) {
            oauthServiceClient.updateOverviewIdRole(overview.getId(), overview.getConfigType());
        }
        if (Objects.nonNull(overview.getStatus()) && (!overview.getStatus().equals(oldOverview.getStatus()))) {
            // 同步到oauth机构状态
            oauthServiceClient.updateOrganization(new Organization(overview.getId(), SystemCode.MANAGEMENT_CLIENT,
                    UserType.OVERVIEW, overview.getStatus()));
        }
        // TODO wulizhou 返回当前数据列表所需数据
        return null;
    }

    /**
     * 更新医院管理员用户状态
     *
     * @param request 用户信息
     * @return boolean
     **/
    public boolean updateOverviewAdminUserStatus(StatusRequest request) {
        HospitalAdmin hospitalAdmin = hospitalAdminService.findOne(new HospitalAdmin().setHospitalId(request.getId()).setUserId(request.getUserId()));
        Assert.notNull(hospitalAdmin, "不存在该用户");
        UserDTO user = new UserDTO();
        user.setId(request.getUserId());
        user.setStatus(request.getStatus());
        oauthServiceClient.updateUser(user);
        return true;
    }

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
        // 保存管理员信息
        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        overviewAdminService.saveAdmin(overview.getCreateUserId(), overview.getId(), user.getId(), overview.getGovDeptId());
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
