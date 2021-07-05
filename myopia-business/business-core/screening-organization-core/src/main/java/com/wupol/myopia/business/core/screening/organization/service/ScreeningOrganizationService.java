package com.wupol.myopia.business.core.screening.organization.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.ScreeningOrganizationMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 筛查机构
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ScreeningOrganizationService extends BaseService<ScreeningOrganizationMapper, ScreeningOrganization> {

    @Resource
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private DistrictService districtService;


    /**
     * 生成账号密码
     *
     * @param org 筛查机构
     * @return 账号密码
     */
    public UsernameAndPasswordDTO generateAccountAndPassword(ScreeningOrganization org) {
        String password = PasswordGenerator.getScreeningAdminPwd();
        String username = org.getName();

        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(org.getId())
                .setUsername(username)
                .setPassword(password)
                .setPhone(org.getPhone())
                .setRealName(username)
                .setCreateUserId(org.getCreateUserId())
                .setSystemCode(SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode());

        User user = oauthServiceClient.addMultiSystemUser(userDTO);
        screeningOrganizationAdminService
                .insertAdmin(org.getCreateUserId(), org.getId(),
                        user.getId(), org.getGovDeptId());
        return new UsernameAndPasswordDTO(username, password);
    }


    /**
     * 删除筛查机构
     *
     * @param id 筛查机构ID
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedById(Integer id) {
        ScreeningOrganization screeningOrganization = new ScreeningOrganization();
        screeningOrganization.setId(id);
        screeningOrganization.setStatus(CommonConst.STATUS_IS_DELETED);
        return baseMapper.updateById(screeningOrganization);
    }


    /**
     * 获取导出数据
     *
     * @return List<ScreeningOrganization>
     */
    public List<ScreeningOrganization> getBy(ScreeningOrganizationQueryDTO query) {
        return baseMapper.getBy(query);
    }

    /**
     * 更新机构状态
     *
     * @param request 入参
     * @return 更新个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateStatus(StatusRequest request) {
        ScreeningOrganization org = new ScreeningOrganization();
        org.setId(request.getId());
        org.setStatus(request.getStatus());
        baseMapper.updateById(org);

        // 查找管理员
        ScreeningOrganizationAdmin admin = screeningOrganizationAdminService.getByOrgId(request.getId());
        if (null == admin) {
            throw new BusinessException("数据异常");
        }

        // 更新OAuth2
        UserDTO userDTO = new UserDTO();
        userDTO.setId(admin.getUserId())
                .setStatus(request.getStatus());
        oauthServiceClient.updateUser(userDTO);
        return 1;
    }

    /**
     * 重置密码
     *
     * @param id 筛查机构id
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(Integer id) {
        ScreeningOrganization screeningOrg = baseMapper.selectById(id);
        if (null == screeningOrg) {
            throw new BusinessException("数据异常");
        }
        ScreeningOrganizationAdmin admin = screeningOrganizationAdminService.getByOrgId(id);
        return resetOAuthPassword(screeningOrg, admin.getUserId());
    }

    /**
     * 重置密码
     *
     * @param screeningOrg 筛查机构
     * @param userId       用户id
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetOAuthPassword(ScreeningOrganization screeningOrg, Integer userId) {
        String password = PasswordGenerator.getScreeningAdminPwd();
        String username = screeningOrg.getName();
        oauthServiceClient.resetPwd(userId, password);
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 获取筛查机构详情
     *
     * @param id 筛查机构ID
     * @return org {@link ScreeningOrgResponseDTO}
     */
    public ScreeningOrgResponseDTO getScreeningOrgDetails(Integer id) {
        ScreeningOrgResponseDTO org = baseMapper.getOrgById(id);
        if (null == org) {
            throw new BusinessException("数据异常");
        }
        org.setLastCountDate(new Date());
        return org;
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<ScreeningOrganization> getByPage(Page<?> page, ScreeningOrganizationQueryDTO query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 通过IDs批量查询
     *
     * @param orgIds id列表
     * @return List<ScreeningOrganization>
     */
    public List<ScreeningOrganization> getByIds(List<Integer> orgIds) {
        return baseMapper.selectBatchIds(orgIds);
    }

    /**
     * 根据名称模糊查询
     *
     * @param screeningOrgNameLike 机构名称
     * @return List<ScreeningOrganization>
     */
    public List<ScreeningOrganization> getByNameLike(String screeningOrgNameLike) {
        List<ScreeningOrganization> orgList = baseMapper.getByName(screeningOrgNameLike);
        orgList.forEach(org -> org.setDistrictDetailName(districtService.getDistrictName(org.getDistrictDetail())));
        return orgList;
    }

    /**
     * 检查筛查机构名称是否重复
     *
     * @param name 筛查机构名称
     * @param id   筛查机构ID
     * @return 是否重复
     */
    public Boolean checkScreeningOrgName(String name, Integer id) {
        return !baseMapper.getByNameAndNeId(name, id).isEmpty();
    }

    /**
     * 获取筛查机构列表
     *
     * @param pageRequest 分页请求
     * @param query       查询条件
     * @param districtId  行政区域
     * @return 筛查机构列表
     */
    public IPage<ScreeningOrgResponseDTO> getByCondition(PageRequest pageRequest, ScreeningOrganizationQueryDTO query, Integer districtId) {
        return baseMapper.getScreeningOrganizationListByCondition(
                pageRequest.toPage(), query.getName(), query.getType(), query.getConfigType(), districtId,
                query.getGovDeptId(), query.getPhone(), query.getStatus());
    }

    public String getNameById(Integer id) {
        ScreeningOrganization org = getById(id);
        return Objects.nonNull(org) ? org.getName() : "";
    }


}