package com.wupol.myopia.business.core.screening.organization.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordAndUsernameGenerator;
import com.wupol.myopia.business.common.utils.domain.dto.ResetPasswordRequest;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.common.utils.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.core.common.constant.OrgCacheKey;
import com.wupol.myopia.business.core.screening.organization.domain.dto.CacheOverviewInfoDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OrgAccountListDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.OverviewRequestDTO;
import com.wupol.myopia.business.core.screening.organization.domain.mapper.OverviewMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewAdmin;
import com.wupol.myopia.business.core.screening.organization.domain.query.OverviewQuery;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * 总览机构信息缓存时间
     */
    private static final long OVERVIEW_CACHE_SECOND_TIME = 3600L;
    @Autowired
    private RedisUtil redisUtil;

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
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOverview(OverviewRequestDTO overview) {
        if (checkOverviewName(overview.getName(), overview.getId())) {
            throw new BusinessException("医院名字重复，请确认");
        }
        // 1.更新总览机构信息，总览机构-医院关系记录,生成总览机构-筛查机构，清空缓存
        super.updateById(overview);
        overviewHospitalService.updateBindInfo(overview.getId(), overview.getHospitalIds());
        overviewScreeningOrganizationService.updateBindInfo(overview.getId(), overview.getScreeningOrganizationIds());
        removeOverviewCache(overview.getId());

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
    }

    /**
     * 获取总览机构列表
     * @param page
     * @param query
     * @return
     */
    public IPage<OverviewDTO> getOverviewListByCondition(Page<?> page, OverviewQuery query) {
        return baseMapper.getOverviewListByCondition(page, query);
    }

    /**
     * 总览机构管理员用户账号列表
     *
     * @param overviewId 总览机构Id
     * @return List<OrgAccountListDTO>
     */
    public List<OrgAccountListDTO> getAccountList(Integer overviewId) {
        List<OrgAccountListDTO> accountList = new LinkedList<>();
        List<OverviewAdmin> overviewAdminList = overviewAdminService.findByList(new OverviewAdmin().setOverviewId(overviewId));
        if (CollectionUtils.isEmpty(overviewAdminList)) {
            return accountList;
        }
        // TODO wulizhou 抽取
        List<Integer> userIds = overviewAdminList.stream().map(OverviewAdmin::getUserId).collect(Collectors.toList());
        List<User> userList = oauthServiceClient.getUserBatchByUserIds(userIds);
        Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
        overviewAdminList.forEach(adminUser -> {
            User user = userMap.get(adminUser.getUserId());
            OrgAccountListDTO account = new OrgAccountListDTO();
            account.setUserId(adminUser.getUserId());
            account.setOrgId(overviewId);
            account.setUsername(user.getUsername());
            account.setStatus(user.getStatus());
            accountList.add(account);
        });
        return accountList;
    }

    /**
     * 更新总览机构管理员用户状态
     *
     * @param request 用户信息
     * @return boolean
     **/
    public boolean updateOverviewAdminUserStatus(StatusRequest request) {
        overviewAdminService.checkIdAndUserId(request.getId(), request.getUserId());
        // TODO wulizhou 抽出来
        UserDTO overviewAdmin = new UserDTO();
        overviewAdmin.setId(request.getUserId());
        overviewAdmin.setStatus(request.getStatus());
        oauthServiceClient.updateUser(overviewAdmin);
        return true;
    }

    /**
     * 重置密码
     *
     * @param request 请求参数
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(ResetPasswordRequest request) {
        overviewAdminService.checkIdAndUserId(request.getId(), request.getUserId());
        String password = PasswordAndUsernameGenerator.getOverviewAdminPwd();
        oauthServiceClient.resetPwd(request.getUserId(), password);
        return new UsernameAndPasswordDTO(request.getUsername(), password);
    }

    /**
     * 添加总览机构管理员账号账号
     *
     * @param overviewId 总览机构ID
     * @return UsernameAndPasswordDTO
     */
    public UsernameAndPasswordDTO addOverviewAdminUserAccount(Integer overviewId) {
        Overview overview = this.getById(overviewId);
        if (Objects.isNull(overview)) {
            throw new BusinessException("不存在该学校");
        }
        // 获取该总览机构已经有多少个账号
        List<OverviewAdmin> overviewAdminList = overviewAdminService.findByList(new OverviewAdmin().setOverviewId(overviewId));
        if (CollectionUtils.isEmpty(overviewAdminList)) {
            throw new BusinessException("数据异常，无主账号");
        }

        // 获取主账号的账号名称
        OverviewAdmin overviewAdmin = overviewAdminList.stream().sorted(Comparator.comparing(OverviewAdmin::getCreateTime)).collect(Collectors.toList()).get(0);
        String mainUsername = oauthServiceClient.getUserDetailByUserId(overviewAdmin.getUserId()).getUsername();
        String username = new StringBuilder(mainUsername).append("x").append(overviewAdminList.size()).toString();
        return generateAccountAndPassword(overview, username);
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
     * 获取指定configType类型的总览机构
     * @param configType
     * @return
     */
    public List<Overview> getByConfigType(Integer configType) {
        Overview query = new Overview();
        query.setConfigType(configType);
        return super.findByList(query);
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
     * 获取状态未更新的总览机构（已到合作开始时间未启用，已到合作结束时间未停止）
     * @return
     */
    public List<Overview> getUnhandleOverview(Date date) {
        return baseMapper.getByCooperationTimeAndStatus(date);
    }

    /**
     * CAS更新机构状态，当且仅当源状态为sourceStatus，且限定id
     * @param id
     * @param targetStatus
     * @param sourceStatus
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateOverviewStatus(Integer id, Integer targetStatus, Integer sourceStatus) {
        // 更新机构状态成功
        int result = baseMapper.updateOverviewStatus(id, targetStatus, sourceStatus);
        if (result > 0) {
            // 更新oauth上机构的状态
            oauthServiceClient.updateOrganization(new Organization(id, SystemCode.MANAGEMENT_CLIENT, UserType.OVERVIEW, targetStatus));
        }
        return result;
    }

    /**
     * 获取指定合作结束时间的医院信息
     * @param start     开始时间早于该时间才处理
     * @param end       指定结束时间，精确到天
     * @return
     */
    public List<Overview> getByCooperationEndTime(Date start, Date end) {
        return baseMapper.getByCooperationEndTime(start, end);
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

    /**
     * 清空总览机构简要信息
     * @param overviewId
     */
    public void removeOverviewCache(Integer overviewId) {
        String key = String.format(OrgCacheKey.ORG_OVERVIEW, overviewId);
        redisUtil.del(key);
    }

    /**
     * 获取总览机构简要信息
     * @param overviewId
     * @return
     */
    public CacheOverviewInfoDTO getSimpleOverviewInfo(Integer overviewId) {
        // 从缓存获取
        String key = String.format(OrgCacheKey.ORG_OVERVIEW, overviewId);
        CacheOverviewInfoDTO cacheOverviewInfoDTO = (CacheOverviewInfoDTO)redisUtil.get(key);
        if (Objects.isNull(cacheOverviewInfoDTO)) {
            // 添加缓存
            Overview overview = super.getById(overviewId);
            cacheOverviewInfoDTO = new CacheOverviewInfoDTO();
            BeanUtils.copyProperties(overview, cacheOverviewInfoDTO);
            cacheOverviewInfoDTO.setHospitalIds(overviewHospitalService.getHospitalIdByOverviewId(overviewId));
            cacheOverviewInfoDTO.setScreeningOrganizationIds(overviewScreeningOrganizationService.getScreeningOrganizationIdByOverviewId(overviewId));
            redisUtil.set(key, cacheOverviewInfoDTO, OVERVIEW_CACHE_SECOND_TIME);
        }
        return cacheOverviewInfoDTO;
    }

    /**
     * 获取指定总览机构绑定的医院Id集
     * @param overviewId
     * @return
     */
    public List<Integer> getBindHospital(Integer overviewId) {
        return getSimpleOverviewInfo(overviewId).getHospitalIds();
    }

    /**
     * 获取指定总览机构绑定的筛查机构Id集
     * @param overviewId
     * @return
     */
    public List<Integer> getBindScreeningOrganization(Integer overviewId) {
        return getSimpleOverviewInfo(overviewId).getScreeningOrganizationIds();
    }

    /**
     * 检验是否有权限进行操作
     * @param user
     * @param hospitalId
     */
    public void checkHospital(CurrentUser user, Integer hospitalId) {
        if (user.isOverviewUser()) {
            if (!getBindHospital(user.getOrgId()).contains(hospitalId)) {
                throw new BusinessException("非法请求");
            }
        }
    }

    /**
     * 检验是否有权限进行操作
     * @param user
     * @param screeningOrganizationId
     */
    public void checkScreeningOrganization(CurrentUser user, Integer screeningOrganizationId) {
        if (user.isOverviewUser()) {
            if (!getBindScreeningOrganization(user.getOrgId()).contains(screeningOrganizationId)) {
                throw new BusinessException("非法请求");
            }
        }
    }

}
