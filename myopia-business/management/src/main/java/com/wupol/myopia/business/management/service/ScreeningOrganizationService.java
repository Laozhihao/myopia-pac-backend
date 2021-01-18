package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.Const;
import com.wupol.myopia.business.management.domain.dto.ScreeningOrgResponse;
import com.wupol.myopia.business.management.domain.dto.StatusRequest;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.dto.UsernameAndPasswordDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class ScreeningOrganizationService extends BaseService<ScreeningOrganizationMapper, ScreeningOrganization> {

    @Resource
    private ScreeningOrganizationMapper screeningOrganizationMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;

    @Value(value = "${oem.province.code}")
    private Long provinceCode;

    @Qualifier("com.wupol.myopia.business.management.client.OauthServiceClient")
    @Autowired
    private OauthServiceClient oauthServiceClient;

    @Resource
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;

    @Resource
    private DistrictService districtService;

    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return UsernameAndPasswordDTO 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveScreeningOrganization(ScreeningOrganization screeningOrganization) {

        Long townCode = screeningOrganization.getTownCode();
        Integer createUserId = screeningOrganization.getCreateUserId();

        // 初始化省代码
        screeningOrganization.setProvinceCode(provinceCode);
        // 设置行政区域名
        screeningOrganization.setDistrictName(districtService.getDistrictNameById(screeningOrganization.getDistrictId()));

        if (null == townCode) {
            throw new BusinessException("数据异常");
        }
        RLock rLock = redissonClient.getLock(Const.LOCK_ORG_REDIS + townCode);

        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                baseMapper.insert(screeningOrganization);
                return generateAccountAndPassword(screeningOrganization);
            }
        } catch (InterruptedException e) {
            log.error("用户id:{}获取锁异常:{}", createUserId, e);
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (rLock.isLocked()) {
                rLock.unlock();
            }
        }
        log.warn("用户id:{}新增机构获取不到锁，区域代码:{}", createUserId, townCode);
        throw new BusinessException("请重试");
    }

    /**
     * 生成账号密码
     *
     * @param org 筛查机构
     * @return 账号密码
     */
    private UsernameAndPasswordDTO generateAccountAndPassword(ScreeningOrganization org) {
        String password = PasswordGenerator.getScreeningAdminPwd();
        String username = org.getName();

        UserDTO userDTO = new UserDTO()
                .setOrgId(org.getId())
                .setUsername(username)
                .setPassword(password)
                .setCreateUserId(org.getCreateUserId())
                .setSystemCode(SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode());

        ApiResult<UserDTO> apiResult = oauthServiceClient.addAdminUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("创建管理员信息异常");
        }
        screeningOrganizationAdminService
                .insertAdmin(org.getCreateUserId(), org.getId(),
                        apiResult.getData().getId(), org.getGovDeptId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 更新筛查机构
     *
     * @param screeningOrganization 筛查机构实体咧
     * @return 筛查机构
     */
    @Transactional(rollbackFor = Exception.class)
    public ScreeningOrganization updateScreeningOrganization(ScreeningOrganization screeningOrganization) {
        // 设置行政区域名
        screeningOrganization.setDistrictName(districtService.getDistrictNameById(screeningOrganization.getDistrictId()));
        baseMapper.updateById(screeningOrganization);
        return baseMapper.selectById(screeningOrganization.getId());
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
        screeningOrganization.setStatus(Const.STATUS_IS_DELETED);
        return baseMapper.updateById(screeningOrganization);
    }

    /**
     * 获取筛查机构列表
     *
     * @param pageRequest 分页
     * @param query       筛查机构列表请求体
     * @param currentUser 当前登录用户
     * @return IPage<ScreeningOrgResponse> {@link IPage}
     */
    public IPage<ScreeningOrgResponse> getScreeningOrganizationList(PageRequest pageRequest,
                                                                    ScreeningOrganizationQuery query,
                                                                    CurrentUser currentUser) {
        Integer orgId = currentUser.getOrgId();
        Integer districtId = districtService.getDistrictId(currentUser, query.getDistrictId());

        // 查询
        IPage<ScreeningOrgResponse> orgLists = screeningOrganizationMapper.getScreeningOrganizationListByCondition(
                pageRequest.toPage(), query.getName(), query.getType(), query.getConfigType(), districtId,
                query.getPhone(), query.getStatus());

        // 为空直接返回
        List<ScreeningOrgResponse> records = orgLists.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return orgLists;
        }
        // 获取筛查人员信息
        List<ScreeningOrganizationStaff> staffs = screeningOrganizationStaffService.getStaffListsByOrgIds(records
                .stream()
                .map(ScreeningOrganization::getId)
                .collect(Collectors.toList()));
        Map<Integer, List<ScreeningOrganizationStaff>> staffMaps = staffs
                .stream()
                .collect(Collectors.groupingBy(ScreeningOrganizationStaff::getId));
        // 封装DTO
        records.forEach(r -> {
            // 同一部门才能更新
            if (r.getGovDeptId().equals(orgId)) {
                r.setCanUpdate(true);
            }
            List<ScreeningOrganizationStaff> staffLists = staffMaps.get(r.getId());
            if (!CollectionUtils.isEmpty(staffLists)) {
                r.setStaffCount(staffLists.size());
            } else {
                r.setStaffCount(0);
            }
            r.setScreeningTime(Const.SCREENING_TIME);
        });
        return orgLists;
    }

    /**
     * 获取导出数据
     *
     * @return List<ScreeningOrganization>
     */
    public List<ScreeningOrganization> getExportData(ScreeningOrganizationQuery query) {
        return screeningOrganizationMapper.getExportData(query);
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
        return baseMapper.updateById(org);
    }

    /**
     * 重置密码
     *
     * @param id 筛查机构id
     * @return 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO resetPassword(Integer id) {
        ScreeningOrganization screeningOrg = screeningOrganizationMapper.selectById(id);
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

        UserDTO userDTO = new UserDTO()
                .setId(userId)
                .setUsername(username)
                .setPassword(password);
        ApiResult<UserDTO> apiResult = oauthServiceClient.modifyUser(userDTO);
        if (!apiResult.isSuccess()) {
            throw new BusinessException("远程调用异常");
        }
        return new UsernameAndPasswordDTO(username, password);
    }
}