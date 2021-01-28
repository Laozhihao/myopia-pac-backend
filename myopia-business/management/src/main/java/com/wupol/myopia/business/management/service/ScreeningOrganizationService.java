package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.*;
import com.wupol.myopia.business.management.domain.mapper.ScreeningOrganizationMapper;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Service
@Log4j2
public class ScreeningOrganizationService extends BaseService<ScreeningOrganizationMapper, ScreeningOrganization> {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;

    @Value(value = "${oem.province.code}")
    private Long provinceCode;

    @Resource
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;

    @Resource
    private DistrictService districtService;

    @Resource
    private OauthService oauthService;

    @Resource
    private ScreeningTaskOrgService screeningTaskOrgService;

    @Resource
    private ScreeningTaskService screeningTaskService;

    @Resource
    private ScreeningResultService screeningResultService;

    @Resource
    private SchoolVisionStatisticService schoolVisionStatisticService;

    @Resource
    private SchoolService schoolService;

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

        if (null == townCode) {
            throw new BusinessException("数据异常");
        }
        RLock rLock = redissonClient.getLock(String.format(CacheKey.LOCK_ORG_REDIS, townCode));

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

        UserDTO user = oauthService.addAdminUser(userDTO);
        screeningOrganizationAdminService
                .insertAdmin(org.getCreateUserId(), org.getId(),
                        user.getId(), org.getGovDeptId());
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
        screeningOrganization.setStatus(CommonConst.STATUS_IS_DELETED);
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
        IPage<ScreeningOrgResponse> orgLists = baseMapper.getScreeningOrganizationListByCondition(
                pageRequest.toPage(), query.getName(), query.getType(), query.getConfigType(), districtId,
                query.getPhone(), query.getStatus());

        // 为空直接返回
        List<ScreeningOrgResponse> records = orgLists.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return orgLists;
        }
        // 获取筛查人员信息
        Map<Integer, List<ScreeningOrganizationStaff>> staffMaps = screeningOrganizationStaffService.getOrgStaffMapByIds(
                records.stream().map(ScreeningOrganization::getId).collect(Collectors.toList()));

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
            r.setDistrictName(districtService.getDistrictName(r.getDistrictDetail()));
            r.setScreeningTime(CommonConst.SCREENING_TIME);
        });
        return orgLists;
    }

    /**
     * 获取导出数据
     *
     * @return List<ScreeningOrganization>
     */
    public List<ScreeningOrganization> getBy(ScreeningOrganizationQuery query) {
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

        UserDTO userDTO = new UserDTO()
                .setId(userId)
                .setUsername(username)
                .setPassword(password);
        oauthService.modifyUser(userDTO);
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 获取筛查机构详情
     *
     * @param id 筛查机构ID
     * @return org {@link ScreeningOrgResponse}
     */
    public ScreeningOrgResponse getScreeningOrgDetails(Integer id) {
        ScreeningOrgResponse org = baseMapper.getOrgById(id);
        if (null == org) {
            throw new BusinessException("数据异常");
        }
        org.setLastCountDate(new Date());
        return org;
    }

    /**
     * 获取筛查记录列表
     *
     * @param request 分页入参
     * @param orgId   机构ID
     * @return {@link IPage}
     */
    public IPage<ScreeningTaskResponse> getRecordLists(PageRequest request, Integer orgId) {
        // 查询筛查任务关联的机构表
        List<ScreeningTaskOrg> taskOrgLists = screeningTaskOrgService.getTaskOrgListsByOrgId(orgId);

        // 为空直接返回
        if (CollectionUtils.isEmpty(taskOrgLists)) {
            return new Page<>();
        }
        // 获取筛查通知任务
        IPage<ScreeningTaskResponse> taskPages = screeningTaskService.getTaskByIds(request, taskOrgLists
                .stream()
                .map(ScreeningTaskOrg::getScreeningTaskId)
                .collect(Collectors.toList()));
        List<ScreeningTaskResponse> tasks = taskPages.getRecords();
        if (CollectionUtils.isEmpty(tasks)) {
            return taskPages;
        }
        tasks.forEach(this::extractedDTO);
        return taskPages;
    }

    /**
     * 封装DTO
     *
     * @param taskResponse 筛查端-记录详情
     */
    private void extractedDTO(ScreeningTaskResponse taskResponse) {
        ScreeningRecordItems response = new ScreeningRecordItems();
        List<RecordDetails> details = new ArrayList<>();

        List<Integer> schoolIds = screeningResultService.getSchoolIdByTaskId(taskResponse.getId());
        if (CollectionUtils.isEmpty(schoolIds)) {
            return;
        }

        // 设置学校总数
        response.setSchoolCount(schoolIds.size());

        // 查询学校统计
        List<SchoolVisionStatistic> schoolStatistics = schoolVisionStatisticService
                .getBySchoolIds(taskResponse.getId(), schoolIds);
        Map<Integer, SchoolVisionStatistic> schoolStatisticMaps = schoolStatistics
                .stream().collect(Collectors.toMap(SchoolVisionStatistic::getSchoolId, Function.identity()));

        // 学校名称
        List<School> schools = schoolService.getByIds(schoolIds);
        Map<Integer, School> schoolMaps = schools
                .stream().collect(Collectors.toMap(School::getId, Function.identity()));

        List<Integer> createUserIds = screeningResultService.getCreateUserIdByTaskId(taskResponse.getId());
        // 员工信息
        if (!CollectionUtils.isEmpty(createUserIds)) {
            List<UserDTO> userDTOS = oauthService.getUserBatchByIds(createUserIds);
            response.setStaffCount(createUserIds.size());
            response.setStaffName(userDTOS
                    .stream().map(UserDTO::getRealName).collect(Collectors.toList()));
        }

        // 设置DTO
        schoolIds.forEach(s -> {
            RecordDetails detail = new RecordDetails();
            detail.setSchoolId(s);
            if (null != schoolMaps.get(s)) {
                detail.setSchoolName(schoolMaps.get(s).getName());
            }
            if (null != schoolStatisticMaps.get(s)) {
                detail.setPlanScreeningNumbers(schoolStatisticMaps.get(s).getPlanScreeningNumbers());
                detail.setRealScreeningNumbers(schoolStatisticMaps.get(s).getRealScreeningNumners());
            }
            details.add(detail);
        });
        response.setDetails(details);
        taskResponse.setItems(response);
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<ScreeningOrganization> getByPage(Page<?> page, ScreeningOrganizationQuery query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 通过IDs批量查询
     *
     * @param orgIds id列表
     * @return List<ScreeningOrganization>
     */
    public List<ScreeningOrganization> getByIds(List<Integer> orgIds) {
        return baseMapper
                .selectList(new QueryWrapper<ScreeningOrganization>()
                        .in("id", orgIds));
    }
}