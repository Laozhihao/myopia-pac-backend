package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.RecordDetails;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningOrgPlanResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningRecordItems;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 筛查机构
 *
 * @author Simple4H
 */
@Service
public class ScreeningOrganizationFacade {

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

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
            List<UserDTO> userDTOS = oauthService.getUserBatchByIds(createUserIds);
            response.setStaffCount(createUserIds.size());
            response.setStaffName(userDTOS
                    .stream().map(UserDTO::getRealName).collect(Collectors.toList()));
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
}
