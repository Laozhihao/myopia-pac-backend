package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.dto.StudentCardResponseDTO;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.management.service.ScreeningPlanService;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.management.service.VisionScreeningResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningResult")
public class VisionScreeningResultController extends BaseController<VisionScreeningResultService, VisionScreeningResult> {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    /**
     * 获取档案卡列表
     *
     * @param schoolId
     * @param planId
     * @return
     */
    @GetMapping("/list-result")
    public List<StudentCardResponseDTO> listStudentScreeningResult(@RequestParam Integer schoolId, @RequestParam Integer planId) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        if (screeningPlan == null) {
            throw new BusinessException("无法找到该筛查计划");
        }
        Integer screeningPlanId = screeningPlan.getId();
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanId);
        screeningPlanSchoolStudents = screeningPlanSchoolStudents.stream().filter(screeningPlanSchoolStudent -> screeningPlanSchoolStudent.getSchoolId().equals(schoolId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
            return new ArrayList<>();
        }
        Set<Integer> screeningPlanSchoolStudentIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentIds);
        return visionScreeningResults.stream().map(visionScreeningResult ->
                studentService.getStudentCardResponseDTO(visionScreeningResult)
        ).collect(Collectors.toList());
    }

}
