package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.PasswordGenerator;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.RecordDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningOrgPlanResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningRecordItems;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskOrgService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrganizationQueryDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationAdmin;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationAdminService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.request.UserDTO;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
public class ScreeningOrganizationBizService {

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Resource
    private ScreeningOrganizationAdminService screeningOrganizationAdminService;
    @Resource
    private DistrictService districtService;
    @Resource
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Resource
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private DistrictBizService districtBizService;

    /**
     * 获取筛查记录列表
     *
     * @param request 分页入参
     * @param orgId   机构ID
     * @return {@link IPage}
     */
    public IPage<ScreeningOrgPlanResponseDTO> getRecordLists(PageRequest request, Integer orgId) {

        // 获取筛查计划
        IPage<ScreeningOrgPlanResponseDTO> planPages = screeningPlanService.getPageByOrgId(request, orgId);
        List<ScreeningOrgPlanResponseDTO> tasks = planPages.getRecords();
        if (CollectionUtils.isEmpty(tasks)) {
            return planPages;
        }
        tasks.forEach(taskResponse -> extractedDTO(taskResponse, orgId));
        return planPages;
    }

    /**
     * 封装DTO
     *
     * @param planResponse 筛查端-记录详情
     * @param orgId        机构ID
     */
    private void extractedDTO(ScreeningOrgPlanResponseDTO planResponse, Integer orgId) {
        ScreeningRecordItems response = new ScreeningRecordItems();
        List<RecordDetails> details = new ArrayList<>();

        Integer planId = planResponse.getId();
        List<ScreeningPlanSchoolDTO> schoolVos = screeningPlanSchoolService.getSchoolVoListsByPlanId(planId);

        // 设置筛查状态
        planResponse.setScreeningStatus(getScreeningStatus(planResponse.getStartTime(), planResponse.getEndTime()));

        // 获取学校ID
        List<Integer> schoolIds = schoolVos.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(schoolIds)) {
            return;
        }
        // 学生统计
        Map<Integer, Integer> planStudentMaps = schoolVos.stream()
                .collect(Collectors.toMap(ScreeningPlanSchool::getSchoolId, ScreeningPlanSchoolDTO::getStudentCount));

        // 设置学校总数
        response.setSchoolCount(schoolIds.size());

        // 学校名称
        List<School> schools = schoolService.getByIds(schoolIds);
        Map<Integer, School> schoolMaps = schools.stream()
                .collect(Collectors.toMap(School::getId, Function.identity()));

        List<Integer> createUserIds = visionScreeningResultService.getCreateUserIdByPlanId(planId, orgId);
        // 员工信息
        if (!CollectionUtils.isEmpty(createUserIds)) {
            List<User> userLists = oauthServiceClient.getUserBatchByIds(createUserIds);
            response.setStaffCount(createUserIds.size());
            response.setStaffName(userLists.stream().map(User::getRealName).collect(Collectors.toList()));
        } else {
            response.setStaffCount(0);
        }

        // 封装DTO
        schoolIds.forEach(schoolId -> {
            RecordDetails detail = new RecordDetails();
            detail.setSchoolId(schoolId);
            if (null != schoolMaps.get(schoolId)) {
                detail.setSchoolName(schoolMaps.get(schoolId).getName());
            }
            detail.setRealScreeningNumbers(visionScreeningResultService.getBySchoolIdAndOrgIdAndPlanId(schoolId, orgId, planId).size());
            detail.setPlanScreeningNumbers(planStudentMaps.get(schoolId));
            detail.setScreeningPlanId(planId);
            detail.setStartTime(planResponse.getStartTime());
            detail.setEndTime(planResponse.getEndTime());
            detail.setPlanTitle(planResponse.getTitle());
            details.add(detail);
        });
        response.setDetails(details);
        planResponse.setItems(response);
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

        // 结束时间加一天
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(endDate);
        calendar.add(Calendar.DATE, 1);
        endDate = calendar.getTime();
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

