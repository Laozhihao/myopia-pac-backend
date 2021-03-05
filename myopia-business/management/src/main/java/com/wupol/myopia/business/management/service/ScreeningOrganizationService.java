package com.wupol.myopia.business.management.service;

import cn.hutool.core.lang.Assert;
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
import com.wupol.myopia.business.management.domain.vo.ScreeningPlanSchoolVo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;

    @Resource
    private DistrictService districtService;

    @Resource
    private OauthService oauthService;

    @Resource
    private ScreeningTaskOrgService screeningTaskOrgService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private SchoolVisionStatisticService schoolVisionStatisticService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;


    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return UsernameAndPasswordDTO 账号密码
     */
    @Transactional(rollbackFor = Exception.class)
    public UsernameAndPasswordDTO saveScreeningOrganization(ScreeningOrganization screeningOrganization) {

        Integer createUserId = screeningOrganization.getCreateUserId();
        String name = screeningOrganization.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("名字不能为空");
        }

        if (checkScreeningOrgName(name, null)) {
            throw new BusinessException("筛查机构名称不能重复");
        }
        RLock rLock = redissonClient.getLock(String.format(CacheKey.LOCK_ORG_REDIS, name));
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
        log.warn("用户id:{}新增机构获取不到锁，机构名称:{}", createUserId, name);
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
                .setPhone(org.getPhone())
                .setRealName(username)
                .setCreateUserId(org.getCreateUserId())
                .setSystemCode(SystemCode.SCREENING_MANAGEMENT_CLIENT.getCode());

        UserDTO user = oauthService.addMultiSystemUser(userDTO);
        screeningOrganizationAdminService
                .insertAdmin(org.getCreateUserId(), org.getId(),
                        user.getId(), org.getGovDeptId());
        return new UsernameAndPasswordDTO(username, password);
    }

    /**
     * 更新筛查机构
     *
     * @param currentUser           当前登录用户
     * @param screeningOrganization 筛查机构实体咧
     * @return 筛查机构
     */
    @Transactional(rollbackFor = Exception.class)
    public ScreeningOrgResponseDTO updateScreeningOrganization(CurrentUser currentUser, ScreeningOrganization screeningOrganization) {

        if (checkScreeningOrgName(screeningOrganization.getName(), screeningOrganization.getId())) {
            throw new BusinessException("筛查机构名称不能重复");
        }
        ScreeningOrgResponseDTO response = new ScreeningOrgResponseDTO();
        ScreeningOrganization checkOrg = baseMapper.selectById(screeningOrganization.getId());

        // 机构管理员
        ScreeningOrganizationAdmin admin = screeningOrganizationAdminService.getByOrgId(screeningOrganization.getId());
        // 更新OAuth账号
        UserDTO userDTO = new UserDTO()
                .setId(admin.getUserId())
                .setPhone(screeningOrganization.getPhone())
                .setRealName(screeningOrganization.getName())
                .setUsername(screeningOrganization.getName());
        oauthService.modifyUser(userDTO);

        // 名字更新重置密码
        if (!StringUtils.equals(checkOrg.getName(), screeningOrganization.getName())) {
            response.setUpdatePassword(Boolean.TRUE);
            response.setUsername(screeningOrganization.getName());
            // 重置密码
            String password = PasswordGenerator.getScreeningAdminPwd();
            oauthService.resetPwd(admin.getUserId(), password);
            response.setPassword(password);
        }

        baseMapper.updateById(screeningOrganization);
        ScreeningOrganization o = baseMapper.selectById(screeningOrganization.getId());
        BeanUtils.copyProperties(o, response);
        response.setDistrictName(districtService.getDistrictName(o.getDistrictDetail()));
        // 详细地址
        response.setAddressDetail(districtService.getAddressDetails(
                o.getProvinceCode(), o.getCityCode(), o.getAreaCode(), o.getTownCode(), o.getAddress()));
        response.setScreeningTime(screeningOrganization.getScreeningTime())
                .setStaffCount(screeningOrganization.getStaffCount());
        // 是否能更新
        if (currentUser.isPlatformAdminUser()) {
            response.setCanUpdate(true);
        } else if (response.getCreateUserId().equals(currentUser.getId())) {
            response.setCanUpdate(true);
        }
        return response;
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
    public IPage<ScreeningOrgResponseDTO> getScreeningOrganizationList(PageRequest pageRequest,
                                                                       ScreeningOrganizationQuery query,
                                                                       CurrentUser currentUser) {
        Integer districtId = districtService.filterQueryDistrictId(currentUser, query.getDistrictId());

        // 查询
        IPage<ScreeningOrgResponseDTO> orgLists = baseMapper.getScreeningOrganizationListByCondition(
                pageRequest.toPage(), query.getName(), query.getType(), query.getConfigType(), districtId,
                query.getGovDeptId(), query.getPhone(), query.getStatus());

        // 为空直接返回
        List<ScreeningOrgResponseDTO> records = orgLists.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return orgLists;
        }

        // 获取筛查人员信息
        Map<Integer, List<ScreeningOrganizationStaff>> staffMaps = screeningOrganizationStaffService
                .getOrgStaffMapByIds(records.stream().map(ScreeningOrganization::getId)
                        .collect(Collectors.toList()));
        // 获取已有任务的机构ID列表
        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);
        // 封装DTO
        records.forEach(r -> {
            // 同一部门才能更新
            if (currentUser.isPlatformAdminUser()) {
                r.setCanUpdate(true);
            } else if (r.getCreateUserId().equals(currentUser.getId())) {
                r.setCanUpdate(true);
            }

            // 筛查人员
            List<ScreeningOrganizationStaff> staffLists = staffMaps.get(r.getId());
            if (!CollectionUtils.isEmpty(staffLists)) {
                r.setStaffCount(staffLists.size());
            } else {
                r.setStaffCount(0);
            }
            // 区域名字
            r.setDistrictName(districtService.getDistrictName(r.getDistrictDetail()));

            // 筛查次数
            r.setScreeningTime(screeningPlanService.getByOrgId(r.getId()).size());
            r.setAlreadyHaveTask(haveTaskOrgIds.contains(r.getId()));

            // 详细地址
            r.setAddressDetail(districtService.getAddressDetails(
                    r.getProvinceCode(), r.getCityCode(), r.getAreaCode(), r.getTownCode(), r.getAddress()));
        });
        return orgLists;
    }

    /**
     * 根据部门ID获取筛查机构列表（带是否已有任务）
     *
     * @param query 筛查机构列表请求体
     * @return List<ScreeningOrgResponse>
     */
    public List<ScreeningOrgResponseDTO> getScreeningOrganizationListByGovDeptId(ScreeningOrganizationQuery query) {
        Assert.notNull(query.getGovDeptId(), "部门id不能为空");
        query.setStatus(CommonConst.STATUS_NOT_DELETED);
        // 查询
        List<ScreeningOrganization> screeningOrganizationList = baseMapper.getBy(query);
        // 为空直接返回
        if (CollectionUtils.isEmpty(screeningOrganizationList)) {
            return Collections.emptyList();
        }
        // 获取已有任务的机构ID列表
        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);
        // 封装DTO
        return screeningOrganizationList.stream().map(r -> {
            ScreeningOrgResponseDTO orgResponseDTO = new ScreeningOrgResponseDTO();
            BeanUtils.copyProperties(r, orgResponseDTO);
            orgResponseDTO.setAlreadyHaveTask(haveTaskOrgIds.contains(r.getId()));
            return orgResponseDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 根据是否需要查询机构是否已有任务，返回时间段内已有任务的机构id
     *
     * @param query
     * @return
     */
    private List<Integer> getHaveTaskOrgIds(ScreeningOrganizationQuery query) {
        if (Objects.nonNull(query.getNeedCheckHaveTask()) && query.getNeedCheckHaveTask()) {
            return screeningTaskOrgService.getHaveTaskOrgIds(query.getGovDeptId(), query.getStartTime(), query.getEndTime());
        }
        return Collections.emptyList();
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
        baseMapper.updateById(org);

        // 查找管理员
        ScreeningOrganizationAdmin admin = screeningOrganizationAdminService.getByOrgId(request.getId());
        if (null == admin) {
            throw new BusinessException("数据异常");
        }

        // 更新OAuth2
        UserDTO userDTO = new UserDTO()
                .setId(admin.getUserId())
                .setStatus(request.getStatus());
        oauthService.modifyUser(userDTO);
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
        oauthService.resetPwd(userId, password);
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
     * 获取筛查记录列表
     *
     * @param request 分页入参
     * @param orgId   机构ID
     * @return {@link IPage}
     */
    public IPage<ScreeningOrgPlanResponse> getRecordLists(PageRequest request, Integer orgId) {

        // 获取筛查计划
        IPage<ScreeningOrgPlanResponse> taskPages = screeningPlanService.getPageByOrgId(request, orgId);
        List<ScreeningOrgPlanResponse> tasks = taskPages.getRecords();
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
    private void extractedDTO(ScreeningOrgPlanResponse taskResponse) {
        ScreeningRecordItems response = new ScreeningRecordItems();
        List<RecordDetails> details = new ArrayList<>();

        List<ScreeningPlanSchoolVo> schoolVos = screeningPlanSchoolService.getSchoolVoListsByPlanId(taskResponse.getId());

        // 设置筛查状态
        taskResponse.setScreeningStatus(getScreeningStatus(taskResponse.getStartTime(), taskResponse.getEndTime()));

        // 获取学校ID
        List<Integer> schoolIds = schoolVos.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schoolIds)) {
            return;
        }
        // 学生统计
        Map<Integer, Integer> planStudentMaps = schoolVos.stream()
                .collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, ScreeningPlanSchoolVo::getStudentCount));

        // 设置学校总数
        response.setSchoolCount(schoolIds.size());

        // 查询学校统计
        List<SchoolVisionStatistic> schoolStatistics = schoolVisionStatisticService
                .getBySchoolIds(taskResponse.getId(), schoolIds);
        Map<Integer, SchoolVisionStatistic> schoolStatisticMaps = schoolStatistics
                .stream().collect(Collectors.toMap(SchoolVisionStatistic::getSchoolId, Function.identity()));

        // 学校名称
        List<School> schools = schoolService.getByIds(schoolIds);
        Map<Integer, School> schoolMaps = schools.stream()
                .collect(Collectors.toMap(School::getId, Function.identity()));

        List<Integer> createUserIds = visionScreeningResultService.getCreateUserIdByTaskId(taskResponse.getId());
        // 员工信息
        if (!CollectionUtils.isEmpty(createUserIds)) {
            List<UserDTO> userDTOS = oauthService.getUserBatchByIds(createUserIds);
            response.setStaffCount(createUserIds.size());
            response.setStaffName(userDTOS
                    .stream().map(UserDTO::getRealName).collect(Collectors.toList()));
        } else {
            response.setStaffCount(0);
        }

        // 封装DTO
        schoolIds.forEach(s -> {
            RecordDetails detail = new RecordDetails();
            detail.setSchoolId(s);
            if (null != schoolMaps.get(s)) {
                detail.setSchoolName(schoolMaps.get(s).getName());
            }
            if (null != schoolStatisticMaps.get(s)) {
                detail.setRealScreeningNumbers(schoolStatisticMaps.get(s).getRealScreeningNumners());
            } else {
                detail.setRealScreeningNumbers(0);
            }
            detail.setPlanScreeningNumbers(planStudentMaps.get(s));
            detail.setScreeningPlanId(taskResponse.getId());
            detail.setStartTime(taskResponse.getStartTime());
            detail.setEndTime(taskResponse.getEndTime());
            detail.setPlanTitle(taskResponse.getTitle());
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
        return baseMapper.selectBatchIds(orgIds);
    }

    /**
     * 根据名称模糊查询
     *
     * @param screeningOrgNameLike 机构名称
     * @return List<ScreeningOrganization>
     */
    public List<ScreeningOrganization> getByNameLike(String screeningOrgNameLike) {
        return baseMapper.getByName(screeningOrgNameLike);
    }

    /**
     * 检查筛查机构名称是否重复
     *
     * @param name 筛查机构名称
     * @param id   筛查机构ID
     * @return 是否重复
     */
    public Boolean checkScreeningOrgName(String name, Integer id) {
        return baseMapper.getByNameAndNeId(name, id).size() > 0;
    }

    /**
     * 获取筛查状态
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 筛查状态 0-未开始 1-进行中 2-已结束
     */
    private Integer getScreeningStatus(Date startDate, Date endDate) {

        Date nowDate = new Date();
        if (nowDate.before(startDate)) {
            return 0;
        }
        if (nowDate.after(startDate) && nowDate.before(endDate)) {
            return 1;
        }
        if (nowDate.after(endDate)) {
            return 2;
        }
        return 1;
    }
    public Object getRecordDetail(Integer id) {
        return visionScreeningResultService.getByTaskId(id);
    }
}