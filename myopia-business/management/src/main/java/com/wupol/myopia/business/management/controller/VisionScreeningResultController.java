package com.wupol.myopia.business.management.controller;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.StudentCardResponseDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.vo.StatConclusionExportVo;
import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
import com.wupol.myopia.business.management.domain.vo.VisionScreeningResultExportVo;
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private DistrictService districtService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ExcelFacade excelFacade;

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

    /**
     * 导出筛查数据（districtId与schoolId不能同时为0）
     * @param screeningNoticeId
     * @param districtId 层级ID，默认0
     * @param schoolId 学校ID，默认0
     * @return
     */
    @GetMapping("/export")
    public Object getOrganizationExportData(Integer screeningNoticeId, @RequestParam(defaultValue = "0") Integer districtId,  @RequestParam(defaultValue = "0") Integer schoolId) throws IOException, UtilException {
        ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
        if (Objects.isNull(screeningNotice)) {
            throw new BusinessException("筛查通知不存在");
        }
        if (CommonConst.DEFAULT_ID.equals(districtId) && CommonConst.DEFAULT_ID.equals(schoolId)) {
            throw new BusinessException("层级与学校必须选择一个");
        }

        List<StatConclusionExportVo> statConclusionExportVos = new ArrayList<>();
        if (!CommonConst.DEFAULT_ID.equals(districtId)) {
            // 合计的要包括自己层级的筛查数据
            List<Integer> childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
            statConclusionExportVos = statConclusionService.getExportVoByScreeningNoticeIdAndDistrictIds(screeningNoticeId, childDistrictIds);
        }
        if (!CommonConst.DEFAULT_ID.equals(schoolId)) {
            statConclusionExportVos = statConclusionService.getExportVoByScreeningNoticeIdAndSchoolId(screeningNoticeId, schoolId);
        }
        // 获取文件需显示的名称
        String districtOrSchoolName = getDistrictOrSchoolName(districtId, schoolId);
        excelFacade.generateVisionScreeningResult(CurrentUserUtil.getCurrentUser().getId(), statConclusionExportVos, districtId, schoolId, districtOrSchoolName);
        return ApiResult.success();
    }

    /**
     * 获取学校或区域层级名称
     * @param districtId
     * @param schoolId
     * @return
     * @throws IOException
     */
    private String getDistrictOrSchoolName(Integer districtId, Integer schoolId) throws IOException {
        if (!CommonConst.DEFAULT_ID.equals(districtId)) {
            District district = districtService.getById(districtId);
            if (Objects.isNull(district)) {
                throw new BusinessException("未找到该行政区域");
            }
            return district.getName();
        } else if (!CommonConst.DEFAULT_ID.equals(schoolId)) {
            School school = schoolService.getById(schoolId);
            if (Objects.isNull(school)) {
                throw new BusinessException("未找到该学校");
            }
            return school.getName();
        }
        throw new BusinessException("层级或学校必须选择一个");
    }
}
