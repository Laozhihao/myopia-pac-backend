package com.wupol.myopia.business.api.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.api.management.domain.vo.SchoolGradeVO;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.DistrictService;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanPageDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.facade.ScreeningRelatedFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2021/4/23 10:31
 */
@Service
public class ScreeningPlanBizService {

    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ScreeningRelatedFacade screeningRelatedFacade;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningPlanPageDTO> getPage(ScreeningPlanQueryDTO query, PageRequest pageRequest) {
        Page<ScreeningPlan> page = (Page<ScreeningPlan>) pageRequest.toPage();
        if (StringUtils.isNotBlank(query.getCreatorNameLike()) && screeningRelatedFacade.initCreateUserIdsAndReturnIsEmpty(query)) {
            return new Page<>();
        }
        if (StringUtils.isNotBlank(query.getScreeningOrgNameLike())) {
            List<Integer> orgIds = screeningOrganizationService.getByNameLike(query.getScreeningOrgNameLike()).stream().map(ScreeningOrganization::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(orgIds)) {
                // 可以直接返回空
                return new Page<>();
            }
            query.setScreeningOrgIds(orgIds);
        }
        IPage<ScreeningPlanPageDTO> screeningPlanIPage = screeningPlanService.selectPageByQuery(page, query);
        // 设置创建人、地址及部门名称
        List<Integer> allGovDeptIds = screeningPlanIPage.getRecords().stream().map(ScreeningPlanPageDTO::getGovDeptId).distinct().collect(Collectors.toList());
        Map<Integer, String> govDeptIdNameMap = org.springframework.util.CollectionUtils.isEmpty(allGovDeptIds) ? Collections.emptyMap() : govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        List<Integer> userIds = screeningPlanIPage.getRecords().stream().map(ScreeningPlan::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));
        screeningPlanIPage.getRecords().forEach(vo -> {
            vo.setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), ""))
                    .setDistrictName(districtService.getDistrictNameByDistrictId(vo.getDistrictId()))
                    .setGovDeptName(govDeptIdNameMap.getOrDefault(vo.getGovDeptId(), ""));
        });
        return screeningPlanIPage;
    }


    /**
     * 获取计划中的学校年级情况
     *
     * @param screeningPlanId
     * @param schoolId
     * @return
     */
    public List<SchoolGradeVO> getSchoolGradeVoByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        //1. 获取该计划学校的筛查学生所有年级、班级
        List<GradeClassesDTO> gradeClasses = screeningPlanSchoolStudentService.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
        //2. 根据年级分组
        Map<Integer, List<GradeClassesDTO>> graderIdClasses = gradeClasses.stream().collect(Collectors.groupingBy(GradeClassesDTO::getGradeId));
        //3. 组装SchoolGradeVo数据
        return graderIdClasses.keySet().stream().map(gradeId -> {
            SchoolGradeVO vo = new SchoolGradeVO();
            List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(gradeId);
            // 查询并设置年级名称
            SchoolGrade grade = schoolGradeService.getById(gradeId);
            vo.setId(gradeId).setName(Objects.nonNull(grade) ? grade.getName() : "");
            // 查询并设置班级名称
            vo.setClasses(gradeClassesDTOS.stream().map(dto -> {
                SchoolClass schoolClass = new SchoolClass();
                SchoolClass classById = schoolClassService.getById(dto.getClassId());
                schoolClass.setId(dto.getClassId()).setName(Objects.nonNull(classById) ? classById.getName() : "");
                return schoolClass;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

}
