package com.wupol.myopia.business.core.screening.organization.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.ScreeningOrganizationMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * 父账号
     */
    public static final Integer PARENT_ACCOUNT = 0;

    /**
     * 子账号
     */
    public static final Integer CHILD_ACCOUNT = 1;


    /**
     * 生成账号密码
     *
     * @param org         筛查机构
     * @param accountType 账号类型 1-主账号 2-子账号
     * @return 账号密码
     */
    public UsernameAndPasswordDTO generateAccountAndPassword(ScreeningOrganization org, Integer accountType) {
        String password = PasswordAndUsernameGenerator.getScreeningAdminPwd();
        String username = PasswordAndUsernameGenerator.getScreeningOrgAdminUserName(screeningOrganizationAdminService.count() + 1);

        UserDTO userDTO = new UserDTO();
        userDTO.setOrgId(org.getId())
                .setUsername(username)
                .setPassword(password)
                .setRealName(org.getName())
                .setCreateUserId(org.getCreateUserId())
                .setSystemCode(SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode());
        if (accountType.equals(PARENT_ACCOUNT)) {
            userDTO.setPhone(org.getPhone());
        }
        userDTO.setOrgConfigType(org.getConfigType());

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

        // 查找管理员
        ScreeningOrganizationAdmin admin = screeningOrganizationAdminService.getByOrgIdAndUserId(request.getId(), request.getUserId());
        if (null == admin) {
            throw new BusinessException("数据异常");
        }
        admin.setStatus(request.getStatus());
        screeningOrganizationAdminService.updateById(admin);

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
     * @param request 筛查机构id
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(ResetPasswordRequest request) {
        Integer orgId = request.getId();
        Integer userId = request.getUserId();
        String username = request.getUsername();
        ScreeningOrganization screeningOrg = baseMapper.selectById(orgId);
        if (null == screeningOrg) {
            throw new BusinessException("数据异常");
        }
        ScreeningOrganizationAdmin admin = screeningOrganizationAdminService.getByOrgIdAndUserId(orgId, userId);
        return resetOAuthPassword(admin.getUserId(), username);
    }

    /**
     * 重置密码
     *
     * @param userId   用户id
     * @param username 用户名字
     * @return 账号密码
     */
    private UsernameAndPasswordDTO resetOAuthPassword(Integer userId, String username) {
        String password = PasswordAndUsernameGenerator.getScreeningAdminPwd();
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

    /**
     * 通过Id获取名称
     *
     * @param id Id
     * @return 名称
     */
    public String getNameById(Integer id) {
        ScreeningOrganization org = getById(id);
        return Objects.nonNull(org) ? org.getName() : "";
    }

    /**
     * 通过configType获取筛查机构
     *
     * @param configType 配置类型
     * @return 筛查机构
     */
    public List<ScreeningOrganization> getByConfigType(Integer configType) {
        return baseMapper.getByConfigType(configType);
    }

    /**
     * 获取所有的筛查结构
     *
     * @return 筛查结构
     */
    public List<ScreeningOrganization> getAll() {
        return baseMapper.getAll();
    }

    /**
     * 筛查机构账号列表
     *
     * @param orgId 筛查机构Id
     * @return List<OrgAccountListDTO>
     */
    public List<OrgAccountListDTO> getAccountList(Integer orgId) {
        List<OrgAccountListDTO> accountList = new ArrayList<>();
        List<ScreeningOrganizationAdmin> listOrgList = screeningOrganizationAdminService.getListOrgList(orgId);
        if (CollectionUtils.isEmpty(listOrgList)) {
            return accountList;
        }
        List<Integer> userIds = listOrgList.stream().map(ScreeningOrganizationAdmin::getUserId).collect(Collectors.toList());
        List<User> userList = oauthServiceClient.getUserBatchByUserIds(userIds);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        listOrgList.forEach(orgAdmin -> {
            OrgAccountListDTO account = new OrgAccountListDTO();
            account.setUserId(orgAdmin.getUserId());
            account.setOrgId(orgAdmin.getScreeningOrgId());
            account.setUsername(userMap.get(orgAdmin.getUserId()).getUsername());
            account.setStatus(orgAdmin.getStatus());
            accountList.add(account);
        });
        return accountList;
    }
}