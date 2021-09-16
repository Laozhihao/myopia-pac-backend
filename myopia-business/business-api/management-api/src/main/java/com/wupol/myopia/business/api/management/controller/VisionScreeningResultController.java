package com.wupol.myopia.business.api.management.controller;

import com.google.common.collect.Lists;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.api.management.service.StudentBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.interfaces.HasName;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCardResponseVO;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
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
    private SchoolService schoolService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ExcelFacade excelFacade;
    @Autowired
    private StudentBizService studentBizService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取档案卡列表
     *
     * @param schoolId 学校Id
     * @param planId   计划Id
     * @param gradeId  年纪Id
     * @param classId  班级Id
     * @return List<StudentCardResponseVO>
     */
    @GetMapping("/list-result")
    public List<StudentCardResponseVO> listStudentScreeningResult(@RequestParam Integer schoolId,
                                                                  @RequestParam Integer planId, @RequestParam(required = false) Integer resultId,
                                                                  @RequestParam(required = false) Integer gradeId, @RequestParam(required = false) Integer classId,
                                                                  @RequestParam(value = "planStudentIds", required = false) Set<Integer> planStudentIds) {
        // 方便前端模板渲染复用
        if (Objects.nonNull(resultId)) {
            VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(resultId);
            return Lists.newArrayList(studentBizService.getStudentCardResponseDTO(visionScreeningResult));
        }
        ScreeningPlan screeningPlan = screeningPlanService.getById(planId);
        if (screeningPlan == null) {
            throw new BusinessException("无法找到该筛查计划");
        }
        Set<Integer> screeningPlanSchoolStudentIds;
        if (CollectionUtils.isEmpty(planStudentIds)) {
            Integer screeningPlanId = screeningPlan.getId();
            List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeIdAndClassId(screeningPlanId, schoolId,
                    gradeId, classId);
            if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
                return new ArrayList<>();
            }
            screeningPlanSchoolStudentIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
        } else {
            screeningPlanSchoolStudentIds = planStudentIds;
        }
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentIds);
        return studentBizService.generateBatchStudentCard(visionScreeningResults);
    }

    /**
     * 筛查通知-导出筛查数据（screeningOrgId、districtId与schoolId不能同时为0）
     * <p>这个接口需要考虑到筛查机构为单点的情况，为单点筛查机构，筛查通知Id可以为空
     * 单点筛查机构没有筛查通知</p>
     *
     * @param screeningNoticeId 筛查通知Id
     * @param screeningOrgId    筛查机构ID，默认0
     * @param districtId        层级ID，默认0
     * @param schoolId          学校ID，默认0
     * @return ApiResult.success();
     */
    @GetMapping("/export")
    public Object getScreeningNoticeExportData(Integer screeningNoticeId, @RequestParam(defaultValue = "0") Integer screeningOrgId,
                                               @RequestParam(defaultValue = "0") Integer districtId, @RequestParam(defaultValue = "0") Integer schoolId,
                                               @RequestParam(defaultValue = "0") Integer planId) throws IOException, UtilException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        // 参数校验
        validateExportParams(screeningNoticeId, screeningOrgId, districtId, schoolId, planId);
        List<StatConclusionExportDTO> statConclusionExportVos = new ArrayList<>();
        // 获取文件需显示的名称的机构/学校/区域前缀
        String exportFileNamePrefix = "";
        boolean isSchoolExport = false;

        // 是否单点机构
        if (currentUser.isScreeningUser() && ScreeningOrgConfigTypeEnum.CONFIG_TYPE_1.getType().equals(screeningOrganizationService.getById(currentUser.getOrgId()).getConfigType())) {
            exportFileNamePrefix = checkNotNullAndGetName(screeningOrganizationService.getById(currentUser.getOrgId()), "筛查机构");
            if (Objects.isNull(planId)) {
                throw new BusinessException("单点筛查机构PlanId不能为空");
            }
            statConclusionExportVos = statConclusionService.getExportVoByScreeningPlanIdAndScreeningOrgId(planId, currentUser.getOrgId());
        } else {
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
        }
        if (CollectionUtils.isEmpty(statConclusionExportVos)) {
            throw new BusinessException("暂无筛查数据，无法导出");
        }
        // 重复导出
        String key = String.format(RedisConstant.FILE_EXPORT_NOTICE_DATA, screeningNoticeId, screeningOrgId, districtId, schoolId, planId, currentUser.getId());
        checkIsExport(key);

        statConclusionExportVos.forEach(vo -> vo.setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress())));
        // 获取文件需显示的名称
        excelFacade.generateVisionScreeningResult(currentUser.getId(), statConclusionExportVos, isSchoolExport, exportFileNamePrefix, key);
        return ApiResult.success();
    }

    /**
     * 筛查计划-导出筛查数据（screeningOrgId与schoolId不能同时为0）
     * @param screeningPlanId
     * @param screeningOrgId 筛查机构ID，默认0
     * @param schoolId 学校ID，默认0
     * @return
     */
    @GetMapping("/plan/export")
    public Object getScreeningPlanExportData(Integer screeningPlanId, @RequestParam(defaultValue = "0") Integer screeningOrgId,
                                             @RequestParam(defaultValue = "0") Integer schoolId) throws IOException, UtilException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        // 参数校验
        validatePlanExportParams(screeningPlanId, screeningOrgId, schoolId);
        List<StatConclusionExportDTO> statConclusionExportDTOs = new ArrayList<>();
        // 获取文件需显示的名称的机构/学校/区域前缀
        String exportFileNamePrefix = "";
        boolean isSchoolExport = false;
        if (!CommonConst.DEFAULT_ID.equals(screeningOrgId)) {
            exportFileNamePrefix = checkNotNullAndGetName(screeningOrganizationService.getById(screeningOrgId), "筛查机构");
            statConclusionExportDTOs = statConclusionService.getExportVoByScreeningPlanIdAndScreeningOrgId(screeningPlanId, screeningOrgId);
        }
        if (!CommonConst.DEFAULT_ID.equals(schoolId)) {
            exportFileNamePrefix = checkNotNullAndGetName(schoolService.getById(schoolId), "学校");
            isSchoolExport = true;
            statConclusionExportDTOs = statConclusionService.getExportVoByScreeningPlanIdAndSchoolId(screeningPlanId, schoolId);
        }
        if (CollectionUtils.isEmpty(statConclusionExportDTOs)) {
            throw new BusinessException("暂无筛查数据，无法导出");
        }
        statConclusionExportDTOs.forEach(vo -> vo.setAddress(districtService.getAddressDetails(vo.getProvinceCode(), vo.getCityCode(), vo.getAreaCode(), vo.getTownCode(), vo.getAddress())));
        String key = String.format(RedisConstant.FILE_EXPORT_PLAN_DATA, screeningPlanId,screeningOrgId,schoolId,currentUser.getId());
        checkIsExport(key);
        // 获取文件需显示的名称
        excelFacade.generateVisionScreeningResult(currentUser.getId(), statConclusionExportDTOs, isSchoolExport, exportFileNamePrefix, key);
        return ApiResult.success();
    }

    /**
     * 是否正在导出
     * @param key Key
     */
    private void checkIsExport(String key) {
        Object o = redisUtil.get(key);
        if (Objects.nonNull(o)) {
            throw new BusinessException("正在导出中，请勿重复导出");
        }
        redisUtil.set(key, 1, 60 * 60 * 24);
    }

    /**
     * 校验筛查数据导出参数
     * @param screeningNoticeId
     * @param screeningOrgId
     * @param districtId
     * @param schoolId
     * @param planId
     */
    private void validateExportParams(Integer screeningNoticeId, Integer screeningOrgId,
                                      Integer districtId, Integer schoolId,Integer planId) {
        if (Objects.nonNull(planId)) {
            return;
        }
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
     * 校验筛查数据导出参数
     * @param screeningPlanId
     * @param screeningOrgId
     * @param schoolId
     */
    private void validatePlanExportParams(Integer screeningPlanId, Integer screeningOrgId, Integer schoolId) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
        if (Objects.isNull(screeningPlan)) {
            throw new BusinessException("筛查计划不存在");
        }
        List<Integer> needCheckIdList = Arrays.asList(screeningOrgId, schoolId);
        if (needCheckIdList.stream().filter(i -> !CommonConst.DEFAULT_ID.equals(i)).count() != 1) {
            throw new BusinessException("必须选择层级、学校或筛查机构中一个维度");
        }
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

    @GetMapping("/screening/planStudent/card/{planStudentId}")
    public List<StudentCardResponseVO> getResultByPlanStudentId(@PathVariable("planStudentId") Integer planStudentId) {
        return studentBizService.getCardDetailByPlanStudentId(planStudentId);
    }
}
