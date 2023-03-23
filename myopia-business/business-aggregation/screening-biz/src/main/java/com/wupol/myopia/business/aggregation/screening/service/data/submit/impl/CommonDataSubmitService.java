package com.wupol.myopia.business.aggregation.screening.service.data.submit.impl;

import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.PlanStudentInfoDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用
 *
 * @author Simple4H
 */
@Service
public class CommonDataSubmitService {

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private SchoolStudentService schoolStudentService;

    /**
     * 通过证件号获取筛查信息
     */
    public Map<String, VisionScreeningResult> getScreeningData(List<Map<Integer, String>> listMap, Integer schoolId, Function<Map<Integer, String>, String> function) {
        List<String> credentialsLists = listMap.stream().map(function).collect(Collectors.toList());
        List<SchoolStudent> studentList = schoolStudentService.getByIdCardsOrPassports(credentialsLists, credentialsLists, schoolId).stream().filter(s -> Objects.equals(s.getSchoolId(), schoolId)).collect(Collectors.toList());

        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getLastByStudentIds(studentList.stream().map(SchoolStudent::getStudentId).collect(Collectors.toList()), schoolId);

        return studentList.stream()
                .filter(s -> StringUtils.isNotBlank(s.getIdCard()) || StringUtils.isNotBlank(s.getPassport()))
                .collect(Collectors.toMap(s -> StringUtils.upperCase(StringUtils.isNotBlank(s.getIdCard()) ? s.getIdCard() : s.getPassport()), s -> resultMap.getOrDefault(s.getStudentId(), new VisionScreeningResult())));
    }

    /**
     * 通过证件号在筛查计划中获取筛查数据
     */
    public Map<String, VisionScreeningResult> getScreeningData(List<Map<Integer, String>> listMap, Integer schoolId, Integer screeningPlanId, Function<Map<Integer, String>, String> function) {

        List<String> credentialsList = listMap.stream().map(function).collect(Collectors.toList());
        // 筛查计划中学生数据查询
        List<PlanStudentInfoDTO> planStudentList = screeningPlanSchoolStudentService.getByCredentials(schoolId, screeningPlanId, credentialsList, credentialsList);
        // 根据学生id查询筛查信息
        Map<Integer, VisionScreeningResult> resultMap = visionScreeningResultService.getFirstMap(planStudentList.stream().map(PlanStudentInfoDTO::getId).collect(Collectors.toList()), schoolId, screeningPlanId);

        return planStudentList.stream()
                .filter(s -> StringUtils.isNotBlank(s.getIdCard()) || StringUtils.isNotBlank(s.getPassport()))
                .collect(Collectors.toMap(s -> StringUtils.upperCase(StringUtils.isNotBlank(s.getIdCard()) ? s.getIdCard() : s.getPassport()), s -> resultMap.getOrDefault(s.getId(), new VisionScreeningResult())));
    }
}
