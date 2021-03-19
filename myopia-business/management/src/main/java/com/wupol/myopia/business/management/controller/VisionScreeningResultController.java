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
import com.wupol.myopia.business.management.facade.ExcelFacade;
import com.wupol.myopia.business.management.interfaces.HasName;
import com.wupol.myopia.business.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
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
    private ScreeningOrganizationService screeningOrganizationService;
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
     * 导出筛查数据（screeningOrgId、districtId与schoolId不能同时为0）
     * @param screeningNoticeId
     * @param screeningOrgId 筛查机构ID，默认0
     * @param districtId 层级ID，默认0
     * @param schoolId 学校ID，默认0
     * @return
     */
    @GetMapping("/export")
    public Object getOrganizationExportData(Integer screeningNoticeId, @RequestParam(defaultValue = "0") Integer screeningOrgId, @RequestParam(defaultValue = "0") Integer districtId,  @RequestParam(defaultValue = "0") Integer schoolId) throws IOException, UtilException {
        // 参数校验
        validateExportParams(screeningNoticeId, screeningOrgId, districtId, schoolId);
        List<StatConclusionExportVo> statConclusionExportVos = new ArrayList<>();
        // 获取文件需显示的名称的机构/学校/区域前缀
        String exportFileNamePrefix = "";
        Boolean isSchoolExport = false;
        if (!CommonConst.DEFAULT_ID.equals(screeningOrgId)) {
            exportFileNamePrefix = checkNotNullAndGetName(screeningOrganizationService.getById(screeningOrgId), "筛查机构");
            statConclusionExportVos = statConclusionService.getExportVoByScreeningNoticeIdAndScreeningOrgId(screeningNoticeId, screeningOrgId);
        }
        if (!CommonConst.DEFAULT_ID.equals(districtId)) {
            exportFileNamePrefix = checkNotNullAndGetName(districtService.getById(districtId), "行政区域");
            // 合计的要包括自己层级的筛查数据
            List<Integer> childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
            statConclusionExportVos = statConclusionService.getExportVoByScreeningNoticeIdAndDistrictIds(screeningNoticeId, childDistrictIds);
        }
        if (!CommonConst.DEFAULT_ID.equals(schoolId)) {
            exportFileNamePrefix = checkNotNullAndGetName(schoolService.getById(schoolId), "学校");
            isSchoolExport = true;
            statConclusionExportVos = statConclusionService.getExportVoByScreeningNoticeIdAndSchoolId(screeningNoticeId, schoolId);
        }
        initAddressFullPath(statConclusionExportVos);
        // 获取文件需显示的名称
        excelFacade.generateVisionScreeningResult(CurrentUserUtil.getCurrentUser().getId(), statConclusionExportVos, isSchoolExport, exportFileNamePrefix);
        return ApiResult.success();
    }

    /**
     * 校验筛查数据导出参数
     * @param screeningNoticeId
     * @param screeningOrgId
     * @param districtId
     * @param schoolId
     */
    private void validateExportParams(Integer screeningNoticeId, Integer screeningOrgId, Integer districtId, Integer schoolId) {
        ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
        if (Objects.isNull(screeningNotice)) {
            throw new BusinessException("筛查通知不存在");
        }
        List<Integer> needCheckIdList = Arrays.asList(screeningOrgId, districtId, schoolId);
        if (needCheckIdList.stream().filter(i -> !CommonConst.DEFAULT_ID.equals(i)).count() != 1) {
            throw new BusinessException("必须选择层级、学校或筛查机构中一个维度");
        }
    }

    /**
     * 组装全地址
     * @param statConclusionExportVos
     */
    private void initAddressFullPath(List<StatConclusionExportVo> statConclusionExportVos) {
        Set<Long> districtCode = new HashSet<>();
        statConclusionExportVos.forEach(vo -> {
            districtCode.add(vo.getProvinceCode());
            districtCode.add(vo.getCityCode());
            districtCode.add(vo.getTownCode());
            districtCode.add(vo.getAreaCode());
        });
        Set<Long> notNullDistrictCode = districtCode.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> districtCodeNameMap = districtService.getByCodes(new ArrayList<>(notNullDistrictCode)).stream().collect(Collectors.toMap(District::getCode, District::getName));
        statConclusionExportVos.forEach(vo -> vo.setAddress(String.join(districtCodeNameMap.getOrDefault(vo.getProvinceCode(), ""), districtCodeNameMap.getOrDefault(vo.getCityCode(), ""), districtCodeNameMap.getOrDefault(vo.getAreaCode(), ""), districtCodeNameMap.getOrDefault(vo.getTownCode(), ""), vo.getAddress())));
    }

    /**
     * 判空并获取名称
     * @param object
     * @param desc
     * @return
     */
    private <T extends HasName> String checkNotNullAndGetName(T object, String desc) {
        if (Objects.isNull(object)) {
            throw new BusinessException(String.format("未找到该%s", desc));
        }
        return object.getName();
    }
}