    /**
     * 更新筛查机构
     *
     * @param currentUser           当前登录用户
     * @param screeningOrganization 筛查机构实体咧
     * @return 筛查机构
     */
    @Transactional(rollbackFor = Exception.class)
    public ScreeningOrgResponseDTO updateScreeningOrganization(CurrentUser currentUser, ScreeningOrganization screeningOrganization) {

        if (screeningOrganizationService.checkScreeningOrgName(screeningOrganization.getName(), screeningOrganization.getId())) {
            throw new BusinessException("筛查机构名称不能重复");
        }
        ScreeningOrgResponseDTO response = new ScreeningOrgResponseDTO();
        ScreeningOrganization checkOrg = screeningOrganizationService.getById(screeningOrganization.getId());

        // 机构管理员
        ScreeningOrganizationAdmin admin = screeningOrganizationAdminService.getByOrgId(screeningOrganization.getId());
        // 更新OAuth账号
        UserDTO userDTO = new UserDTO();
        userDTO.setId(admin.getUserId())
                .setPhone(screeningOrganization.getPhone())
                .setRealName(screeningOrganization.getName())
                .setUsername(screeningOrganization.getName());
        oauthServiceClient.modifyUser(userDTO);

        // 名字更新重置密码
        if (!StringUtils.equals(checkOrg.getName(), screeningOrganization.getName())) {
            response.setUpdatePassword(Boolean.TRUE);
            response.setUsername(screeningOrganization.getName());
            // 重置密码
            String password = PasswordGenerator.getScreeningAdminPwd();
            oauthServiceClient.resetPwd(admin.getUserId(), password);
            response.setPassword(password);
        }

        screeningOrganizationService.updateById(screeningOrganization);
        ScreeningOrganization organization = screeningOrganizationService.getById(screeningOrganization.getId());
        BeanUtils.copyProperties(organization, response);
        response.setDistrictName(districtService.getDistrictName(organization.getDistrictDetail()));
        // 详细地址
        response.setAddressDetail(districtService.getAddressDetails(
                organization.getProvinceCode(), organization.getCityCode(), organization.getAreaCode(), organization.getTownCode(), organization.getAddress()));
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
     * 获取筛查机构列表
     *
     * @param pageRequest 分页
     * @param query       筛查机构列表请求体
     * @param currentUser 当前登录用户
     * @return IPage<ScreeningOrgResponse> {@link IPage}
     */
    public IPage<ScreeningOrgResponseDTO> getScreeningOrganizationList(PageRequest pageRequest,
                                                                       ScreeningOrganizationQueryDTO query,
                                                                       CurrentUser currentUser) {
        Integer districtId = districtBizService.filterQueryDistrictId(currentUser, query.getDistrictId());

        // 查询
        IPage<ScreeningOrgResponseDTO> orgLists = screeningOrganizationService.getByCondition(pageRequest, query, districtId);

        // 为空直接返回
        List<ScreeningOrgResponseDTO> orgListsRecords = orgLists.getRecords();
        if (CollectionUtils.isEmpty(orgListsRecords)) {
            return orgLists;
        }

        // 获取筛查人员信息
        Map<Integer, List<ScreeningOrganizationStaff>> staffMaps = screeningOrganizationStaffService
                .getOrgStaffMapByIds(orgListsRecords.stream().map(ScreeningOrganization::getId)
                        .collect(Collectors.toList()));
        // 获取已有任务的机构ID列表
        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);

        // 筛查次数
        List<ScreeningPlan> planLists = screeningPlanService
                .getByOrgIds(orgListsRecords.stream().map(ScreeningOrganization::getId)
                        .collect(Collectors.toList()));
        Map<Integer, Long> orgPlanMaps = planLists.stream().collect(Collectors
                .groupingBy(ScreeningPlan::getScreeningOrgId, Collectors.counting()));
        // 封装DTO
        orgListsRecords.forEach(orgResponseDTO -> {
            // 同一部门才能更新
            if (currentUser.isPlatformAdminUser()) {
                orgResponseDTO.setCanUpdate(true);
            } else if (orgResponseDTO.getCreateUserId().equals(currentUser.getId())) {
                orgResponseDTO.setCanUpdate(true);
            }

            // 筛查人员
            List<ScreeningOrganizationStaff> staffLists = staffMaps.get(orgResponseDTO.getId());
            if (!CollectionUtils.isEmpty(staffLists)) {
                orgResponseDTO.setStaffCount(staffLists.size());
            } else {
                orgResponseDTO.setStaffCount(0);
            }
            // 区域名字
            orgResponseDTO.setDistrictName(districtService.getDistrictName(orgResponseDTO.getDistrictDetail()));

            // 筛查次数
            orgResponseDTO.setScreeningTime(orgPlanMaps.getOrDefault(orgResponseDTO.getId(), 0L));
            orgResponseDTO.setAlreadyHaveTask(haveTaskOrgIds.contains(orgResponseDTO.getId()));

            // 详细地址
            orgResponseDTO.setAddressDetail(districtService.getAddressDetails(
                    orgResponseDTO.getProvinceCode(), orgResponseDTO.getCityCode(), orgResponseDTO.getAreaCode(), orgResponseDTO.getTownCode(), orgResponseDTO.getAddress()));
        });
        return orgLists;
    }

    /**
     * 根据部门ID获取筛查机构列表（带是否已有任务）
     *
     * @param query 筛查机构列表请求体
     * @return List<ScreeningOrgResponse>
     */
    public List<ScreeningOrgResponseDTO> getScreeningOrganizationListByGovDeptId(ScreeningOrganizationQueryDTO query) {
        Assert.notNull(query.getGovDeptId(), "部门id不能为空");
        query.setStatus(CommonConst.STATUS_NOT_DELETED);
        // 查询
        List<ScreeningOrganization> screeningOrganizationList = screeningOrganizationService.getBy(query);
        // 为空直接返回
        if (CollectionUtils.isEmpty(screeningOrganizationList)) {
            return Collections.emptyList();
        }
        // 获取已有任务的机构ID列表
        List<Integer> haveTaskOrgIds = getHaveTaskOrgIds(query);
        // 封装DTO
        return screeningOrganizationList.stream().map(organization -> {
            ScreeningOrgResponseDTO orgResponseDTO = new ScreeningOrgResponseDTO();
            BeanUtils.copyProperties(organization, orgResponseDTO);
            orgResponseDTO.setAlreadyHaveTask(haveTaskOrgIds.contains(organization.getId()));
            return orgResponseDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 根据是否需要查询机构是否已有任务，返回时间段内已有任务的机构id
     *
     * @param query 条件
     * @return List<Integer>
     */
    private List<Integer> getHaveTaskOrgIds(ScreeningOrganizationQueryDTO query) {
        if (Objects.nonNull(query.getNeedCheckHaveTask()) && query.getNeedCheckHaveTask()) {
            return screeningTaskOrgService.getHaveTaskOrgIds(query.getGovDeptId(), query.getStartTime(), query.getEndTime());
        }
        return Collections.emptyList();
    }
}
